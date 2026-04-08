<script lang="ts">
	import { browser } from "$app/environment";
	import { goto } from "$app/navigation";
	import {
		AlertCircle,
		ArrowRight,
		CheckCircle2,
		ExternalLink,
		Eye,
		EyeOff,
		KeyRound,
		Landmark,
		Loader2,
		ShieldCheck,
		Wallet
	} from "@lucide/svelte";
	import ThemeToggle from "$lib/components/layout/theme-toggle.svelte";
	import { Button } from "$lib/components/ui/button";
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "$lib/components/ui/card";
	import { Input } from "$lib/components/ui/input";
	import { Label } from "$lib/components/ui/label";
	import {
		createConnectToken,
		fetchAccounts,
		getApiErrorMessage,
		saveBankConnection,
		saveCredentials,
		syncData
	} from "$lib/api";
	import { setupState } from "$lib/state/setup.svelte";

	type WizardStep = "welcome" | "credentials" | "connect" | "success";

	const steps = [
		{ key: "welcome", label: "Introdução", icon: ShieldCheck },
		{ key: "credentials", label: "Credenciais", icon: KeyRound },
		{ key: "connect", label: "Conectar Banco", icon: Landmark },
		{ key: "success", label: "Pronto", icon: CheckCircle2 }
	] as const;

	const WELCOME_DESCRIPTION_CLASS = "mx-auto max-w-[52ch] text-sm leading-relaxed text-muted-foreground";
	const DATE_FIELD_LABEL_CLASS = "text-xs font-medium tracking-wide text-muted-foreground";
	const PLUGGY_SDK_SRC = "https://cdn.pluggy.ai/pluggy-connect/v1.3.1/pluggy-connect.js";

	let step = $state<WizardStep>("welcome");
	let connectToken = $state<string | null>(null);
	let finishing = $state(false);

	let clientId = $state("");
	let clientSecret = $state("");
	let showClientSecret = $state(false);
	let credentialsLoading = $state(false);
	let credentialsError = $state<string | null>(null);

	let connectLoading = $state(false);
	let connectError = $state<string | null>(null);
	let connectScriptLoading = $state(false);
	let connectScriptReady = $state(false);
	let connectScriptError = $state<string | null>(null);

	const currentStepIndex = $derived(steps.findIndex((item) => item.key === step));
	const invalidCredentials = $derived(!clientId.trim() || !clientSecret.trim());
	const canOpenConnectWidget = $derived(
		!connectLoading && !!connectToken && connectScriptReady && !connectScriptLoading
	);

	$effect(() => {
		if (setupState.isSetupComplete) {
			void goto("/", { replaceState: true });
		}
	});

	$effect(() => {
		if (!browser || step !== "connect" || !connectToken) return;
		if (connectScriptReady || connectScriptLoading) return;

		if (window.PluggyConnect || window.Pluggy) {
			connectScriptReady = true;
			connectScriptError = null;
			return;
		}

		connectScriptLoading = true;
		connectScriptError = null;

		const existingScript = document.querySelector<HTMLScriptElement>(
			`script[src="${PLUGGY_SDK_SRC}"]`
		);

		const script = existingScript ?? document.createElement("script");
		if (!existingScript) {
			script.src = PLUGGY_SDK_SRC;
			script.async = true;
			document.head.appendChild(script);
		} else if (window.PluggyConnect || window.Pluggy) {
			connectScriptLoading = false;
			connectScriptReady = true;
			return;
		}

		const handleLoad = () => {
			connectScriptLoading = false;
			connectScriptReady = !!(window.PluggyConnect || window.Pluggy);
			if (!connectScriptReady) {
				connectScriptError =
					"O SDK da Pluggy foi carregado, mas não inicializou corretamente. Tente recarregar a página.";
			}
		};

		const handleError = () => {
			connectScriptLoading = false;
			connectScriptReady = false;
			connectScriptError =
				"Não foi possível carregar o SDK da Pluggy. Verifique sua conexão e tente novamente.";
		};

		script.addEventListener("load", handleLoad);
		script.addEventListener("error", handleError);

		return () => {
			script.removeEventListener("load", handleLoad);
			script.removeEventListener("error", handleError);
		};
	});

	function getPluggyGlobal(): PluggyConnectGlobal | null {
		if (!browser) return null;
		return window.PluggyConnect ?? window.Pluggy ?? null;
	}

	function openPluggyConnect(options: PluggyConnectOptions) {
		const pluggyGlobal = getPluggyGlobal();
		if (!pluggyGlobal) {
			throw new Error("SDK da Pluggy não está disponível.");
		}

		if (typeof pluggyGlobal === "function") {
			try {
				const instance = new pluggyGlobal(options);
				if (instance?.open) {
					instance.open();
					return;
				}
				if (instance?.init) {
					instance.init();
					return;
				}
			} catch {
				const instance = pluggyGlobal(options);
				if (instance?.open) {
					instance.open();
					return;
				}
				if (instance?.init) {
					instance.init();
					return;
				}
			}
		}

		if (typeof pluggyGlobal === "object") {
			if (typeof pluggyGlobal.create === "function") {
				const instance = pluggyGlobal.create(options);
				if (instance?.open) {
					instance.open();
					return;
				}
				if (instance?.init) {
					instance.init();
					return;
				}
			}

			if (typeof pluggyGlobal.init === "function") {
				const instance = pluggyGlobal.init(options);
				if (instance?.open) {
					instance.open();
					return;
				}
				if (instance?.init) {
					instance.init();
					return;
				}
			}
		}

		throw new Error("Não foi possível inicializar o Pluggy Connect.");
	}

	function goToCredentialsStep() {
		step = "credentials";
	}

	async function handleCredentialsContinue() {
		credentialsError = null;

		if (invalidCredentials) {
			credentialsError = "Preencha ambos os campos.";
			return;
		}

		credentialsLoading = true;
		try {
			await saveCredentials({
				clientId: clientId.trim(),
				clientSecret: clientSecret.trim()
			});

			const response = await createConnectToken();
			connectToken = response.connectToken;
			step = "connect";
		} catch (error) {
			credentialsError = getApiErrorMessage(
				error,
				"Não foi possível salvar as credenciais. Verifique os dados e tente novamente."
			);
		} finally {
			credentialsLoading = false;
		}
	}

	async function handlePluggySuccess(payload: PluggyConnectSuccessPayload) {
		connectError = null;
		const itemId = payload.item?.id?.trim();
		if (!itemId) {
			connectError = "A Pluggy não retornou um itemId válido para salvar a conexão.";
			return;
		}

		connectLoading = true;
		try {
			const bankName =
				payload.item.connector?.name?.trim() ||
				payload.item.institution?.name?.trim() ||
				"Banco não identificado";

			await saveBankConnection({ itemId, bankName });

			// Inviolable contract: advance only after backend persistence succeeds.
			step = "success";
		} catch (error) {
			connectError = getApiErrorMessage(
				error,
				"A conexão foi autorizada, mas falhou ao salvar no servidor."
			);
		} finally {
			connectLoading = false;
		}
	}

	function handlePluggyError(payload: PluggyConnectErrorPayload) {
		connectError = payload.message || "Erro ao conectar com o banco.";
	}

	function handleOpenConnectWidget() {
		if (!connectToken) return;

		connectError = null;

		const theme: "light" | "dark" =
			browser && document.documentElement.classList.contains("dark") ? "dark" : "light";

		try {
			openPluggyConnect({
				connectToken,
				includeSandbox: true,
				theme,
				language: "pt",
				onSuccess: (payload) => {
					void handlePluggySuccess(payload);
				},
				onError: handlePluggyError
			});
		} catch (error) {
			connectError = getApiErrorMessage(error, "Não foi possível abrir o widget da Pluggy.");
		}
	}

	async function handleFinish() {
		finishing = true;
		try {
			await syncData();
			const accounts = await fetchAccounts();
			setupState.completeSetup(accounts);
			await goto("/", { replaceState: true });
		} catch {
			try {
				const accounts = await fetchAccounts();
				setupState.completeSetup(accounts);
				await goto("/", { replaceState: true });
			} catch {
				finishing = false;
			}
		}
	}
</script>

<div class="relative flex min-h-screen flex-col items-center justify-center bg-background px-4 py-10">
	<div class="absolute top-4 right-4 z-10 md:top-6 md:right-6">
		<ThemeToggle />
	</div>

	<div class="mb-8 flex items-center gap-2.5">
		<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-primary">
			<Wallet class="h-5 w-5 text-primary-foreground" />
		</div>
		<span class="text-xl font-semibold tracking-tight text-foreground">Personal Finance</span>
	</div>

	<div class="mb-7 flex items-center justify-center gap-3">
		{#each steps as item, index (item.key)}
			{#if index > 0}
				<div
					class={`h-px w-10 transition-colors ${index < currentStepIndex ? "bg-primary/70" : "bg-border"}`}
				></div>
			{/if}
			<div class="flex items-center gap-2">
				<div
					class={`flex h-8 w-8 items-center justify-center rounded-full border transition-colors ${
						index === currentStepIndex
							? "border-primary bg-primary text-primary-foreground"
							: index < currentStepIndex
								? "border-primary/30 bg-primary/15 text-primary"
								: "border-border bg-muted text-muted-foreground"
					}`}
				>
					<item.icon class="h-4 w-4" />
				</div>
				<span
					class={`hidden text-sm font-medium sm:inline ${
						index === currentStepIndex ? "text-foreground" : "text-muted-foreground"
					}`}
				>
					{item.label}
				</span>
			</div>
		{/each}
	</div>

	{#if step === "welcome"}
		<Card class="w-full max-w-xl border-border shadow-sm">
			<CardHeader class="text-center">
				<div class="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-primary/10">
					<ShieldCheck class="h-7 w-7 text-primary" />
				</div>
				<CardTitle class="text-xl font-semibold text-card-foreground">
					Conexão Segura via Open Finance
				</CardTitle>
				<CardDescription class={WELCOME_DESCRIPTION_CLASS}>
					Utilizamos a
					<a
						href="https://pluggy.ai"
						target="_blank"
						rel="noopener noreferrer"
						class="font-medium text-foreground underline underline-offset-4"
					>
						Pluggy
					</a>
					como infraestrutura oficial do
					<span class="font-medium text-foreground">Open Finance Brasil</span>
					para conectar suas contas de forma segura e criptografada.
				</CardDescription>
			</CardHeader>
			<CardContent class="space-y-6">
				<div class="space-y-4">
					<p class="text-xs font-medium tracking-wide text-muted-foreground">Como funciona</p>
					<div class="space-y-3">
						{#each [
							{
								number: "1",
								title: "Crie uma conta na Pluggy",
								description:
									"Acesse o dashboard da Pluggy e crie uma conta gratuita. É rápido e não exige cartão de crédito."
							},
							{
								number: "2",
								title: "Copie suas credenciais (API Keys)",
								description:
									"No painel da Pluggy, vá em Application Keys e copie o Client ID e Client Secret."
							},
							{
								number: "3",
								title: "Cole aqui e conecte seu banco",
								description:
									"Na próxima etapa você informará as keys. Depois, basta selecionar seu banco e autorizar a conexão."
							}
						] as info (info.number)}
							<div class="flex gap-3 rounded-lg px-1 py-1 transition-colors hover:bg-muted/25">
								<div
									class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full border border-border bg-muted/50 text-xs font-semibold text-muted-foreground"
								>
									{info.number}
								</div>
								<div>
									<p class="text-sm font-medium text-card-foreground">{info.title}</p>
									<p class="text-sm leading-relaxed text-muted-foreground">{info.description}</p>
								</div>
							</div>
						{/each}
					</div>
				</div>

				<div class="rounded-lg border border-border bg-muted/30 p-4">
					<div class="flex items-start gap-2.5">
						<ShieldCheck class="mt-0.5 h-4 w-4 shrink-0 text-success" />
						<div class="space-y-1">
							<p class="text-sm font-medium text-card-foreground">100% seguro</p>
							<p class="text-xs text-muted-foreground">
								Toda a comunicação é feita via protocolo oficial do Open Finance, regulado pelo Banco
								Central do Brasil. Nenhuma senha bancária é compartilhada — a autorização é feita
								diretamente no app do seu banco.
							</p>
						</div>
					</div>
				</div>

				<div class="flex flex-col gap-3">
					<a
						href="https://dashboard.pluggy.ai/signup"
						target="_blank"
						rel="noopener noreferrer"
						class="inline-flex"
					>
						<Button variant="outline" class="w-full gap-2">
							Criar conta na Pluggy
							<ExternalLink class="h-3.5 w-3.5" />
						</Button>
					</a>
					<Button class="w-full" onclick={goToCredentialsStep}>
						Já tenho minhas keys
						<ArrowRight class="ml-2 h-4 w-4" />
					</Button>
				</div>
			</CardContent>
		</Card>
	{/if}

	{#if step === "credentials"}
		<Card class="w-full max-w-md border-border shadow-sm">
			<CardHeader class="text-center">
				<CardTitle class="text-xl font-semibold text-card-foreground">Credenciais Pluggy</CardTitle>
				<CardDescription class="text-sm leading-relaxed text-muted-foreground">
					Insira seu <span class="font-medium">Client ID</span> e
					<span class="font-medium">Client Secret</span> da Pluggy para começar a conectar suas contas
					bancárias.
				</CardDescription>
			</CardHeader>
			<CardContent>
				<div class="space-y-4">
					<div class="space-y-2">
						<Label for="clientId" class={DATE_FIELD_LABEL_CLASS}>Client ID</Label>
						<Input
							id="clientId"
							type="text"
							placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
							bind:value={clientId}
							disabled={credentialsLoading}
							autofocus
							class="font-mono text-sm"
						/>
					</div>
					<div class="space-y-2">
						<Label for="clientSecret" class={DATE_FIELD_LABEL_CLASS}>Client Secret</Label>
						<div class="relative">
							<Input
								id="clientSecret"
								type={showClientSecret ? "text" : "password"}
								placeholder="••••••••••••••••••••"
								bind:value={clientSecret}
								disabled={credentialsLoading}
								class="pr-10 font-mono text-sm"
							/>
							<Button
								type="button"
								variant="ghost"
								size="icon-sm"
								class="absolute top-1/2 right-1 -translate-y-1/2 text-muted-foreground hover:text-foreground"
								onclick={() => (showClientSecret = !showClientSecret)}
								aria-label={showClientSecret ? "Ocultar Client Secret" : "Mostrar Client Secret"}
							>
								{#if showClientSecret}
									<EyeOff class="h-4 w-4" />
								{:else}
									<Eye class="h-4 w-4" />
								{/if}
							</Button>
						</div>
					</div>

					{#if credentialsError}
						<div
							class="flex items-start gap-2 rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"
						>
							<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
							<span>{credentialsError}</span>
						</div>
					{/if}

					<Button
						type="button"
						class="w-full"
						disabled={credentialsLoading || invalidCredentials}
						onclick={handleCredentialsContinue}
					>
						{#if credentialsLoading}
							<Loader2 class="mr-2 h-4 w-4 animate-spin" />
							Salvando...
						{:else}
							<ArrowRight class="mr-2 h-4 w-4" />
							Continuar
						{/if}
					</Button>
				</div>
				<p class="mt-4 text-center text-xs text-muted-foreground">
					Suas credenciais são enviadas diretamente ao servidor e
					<span class="font-medium">não são armazenadas no navegador</span>.
				</p>
			</CardContent>
		</Card>
	{/if}

	{#if step === "connect" && connectToken}
		<Card class="w-full max-w-md border-border shadow-sm">
			<CardHeader class="text-center">
				<CardTitle class="text-xl font-semibold text-card-foreground">Conecte seu Banco</CardTitle>
				<CardDescription class="text-sm leading-relaxed text-muted-foreground">
					Abra o Pluggy Connect para selecionar seu banco e autorizar o vínculo da conta.
				</CardDescription>
			</CardHeader>
			<CardContent>
				{#if connectScriptError}
					<div
						class="mb-4 flex items-start gap-2 rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"
					>
						<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
						<span>{connectScriptError}</span>
					</div>
				{/if}

				{#if connectError}
					<div
						class="mb-4 flex items-start gap-2 rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"
					>
						<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
						<span>{connectError}</span>
					</div>
				{/if}

				<div class="mb-4 rounded-lg border border-border bg-muted/30 p-3">
					<p class="text-xs text-muted-foreground">
						Token de conexão gerado com sucesso. Ao clicar abaixo, o widget oficial da Pluggy será aberto:
					</p>
					<p class="mt-1 break-all font-mono text-xs text-foreground">{connectToken}</p>
				</div>

				<Button
					type="button"
					class="w-full"
					disabled={!canOpenConnectWidget}
					onclick={handleOpenConnectWidget}
				>
					{#if connectLoading}
						<Loader2 class="mr-2 h-4 w-4 animate-spin" />
						Salvando conexão...
					{:else if connectScriptLoading}
						<Loader2 class="mr-2 h-4 w-4 animate-spin" />
						Carregando Pluggy Connect...
					{:else}
						Conectar Banco
						<ArrowRight class="ml-2 h-4 w-4" />
					{/if}
				</Button>
			</CardContent>
		</Card>
	{/if}

	{#if step === "success"}
		<Card class="w-full max-w-md border-border shadow-sm">
			<CardHeader class="text-center">
				<div class="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-success/10">
					<CheckCircle2 class="h-7 w-7 text-success" />
				</div>
				<CardTitle class="text-xl font-semibold text-card-foreground">Tudo pronto!</CardTitle>
				<CardDescription class="text-sm leading-relaxed text-muted-foreground">
					Sua conta bancária foi conectada com sucesso. Clique abaixo para sincronizar e ir ao dashboard.
				</CardDescription>
			</CardHeader>
			<CardContent>
				<Button class="w-full" onclick={handleFinish} disabled={finishing}>
					{#if finishing}
						<Loader2 class="mr-2 h-4 w-4 animate-spin" />
						Sincronizando...
					{:else}
						<ArrowRight class="mr-2 h-4 w-4" />
						Sincronizar e ir ao Painel
					{/if}
				</Button>
			</CardContent>
		</Card>
	{/if}

	<p class="mt-8 text-xs tracking-wide text-muted-foreground">
		Tecnologia Pluggy — Dados criptografados e seguros.
	</p>
</div>

