<script lang="ts">
	import { untrack } from "svelte";
	import { AlertCircle, RefreshCw } from "@lucide/svelte";
	import CategoryDonutChart from "$lib/components/dashboard/category-donut-chart.svelte";
	import IncomeExpenseChart from "$lib/components/dashboard/income-expense-chart.svelte";
	import MetricCards from "$lib/components/dashboard/metric-cards.svelte";
	import RecentTransactionsTable from "$lib/components/dashboard/recent-transactions-table.svelte";
	import { Button } from "$lib/components/ui/button";
	import {
		fetchDashboardCashflow,
		fetchDashboardCategories,
		fetchDashboardMetrics,
		fetchTransactions,
		getApiErrorMessage,
		syncData
	} from "$lib/api";
	import { mapCashflowPointsToChart, mapCategoryAggregatesToDonut } from "$lib/dashboard-transformers";
	import { filtersState } from "$lib/state/filters.svelte";
	import type { CashflowChartPoint, CategoryDonutItem, DashboardMetrics, Transaction } from "$lib/types";

	type DashboardSectionState<T> = {
		data: T;
		loading: boolean;
		error: string | null;
	};

	type DashboardFilterSnapshot = {
		accountId: string | null;
		dateFrom: string;
		dateTo: string;
	};

	const EMPTY_METRICS: DashboardMetrics = {
		currentBalance: 0,
		totalIncome: 0,
		totalExpenses: 0,
		currencyCode: "BRL"
	};

	const EMPTY_CATEGORIES = {
		expenses: [] as CategoryDonutItem[],
		income: [] as CategoryDonutItem[]
	};

	let metrics = $state<DashboardSectionState<DashboardMetrics>>({
		data: EMPTY_METRICS,
		loading: true,
		error: null
	});

	let cashflow = $state<DashboardSectionState<CashflowChartPoint[]>>({
		data: [],
		loading: true,
		error: null
	});

	let categories = $state<DashboardSectionState<typeof EMPTY_CATEGORIES>>({
		data: EMPTY_CATEGORIES,
		loading: true,
		error: null
	});

	let recentTransactions = $state<DashboardSectionState<Transaction[]>>({
		data: [],
		loading: true,
		error: null
	});

	let syncing = $state(false);
	let syncError = $state<string | null>(null);

	let metricsRequestId = 0;
	let cashflowRequestId = 0;
	let categoriesRequestId = 0;
	let transactionsRequestId = 0;

	const filtersTrigger = $derived(
		`${filtersState.accountId ?? "all"}|${filtersState.datePreset}|${filtersState.customDateRange.dateFrom}|${filtersState.customDateRange.dateTo}`
	);

	function snapshotFilters(): DashboardFilterSnapshot {
		const next = untrack(() => filtersState.filters);
		return {
			accountId: next.accountId,
			dateFrom: next.dateFrom,
			dateTo: next.dateTo
		};
	}

	async function reloadMetrics(currentFilters: DashboardFilterSnapshot) {
		const requestId = ++metricsRequestId;
		metrics = { ...metrics, loading: true, error: null };

		try {
			const response = await fetchDashboardMetrics(
				currentFilters.accountId,
				currentFilters.dateFrom,
				currentFilters.dateTo
			);
			if (requestId !== metricsRequestId) return;

			metrics = {
				data: response,
				loading: false,
				error: null
			};
		} catch (error) {
			if (requestId !== metricsRequestId) return;
			metrics = {
				...metrics,
				loading: false,
				error: getApiErrorMessage(error, "Falha ao carregar métricas do painel.")
			};
		}
	}

	async function reloadCashflow(currentFilters: DashboardFilterSnapshot) {
		const requestId = ++cashflowRequestId;
		cashflow = { ...cashflow, loading: true, error: null };

		try {
			const response = await fetchDashboardCashflow(
				currentFilters.accountId,
				currentFilters.dateFrom,
				currentFilters.dateTo
			);
			if (requestId !== cashflowRequestId) return;

			cashflow = {
				data: mapCashflowPointsToChart(response.granularity, response.points),
				loading: false,
				error: null
			};
		} catch (error) {
			if (requestId !== cashflowRequestId) return;
			cashflow = {
				...cashflow,
				loading: false,
				error: getApiErrorMessage(error, "Falha ao carregar gráfico de fluxo de caixa.")
			};
		}
	}

	async function reloadCategories(currentFilters: DashboardFilterSnapshot) {
		const requestId = ++categoriesRequestId;
		categories = { ...categories, loading: true, error: null };

		try {
			const response = await fetchDashboardCategories(
				currentFilters.accountId,
				currentFilters.dateFrom,
				currentFilters.dateTo
			);
			if (requestId !== categoriesRequestId) return;

			categories = {
				data: {
					expenses: mapCategoryAggregatesToDonut(response.expenses),
					income: mapCategoryAggregatesToDonut(response.income)
				},
				loading: false,
				error: null
			};
		} catch (error) {
			if (requestId !== categoriesRequestId) return;
			categories = {
				...categories,
				loading: false,
				error: getApiErrorMessage(error, "Falha ao carregar gráfico de categorias.")
			};
		}
	}

	async function reloadTransactions(currentFilters: DashboardFilterSnapshot) {
		const requestId = ++transactionsRequestId;
		recentTransactions = { ...recentTransactions, loading: true, error: null };

		try {
			const response = await fetchTransactions(
				currentFilters.accountId,
				currentFilters.dateFrom,
				currentFilters.dateTo,
				1,
				20,
				"DESC"
			);
			if (requestId !== transactionsRequestId) return;

			recentTransactions = {
				data: response.items,
				loading: false,
				error: null
			};
		} catch (error) {
			if (requestId !== transactionsRequestId) return;
			recentTransactions = {
				...recentTransactions,
				loading: false,
				error: getApiErrorMessage(error, "Falha ao carregar transações recentes.")
			};
		}
	}

	async function reloadCharts(currentFilters: DashboardFilterSnapshot = snapshotFilters()) {
		await Promise.all([reloadCashflow(currentFilters), reloadCategories(currentFilters)]);
	}

	async function sync() {
		syncing = true;
		syncError = null;
		const currentFilters = snapshotFilters();

		try {
			await syncData();
			await Promise.all([
				reloadMetrics(currentFilters),
				reloadCashflow(currentFilters),
				reloadCategories(currentFilters),
				reloadTransactions(currentFilters)
			]);
		} catch (error) {
			syncError = getApiErrorMessage(error, "Falha ao sincronizar painel.");
		} finally {
			syncing = false;
		}
	}

	$effect(() => {
		// Reactive trigger only; data writes happen in untracked async calls to avoid effect loops.
		filtersTrigger;

		untrack(() => {
			const currentFilters = snapshotFilters();
			void reloadMetrics(currentFilters);
			void reloadCashflow(currentFilters);
			void reloadCategories(currentFilters);
			void reloadTransactions(currentFilters);
		});
	});
</script>

<div class="space-y-7">
	<div class="flex items-center justify-between">
		<div>
			<h1 class="text-2xl font-semibold tracking-tight text-foreground">Painel</h1>
			<p class="mt-1 text-sm text-muted-foreground">Visão geral das suas finanças.</p>
		</div>
		<div class="flex items-center gap-2">
			<Button variant="outline" size="sm" onclick={sync} disabled={syncing}>
				<RefreshCw class={`mr-2 h-3.5 w-3.5 ${syncing ? "animate-spin" : ""}`} />
				{syncing ? "Sincronizando..." : "Sincronizar"}
			</Button>
		</div>
	</div>

	{#if syncError}
		<div
			class="flex items-start gap-2 rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive"
			role="alert"
		>
			<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
			<div>
				<p class="font-medium">Falha na sincronização</p>
				<p class="text-destructive/80">{syncError}</p>
			</div>
		</div>
	{/if}

	<MetricCards
		metrics={metrics.data}
		loading={metrics.loading}
		error={metrics.error}
		onRetry={() => void reloadMetrics(snapshotFilters())}
	/>

	<div class="min-h-80">
		<IncomeExpenseChart
			data={cashflow.data}
			currencyCode={metrics.data.currencyCode}
			loading={cashflow.loading}
			error={cashflow.error}
			onRetry={() => void reloadCharts(snapshotFilters())}
		/>
	</div>

	<div class="grid min-h-80 grid-cols-2 gap-6">
		<CategoryDonutChart
			title="Despesas por Categoria"
			data={categories.data.expenses}
			loading={categories.loading}
			error={categories.error}
			onRetry={() => void reloadCharts(snapshotFilters())}
		/>
		<CategoryDonutChart
			title="Receitas por Categoria"
			data={categories.data.income}
			loading={categories.loading}
			error={categories.error}
			onRetry={() => void reloadCharts(snapshotFilters())}
		/>
	</div>

	<RecentTransactionsTable
		transactions={recentTransactions.data}
		accountCurrencyCode={metrics.data.currencyCode}
		loading={recentTransactions.loading}
		error={recentTransactions.error}
		onRetry={() => void reloadTransactions(snapshotFilters())}
	/>
</div>
