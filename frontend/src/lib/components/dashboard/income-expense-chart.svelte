<script lang="ts">
	import { AlertCircle } from "@lucide/svelte";
	import { BarChart } from "layerchart";
	import { Button } from "$lib/components/ui/button";
	import { type ChartConfig, ChartContainer } from "$lib/components/ui/chart";
	import type { CashflowChartPoint } from "$lib/types";

	interface IncomeExpenseChartProps {
		data: CashflowChartPoint[];
		currencyCode?: string;
		loading?: boolean;
		error?: string | null;
		onRetry?: () => void;
	}

	const chartConfig: ChartConfig = {
		income: {
			label: "Receitas",
			color: "var(--color-success)"
		},
		expenses: {
			label: "Despesas",
			color: "var(--color-destructive)"
		}
	};

	let { data, currencyCode = "BRL", loading = false, error = null, onRetry }: IncomeExpenseChartProps =
		$props();

	function formatCompact(value: number, code: string): string {
		if (value === 0) return "R$ 0";
		return new Intl.NumberFormat("pt-BR", {
			style: "currency",
			currency: code,
			notation: "compact",
			maximumFractionDigits: 1
		}).format(value);
	}

	function formatFull(value: number, code: string): string {
		return new Intl.NumberFormat("pt-BR", {
			style: "currency",
			currency: code
		}).format(value);
	}

	const chartSeries = $derived([
		{
			key: "income",
			label: "Receitas",
			value: "income",
			color: "var(--color-success)"
		},
		{
			key: "expenses",
			label: "Despesas",
			value: "expenses",
			color: "var(--color-destructive)"
		}
	]);

	const safeChartData = $derived(
		Array.isArray(data)
			? data
					.map((point) => ({
						label: point.label,
						income: Number.isFinite(point.income) ? point.income : 0,
						expenses: Number.isFinite(point.expenses) ? point.expenses : 0
					}))
					.filter((point) => typeof point.label === "string" && point.label.trim().length > 0)
			: []
	);

	const hasChartData = $derived(safeChartData.length > 0);
</script>

{#if loading}
	<div class="h-full w-full rounded-xl border border-border bg-card p-6 shadow-sm">
		<div class="h-3 w-36 animate-pulse rounded bg-muted"></div>
		<div class="mt-6 h-56 w-full animate-pulse rounded-lg bg-muted"></div>
	</div>
{:else if error}
	<div class="flex h-full w-full flex-col rounded-xl border border-destructive/30 bg-destructive/5 p-6 shadow-sm">
		<p class="mb-4 text-xs font-medium tracking-wide text-destructive/80">Receitas vs Despesas</p>
		<div class="flex flex-1 items-center justify-between gap-3 text-sm text-destructive">
			<div class="flex items-start gap-2">
				<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
				<div>
					<p class="font-medium">Falha ao carregar gráfico</p>
					<p class="text-destructive/80">{error}</p>
				</div>
			</div>
			{#if onRetry}
				<Button variant="outline" size="sm" onclick={onRetry}>Tentar novamente</Button>
			{/if}
		</div>
	</div>
{:else}
	<div class="h-full w-full rounded-xl border border-border bg-card p-6 shadow-sm">
		<p class="mb-4 text-xs font-medium tracking-wide text-muted-foreground">Receitas vs Despesas</p>
		{#if hasChartData}
			<div class="h-[240px] w-full overflow-hidden pl-10 pt-2">
				<ChartContainer config={chartConfig} class="h-[232px] w-full">
					<BarChart
						data={safeChartData}
						x="label"
						series={chartSeries}
						seriesLayout="group"
						tooltip
						legend
						props={{
							xAxis: {
								tickMarks: false
							},
							yAxis: {
								format: (value) => formatCompact(Number(value), currencyCode),
								tickMarks: false
							},
							grid: {
								class: "stroke-border/70"
							},
							tooltip: {
								item: {
									format: (value) => formatFull(Number(value), currencyCode)
								}
							}
						}}
					/>
				</ChartContainer>
			</div>
		{:else}
			<div class="flex h-60 items-center justify-center rounded-lg border border-border bg-muted/20">
				<p class="text-sm text-muted-foreground">Sem dados no período selecionado.</p>
			</div>
		{/if}
	</div>
{/if}

