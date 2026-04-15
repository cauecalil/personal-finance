<script lang="ts">
	import "../app.css";
	import { browser, building } from "$app/environment";
	import { goto } from "$app/navigation";
	import favicon from "$lib/assets/favicon.svg";
	import { Loader2 } from "@lucide/svelte";
	import { page } from "$app/state";
	import { ModeWatcher } from "mode-watcher";
	import { onMount } from "svelte";
	import { sendHeartbeat } from "$lib/api";
	import AppHeader from "$lib/components/layout/app-header.svelte";
	import AppSidebar from "$lib/components/layout/app-sidebar.svelte";
	import { TooltipProvider } from "$lib/components/ui/tooltip";
	import type { DatePreset } from "$lib/filter-context-value";
	import { filtersState } from "$lib/state/filters.svelte";
	import { setupState } from "$lib/state/setup.svelte";

	const HEARTBEAT_INTERVAL_MS = 5000;
	let { children } = $props();

	const headerAccounts = $derived(
		setupState.accounts.map((account) => ({
			id: account.id,
			name: account.name,
			institutionName: account.bank?.name ?? ""
		}))
	);

	const selectedAccountLabel = $derived(
		filtersState.accountId === null
			? "Todas as Contas"
			: (headerAccounts.find((account) => account.id === filtersState.accountId)?.name ??
				"Todas as Contas")
	);

	const isSetupRoute = $derived(page.url.pathname === "/setup" || page.url.pathname.startsWith("/setup/"));

	function handleDatePresetChange(preset: Exclude<DatePreset, "custom">) {
		filtersState.setDatePreset(preset);
	}

	$effect(() => {
		void setupState.bootstrap();
	});

	$effect(() => {
		if (setupState.checking) return;
		if (setupState.isSetupComplete) return;
		if (isSetupRoute) return;

		void goto("/setup", { replaceState: true });
	});

	onMount(() => {
		if (!browser || building) return;

		const pulse = () => {
			void sendHeartbeat().catch(() => {});
		};

		pulse();
		const timerId = window.setInterval(pulse, HEARTBEAT_INTERVAL_MS);

		return () => {
			window.clearInterval(timerId);
		};
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<ModeWatcher defaultMode="system" />

<TooltipProvider delayDuration={300}>
	{#if setupState.checking}
		<div class="flex h-screen items-center justify-center bg-background">
			<Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
		</div>
	{:else if isSetupRoute}
		{@render children()}
	{:else}
		<div class="flex h-screen overflow-hidden bg-background">
			<AppSidebar />

			<div class="flex flex-1 flex-col overflow-hidden">
				{#key `${filtersState.filters.dateFrom}:${filtersState.filters.dateTo}`}
					<AppHeader
						selectedAccountLabel={selectedAccountLabel}
						selectedDateLabel={filtersState.datePresetLabel}
						accounts={headerAccounts}
						onAccountChange={filtersState.setAccountId}
						onDatePresetChange={handleDatePresetChange}
						customDateFrom={filtersState.filters.dateFrom}
						customDateTo={filtersState.filters.dateTo}
						onCustomDateRangeChange={filtersState.setCustomDateRange}
					/>
				{/key}

				<main class="flex-1 overflow-y-auto p-8">
					{@render children()}
				</main>
			</div>
		</div>
	{/if}
</TooltipProvider>
