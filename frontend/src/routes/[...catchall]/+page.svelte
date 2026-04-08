<script lang="ts">
	import { browser } from "$app/environment";
	import { goto } from "$app/navigation";
	import { ArrowLeft, Compass } from "@lucide/svelte";
	import { Button } from "$lib/components/ui/button";

	function goBack() {
		if (browser && window.history.length > 1) {
			window.history.back();
			return;
		}
		void goto("/", { replaceState: true });
	}

	function goDashboard() {
		void goto("/", { replaceState: true });
	}
</script>

<div class="flex min-h-screen flex-col items-center justify-center bg-background px-4">
	<div class="relative mb-8">
		<div class="flex h-28 w-28 items-center justify-center rounded-full bg-muted">
			<Compass class="h-12 w-12 text-muted-foreground/60" />
		</div>
		<span
			class="absolute -right-2 -top-2 flex h-10 w-10 items-center justify-center rounded-full bg-primary text-sm font-bold text-primary-foreground shadow-lg"
		>
			404
		</span>
	</div>

	<h1 class="text-2xl font-semibold tracking-tight text-foreground">Página não encontrada</h1>
	<p class="mt-2 max-w-sm text-center text-sm text-muted-foreground">
		O endereço que você tentou acessar não existe ou foi movido. Verifique a URL ou volte ao início.
	</p>

	<div class="mt-8 flex items-center gap-3">
		<Button variant="outline" onclick={goBack}>
			<ArrowLeft class="mr-2 h-4 w-4" />
			Voltar
		</Button>
		<Button onclick={goDashboard}>Ir ao Painel</Button>
	</div>

	<p class="mt-12 text-xs text-muted-foreground/50">Se o problema persistir, entre em contato com o suporte.</p>
</div>

