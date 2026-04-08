<script lang="ts">
	import { untrack } from "svelte";
	import { browser } from "$app/environment";
	import { goto } from "$app/navigation";
	import { AlertCircle, Building2, Landmark, Link2, Loader2, LogOut, Plus, RefreshCw, Trash2 } from "@lucide/svelte";
	import {
		AlertDialog,
		AlertDialogAction,
		AlertDialogCancel,
		AlertDialogContent,
		AlertDialogDescription,
		AlertDialogFooter,
		AlertDialogHeader,
		AlertDialogTitle,
		AlertDialogTrigger
	} from "$lib/components/ui/alert-dialog";
	import { Badge } from "$lib/components/ui/badge";
	import { Button } from "$lib/components/ui/button";
	import { Separator } from "$lib/components/ui/separator";
	import {
		createConnectToken,
		deleteBankConnection,
		deleteCredentials,
		fetchAccounts,
		fetchBankConnections,
		getApiErrorMessage,
		saveBankConnection,
		syncData
	} from "$lib/api";
	import { formatDateTimeShort } from "$lib/date";
	import { setupState } from "$lib/state/setup.svelte";
	import type { BankConnection } from "$lib/types";

	const PLUGGY_SDK_SRC = "https://cdn.pluggy.ai/pluggy-connect/v1.3.1/pluggy-connect.js";

	let connections = $state<BankConnection[]>([]);
	let loadingConns = $state(true);
	let loadConnectionsRequestId = 0;

	let syncing = $state(false);
	let syncMsg = $state<string | null>(null);

	let loadingConnectToken = $state(false);
	let connectToken = $state<string | null>(null);
	let openRequested = $state(false);
	let connecting = $state(false);
	let connectError = $state<string | null>(null);
	let connectScriptLoading = $state(false);
	let connectScriptReady = $state(false);
	let connectScriptError = $state<string | null>(null);

	let deleting = $state(false);
	let removingConnectionId = $state<number | null>(null);

	async function loadConnections() {
		const requestId = loadConnectionsRequestId + 1;
		loadConnectionsRequestId = requestId;
		loadingConns = true;
		try {
			const data = await fetchBankConnections();
			if (requestId !== loadConnectionsRequestId) return;
			connections = data;
		} catch {
			if (requestId !== loadConnectionsRequestId) return;
			connections = [];
		} finally {
			if (requestId === loadConnectionsRequestId) {
				loadingConns = false;
			}
		}
	}

	$effect(() => {
		untrack(() => {
			void loadConnections();
		});
	});

	$effect(() => {
		if (!browser || !connectToken) return;
		if (untrack(() => connectScriptReady || connectScriptLoading)) return;

		if (typeof window.PluggyConnect === "function") {
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
			script.dataset.pluggyLoaded = "false";
			document.head.appendChild(script);
		} else if (script.dataset.pluggyLoaded === "true") {
			connectScriptLoading = false;
			connectScriptReady = typeof window.PluggyConnect === "function";
			if (!connectScriptReady) {
				connectScriptError =
					"O SDK da Pluggy já foi carregado, mas não está disponível. Recarregue a página para tentar novamente.";
			}
			return;
		} else if (typeof window.PluggyConnect === "function") {
			connectScriptLoading = false;
			connectScriptReady = true;
			return;
		}

		let settled = false;

		const completeWithSuccess = () => {
			if (settled) return;
			settled = true;
			window.clearTimeout(timeoutId);
			window.clearInterval(pollId);
			connectScriptLoading = false;
			connectScriptReady = true;
			connectScriptError = null;
			script.dataset.pluggyLoaded = "true";
		};

		const completeWithFailure = (message: string) => {
			if (settled) return;
			settled = true;
			window.clearTimeout(timeoutId);
			window.clearInterval(pollId);
			connectScriptLoading = false;
			connectScriptReady = false;
			connectScriptError = message;
		};

		const timeoutId = window.setTimeout(() => {
			if (typeof window.PluggyConnect === "function") {
				completeWithSuccess();
				return;
			}
			completeWithFailure(
				"Tempo esgotado ao carregar o SDK da Pluggy. Verifique adblock/firewall e tente novamente."
			);
		}, 15000);

		const pollId = window.setInterval(() => {
			if (typeof window.PluggyConnect === "function") {
				completeWithSuccess();
			}
		}, 250);

		const handleLoad = () => {
			if (typeof window.PluggyConnect === "function") {
				completeWithSuccess();
				return;
			}
			completeWithFailure(
				"O SDK da Pluggy foi carregado, mas não inicializou corretamente. Tente recarregar a página."
			);
		};

		const handleError = () => {
			completeWithFailure(
				"Não foi possível carregar o SDK da Pluggy. Verifique sua conexão e tente novamente."
			);
		};

		script.addEventListener("load", handleLoad);
		script.addEventListener("error", handleError);

		return () => {
			settled = true;
			window.clearTimeout(timeoutId);
			window.clearInterval(pollId);
			script.removeEventListener("load", handleLoad);
			script.removeEventListener("error", handleError);
		};
	});

	function getPluggyConstructor(): PluggyConnectGlobalFunction | null {
		if (!browser) return null;
		return typeof window.PluggyConnect === "function" ? window.PluggyConnect : null;
	}

	function openPluggyConnect(options: PluggyConnectOptions) {
		const PluggyConnectCtor = getPluggyConstructor();
		if (!PluggyConnectCtor) {
			throw new Error("SDK da Pluggy não está disponível.");
		}

		const widget = new PluggyConnectCtor(options);
		if (!widget || typeof widget.init !== "function") {
			throw new Error("SDK da Pluggy carregado, mas a API init() não está disponível.");
		}

		widget.init();
	}

	async function handleSync() {
		syncing = true;
		syncMsg = null;
		try {
			await syncData();
			syncMsg = "Sincronização concluída com sucesso.";
		} catch {
			syncMsg = "Erro ao sincronizar.";
		} finally {
			syncing = false;
		}
	}

	async function handleStartAddConnection() {
		connectError = null;
		openRequested = true;
		loadingConnectToken = true;

		try {
			const response = await createConnectToken();
			connectToken = response.connectToken;

			if (connectScriptReady) {
				openRequested = false;
				handleOpenConnectWidget();
			}
		} catch (error) {
			openRequested = false;
			connectError = getApiErrorMessage(
				error,
				"Não foi possível iniciar uma nova conexão agora."
			);
		} finally {
			loadingConnectToken = false;
		}
	}

	function resetConnectionFlow() {
		connectToken = null;
		openRequested = false;
		connectError = null;
	}

	$effect(() => {
		if (!openRequested) return;
		if (!connectToken || !connectScriptReady || connecting) return;

		openRequested = false;
		handleOpenConnectWidget();
	});

	async function handlePluggySuccess(payload: PluggyConnectSuccessPayload) {
		connecting = true;
		connectError = null;

		const itemId = payload.item?.id?.trim();
		if (!itemId) {
			connectError = "A Pluggy não retornou um itemId válido para salvar a conexão.";
			connecting = false;
			return;
		}

		const bankName =
			payload.item.connector?.name?.trim() ||
			payload.item.institution?.name?.trim() ||
			"Banco não identificado";

		try {
			await saveBankConnection({ itemId, bankName });

			await syncData().catch(() => undefined);
			const [nextAccounts] = await Promise.all([fetchAccounts(), loadConnections()]);
			setupState.completeSetup(nextAccounts);
			syncMsg = "Nova conexão adicionada com sucesso.";
			resetConnectionFlow();
		} catch (error) {
			connectError = getApiErrorMessage(
				error,
				"A conexão foi autorizada, mas falhou ao salvar no backend."
			);
		} finally {
			connecting = false;
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

	async function handleRemoveConnection(id: number) {
		removingConnectionId = id;
		try {
			await deleteBankConnection(id);
			connections = connections.filter((connection) => connection.id !== id);
		} finally {
			removingConnectionId = null;
		}
	}

	async function handleDeleteAll() {
		deleting = true;
		try {
			await deleteCredentials();
			setupState.resetSetup();
			await goto("/setup", { replaceState: true });
		} catch {
			deleting = false;
		}
	}

	function connectionStatusLabel(status: BankConnection["status"]): string {
		if (status === "UPDATED") return "Atualizado";
		if (status === "PENDING") return "Pendente";
		return "Erro";
	}
</script>

<div class="space-y-7">
	<div>
		<h1 class="text-2xl font-semibold tracking-tight text-foreground">Configurações</h1>
		<p class="mt-1 text-sm text-muted-foreground">Gerencie suas conexões bancárias e preferências.</p>
	</div>

	<div class="rounded-xl border border-border bg-card shadow-sm">
		<div class="flex items-center justify-between px-6 py-5">
			<div>
				<p class="text-xs font-medium tracking-wide text-muted-foreground">Sincronização Manual</p>
				<p class="mt-1 text-sm text-muted-foreground">
					Atualiza contas e transações de todas as conexões.
				</p>
			</div>
			<Button variant="outline" size="sm" onclick={handleSync} disabled={syncing}>
				<RefreshCw class={`mr-2 h-3.5 w-3.5 ${syncing ? "animate-spin" : ""}`} />
				{syncing ? "Sincronizando..." : "Sincronizar agora"}
			</Button>
		</div>
		{#if syncMsg}
			<Separator />
			<div class="flex items-center gap-2 px-6 py-3.5">
				<AlertCircle class="h-3.5 w-3.5 text-muted-foreground" />
				<p class="text-sm text-muted-foreground">{syncMsg}</p>
			</div>
		{/if}
	</div>

	<div class="rounded-xl border border-border bg-card shadow-sm">
		<div class="flex items-center justify-between px-6 pt-6 pb-4">
			<div class="flex items-center gap-2">
				<Landmark class="h-4 w-4 text-muted-foreground" />
				<p class="text-xs font-medium tracking-wide text-muted-foreground">Contas Bancárias</p>
			</div>
			<Badge variant="secondary" class="font-normal">
				{setupState.accounts.length}
				{setupState.accounts.length === 1 ? "conta" : "contas"}
			</Badge>
		</div>
		<Separator />
		<div class="divide-y divide-border">
			{#if setupState.accounts.length === 0}
				<p class="px-6 py-8 text-center text-sm text-muted-foreground">
					Nenhuma conta encontrada. Sincronize para carregar.
				</p>
			{/if}
			{#each setupState.accounts as account (account.id)}
				<div class="flex items-center justify-between px-6 py-4 transition-colors hover:bg-muted/20">
					<div class="flex items-center gap-3">
						<div
							class="flex h-9 w-9 items-center justify-center rounded-lg"
							style={`background-color: ${account.bank?.primaryColor ? `${account.bank.primaryColor}18` : "transparent"}`}
						>
							<Building2
								class="h-4 w-4"
								style={`color: ${account.bank?.primaryColor ?? "currentColor"}`}
							/>
						</div>
						<div>
							<p class="text-sm font-medium text-card-foreground">{account.name}</p>
							<p class="mt-0.5 text-xs text-muted-foreground">
								{account.bank?.name}
								{#if account.number}
									· •••• {account.number}
								{/if}
							</p>
						</div>
					</div>
					<Badge variant="outline" class="font-normal capitalize">
						{account.type.toLowerCase().replace("_", " ")}
					</Badge>
				</div>
			{/each}
		</div>
	</div>

	<div class="rounded-xl border border-border bg-card shadow-sm">
		<div class="flex items-center justify-between px-6 pt-6 pb-4">
			<div class="flex items-center gap-2">
				<Link2 class="h-4 w-4 text-muted-foreground" />
				<p class="text-xs font-medium tracking-wide text-muted-foreground">Conexões Pluggy</p>
			</div>
			<div class="flex items-center gap-2">
				{#if loadingConns}
					<Loader2 class="h-4 w-4 animate-spin text-muted-foreground" />
				{/if}
				<Button
					variant="outline"
					size="sm"
					onclick={handleStartAddConnection}
					disabled={loadingConnectToken || connectScriptLoading || openRequested || connecting}
				>
					{#if loadingConnectToken || connectScriptLoading || openRequested}
						<Loader2 class="mr-2 h-3.5 w-3.5 animate-spin" />
					{:else}
						<Plus class="mr-2 h-3.5 w-3.5" />
					{/if}
					{loadingConnectToken || connectScriptLoading || openRequested
						? "Carregando Pluggy..."
						: connecting
							? "Finalizando conexão..."
							: "Adicionar conexão"}
				</Button>
			</div>
		</div>
		<Separator />
		{#if connectScriptError || connectError}
			<div class="border-b border-border px-6 py-4">
				<div class="flex items-start gap-2 rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">
					<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
					<span>{connectScriptError ?? connectError}</span>
				</div>
				<div class="mt-3 flex items-center gap-2">
					<Button variant="outline" size="sm" onclick={handleOpenConnectWidget} disabled={!connectToken || connecting}>
						Tentar novamente
					</Button>
					<Button variant="ghost" size="sm" onclick={resetConnectionFlow} disabled={connecting}>
						Fechar
					</Button>
				</div>
			</div>
		{/if}

		<div class="divide-y divide-border">
			{#if !loadingConns && connections.length === 0}
				<p class="px-6 py-8 text-center text-sm text-muted-foreground">Nenhuma conexão encontrada.</p>
			{/if}
			{#each connections as conn (conn.id)}
				<div class="flex items-center justify-between px-6 py-4 transition-colors hover:bg-muted/20">
					<div>
						<p class="text-sm font-medium text-card-foreground">{conn.bankName}</p>
						<p class="mt-0.5 text-xs text-muted-foreground">
							Última sync: {formatDateTimeShort(conn.lastSyncAt)}
						</p>
					</div>
					<div class="flex items-center gap-2">
						<Badge
							variant={conn.status === "ERROR" ? "destructive" : "secondary"}
							class="font-normal"
						>
							{connectionStatusLabel(conn.status)}
						</Badge>
						<AlertDialog>
							<AlertDialogTrigger>
								{#snippet child({ props })}
									<Button
										variant="ghost"
										size="icon"
										class="h-8 w-8 text-muted-foreground hover:text-destructive"
										disabled={removingConnectionId === conn.id}
										{...props}
									>
										{#if removingConnectionId === conn.id}
											<Loader2 class="h-3.5 w-3.5 animate-spin" />
										{:else}
											<Trash2 class="h-3.5 w-3.5" />
										{/if}
									</Button>
								{/snippet}
							</AlertDialogTrigger>
							<AlertDialogContent>
								<AlertDialogHeader>
									<AlertDialogTitle>Remover conexão bancária?</AlertDialogTitle>
									<AlertDialogDescription>
										Essa ação remove a conexão com
										<span class="font-medium text-foreground">{conn.bankName}</span>.
										Você pode adicionar novamente depois, se necessário.
									</AlertDialogDescription>
								</AlertDialogHeader>
								<AlertDialogFooter>
									<AlertDialogCancel class="border bg-background hover:bg-accent hover:text-accent-foreground"
										>Cancelar</AlertDialogCancel
									>
									<AlertDialogAction
										class="bg-destructive text-white hover:bg-destructive/90"
										onclick={() => handleRemoveConnection(conn.id)}
										disabled={removingConnectionId === conn.id}
									>
										{removingConnectionId === conn.id ? "Removendo..." : "Sim, remover"}
									</AlertDialogAction>
								</AlertDialogFooter>
							</AlertDialogContent>
						</AlertDialog>
					</div>
				</div>
			{/each}
		</div>
	</div>

	<div class="rounded-xl border border-destructive/30 bg-destructive/5 shadow-sm">
		<div class="px-6 pt-6 pb-4">
			<p class="text-xs font-medium tracking-wide text-destructive">Zona de Perigo</p>
			<p class="mt-1 text-sm text-muted-foreground">
				Remove
				<span class="font-medium">todas</span>
				as credenciais, conexões, contas e transações do servidor. Você será redirecionado ao wizard de
				configuração.
			</p>
		</div>
		<Separator />
		<div class="px-6 py-4">
			<AlertDialog>
				<AlertDialogTrigger>
					{#snippet child({ props })}
						<Button variant="destructive" size="sm" disabled={deleting} {...props}>
							{#if deleting}
								<Loader2 class="mr-2 h-4 w-4 animate-spin" />
							{:else}
								<LogOut class="mr-2 h-4 w-4" />
							{/if}
							{deleting ? "Removendo..." : "Apagar tudo e Reconfigurar"}
						</Button>
					{/snippet}
				</AlertDialogTrigger>
				<AlertDialogContent>
					<AlertDialogHeader>
						<AlertDialogTitle>Apagar tudo e reconfigurar?</AlertDialogTitle>
						<AlertDialogDescription>
							Essa ação removerá credenciais, conexões, contas e transações do servidor.
							Você será redirecionado para a página de setup.
						</AlertDialogDescription>
					</AlertDialogHeader>
					<AlertDialogFooter>
						<AlertDialogCancel class="border bg-background hover:bg-accent hover:text-accent-foreground"
							>Cancelar</AlertDialogCancel
						>
						<AlertDialogAction
							class="bg-destructive text-white hover:bg-destructive/90"
							onclick={handleDeleteAll}
							disabled={deleting}
						>
							{deleting ? "Removendo..." : "Sim, apagar tudo"}
						</AlertDialogAction>
					</AlertDialogFooter>
				</AlertDialogContent>
			</AlertDialog>
		</div>
	</div>
</div>

