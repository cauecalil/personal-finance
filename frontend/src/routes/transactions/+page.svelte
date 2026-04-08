<script lang="ts">
	import { untrack } from "svelte";
	import { AlertCircle, Loader2 } from "@lucide/svelte";
	import TransactionsPagination from "$lib/components/transactions/transactions-pagination.svelte";
	import TransactionsTable from "$lib/components/transactions/transactions-table.svelte";
	import { fetchTransactions, getApiErrorMessage } from "$lib/api";
	import { filtersState } from "$lib/state/filters.svelte";
	import { setupState } from "$lib/state/setup.svelte";
	import type { Transaction } from "$lib/types";

	const PAGE_SIZE = 15;

	let page = $state(1);
	let transactions = $state<Transaction[]>([]);
	let totalItems = $state(0);
	let totalPages = $state(1);
	let hasNextPage = $state(false);
	let hasPrevPage = $state(false);
	let loading = $state(true);
	let error = $state<string | null>(null);
	let requestId = 0;
	let lastFiltersKey: string | null = null;

	const filters = $derived(filtersState.filters);
	const filtersKey = $derived(`${filters.accountId ?? "all"}|${filters.dateFrom}|${filters.dateTo}`);
	const queryKey = $derived(`${filtersKey}|${page}|${PAGE_SIZE}`);

	const selectedAccount = $derived(
		filters.accountId ? setupState.accounts.find((account) => account.id === filters.accountId) ?? null : null
	);
	const currencyCode = $derived(selectedAccount?.currency ?? setupState.accounts[0]?.currency ?? "BRL");

	const safePage = $derived(Math.min(page, Math.max(1, totalPages)));
	const canGoPrev = $derived(hasPrevPage || safePage > 1);
	const canGoNext = $derived(hasNextPage || safePage < totalPages);

	function goPrev() {
		if (!canGoPrev) return;
		page = Math.max(1, page - 1);
	}

	function goNext() {
		if (!canGoNext) return;
		page += 1;
	}

	$effect(() => {
		const currentFiltersKey = filtersKey;
		if (lastFiltersKey === null) {
			lastFiltersKey = currentFiltersKey;
			return;
		}

		if (lastFiltersKey !== currentFiltersKey) {
			lastFiltersKey = currentFiltersKey;
			if (page !== 1) {
				page = 1;
			}
		}
	});

	$effect(() => {
		queryKey;

		const currentRequest = requestId + 1;
		requestId = currentRequest;
		loading = true;
		error = null;
		const currentFilters = untrack(() => filtersState.filters);

		void (async () => {
			try {
				const response = await fetchTransactions(
					currentFilters.accountId,
					currentFilters.dateFrom,
					currentFilters.dateTo,
					page,
					PAGE_SIZE,
					"DESC"
				);
				if (currentRequest !== requestId) return;

				transactions = response.items;
				totalItems = response.totalItems;
				totalPages = Math.max(1, response.totalPages);
				hasNextPage = response.hasNextPage;
				hasPrevPage = response.hasPrevPage;

				if (page > totalPages) {
					page = totalPages;
					return;
				}

				loading = false;
			} catch (cause) {
				if (currentRequest !== requestId) return;
				error = getApiErrorMessage(cause, "Erro ao carregar transações.");
				loading = false;
			}
		})();
	});
</script>

<div class="space-y-8">
	<div>
		<h1 class="text-2xl font-semibold tracking-tight text-foreground">Transações</h1>
		<p class="text-sm text-muted-foreground">Histórico completo de movimentações.</p>
	</div>

	{#if error}
		<div
			class="flex items-start gap-2 rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive"
			role="alert"
		>
			<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
			<div>
				<p class="font-medium">Erro ao carregar transações</p>
				<p class="text-destructive/80">{error}</p>
			</div>
		</div>
	{/if}

	{#if loading && !error}
		<div class="flex h-64 items-center justify-center" aria-busy="true" aria-live="polite">
			<Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
		</div>
	{/if}

	{#if !loading && !error}
		<div class="rounded-xl border border-border bg-card shadow-sm">
			<TransactionsTable {transactions} {currencyCode} />
			<TransactionsPagination
				page={safePage}
				{totalPages}
				{totalItems}
				pageSize={PAGE_SIZE}
				onPrev={goPrev}
				onNext={goNext}
			/>
		</div>
	{/if}
</div>


