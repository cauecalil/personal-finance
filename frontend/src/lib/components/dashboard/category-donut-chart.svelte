<script lang="ts">
	import { AlertCircle } from "@lucide/svelte";
	import { PieChart } from "layerchart";
	import { Button } from "$lib/components/ui/button";
	import { type ChartConfig, ChartContainer } from "$lib/components/ui/chart";
	import { ScrollArea } from "$lib/components/ui/scroll-area";
	import type { CategoryDonutItem } from "$lib/types";
	import { formatCurrency } from "$lib/utils";

	interface CategoryDonutChartProps {
		title: string;
		data: CategoryDonutItem[];
		loading?: boolean;
		error?: string | null;
		onRetry?: () => void;
	}

	let { title, data, loading = false, error = null, onRetry }: CategoryDonutChartProps = $props();

	const legendHeightClass = "h-52";
	const legendMinHeightClass = "min-h-52";

	const chartData = $derived(
		(Array.isArray(data) ? data : [])
			.map((item, index) => ({
				id: `${index}-${item.category}`.replace(/[^a-zA-Z0-9_-]/g, "_"),
				label: item.category,
				// D3 pie requires finite, non-negative numeric values.
				value: Math.abs(Number.isFinite(Number(item.total)) ? Number(item.total) : 0),
				fill: item.color
			}))
			.filter((item) => typeof item.label === "string" && item.label.trim().length > 0)
	);

	const chartSlices = $derived(chartData.filter((item) => item.value > 0));

	const hasChartData = $derived(chartSlices.length > 0);
	const hasCategoryItems = $derived(data.length > 0);

	const chartConfig: ChartConfig = {
		value: {
			label: "Valor"
		}
	};

	// Recharts used paddingAngle in degrees (3). LayerChart/D3 expects radians.
	const donutPadAngle = (3 * Math.PI) / 180;
</script>

{#if loading}
	<div class="flex h-full w-full flex-col rounded-xl border border-border bg-card p-6 shadow-sm">
		<div class="h-3 w-40 animate-pulse rounded bg-muted"></div>
		<div class="mt-6 h-52 w-full animate-pulse rounded-lg bg-muted"></div>
	</div>
{:else if error}
	<div class="flex h-full w-full flex-col rounded-xl border border-destructive/30 bg-destructive/5 p-6 shadow-sm">
		<p class="mb-4 text-xs font-medium tracking-wide text-destructive/80">{title}</p>
		<div class="flex flex-1 items-center justify-between gap-3 text-sm text-destructive">
			<div class="flex items-start gap-2">
				<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
				<div>
					<p class="font-medium">Falha ao carregar categorias</p>
					<p class="text-destructive/80">{error}</p>
				</div>
			</div>
			{#if onRetry}
				<Button variant="outline" size="sm" onclick={onRetry}>Tentar novamente</Button>
			{/if}
		</div>
	</div>
{:else}
	<div class="flex h-full w-full flex-col rounded-xl border border-border bg-card p-6 shadow-sm">
		<p class="mb-4 text-xs font-medium tracking-wide text-muted-foreground">{title}</p>

		{#if !hasCategoryItems}
			<div class="flex flex-1 items-center justify-center">
				<p class="text-sm text-muted-foreground">Sem dados de categoria para o período selecionado.</p>
			</div>
		{:else}
			<div class="flex flex-1 items-center justify-center gap-8">
				<div class="h-52 w-52 shrink-0">
					{#if hasChartData}
						<ChartContainer config={chartConfig} class="h-full w-full">
							<PieChart
								data={chartSlices}
								key="id"
								label="label"
								value="value"
								c="fill"
								innerRadius={55}
								outerRadius={80}
								padAngle={donutPadAngle}
								tooltip
								legend={false}
								props={{
									tooltip: {
										item: {
											format: (value) => formatCurrency(Number(value), "BRL")
										}
									}
								}}
							/>
						</ChartContainer>
					{:else}
						<div class="flex h-full items-center justify-center rounded-lg border border-border bg-muted/20">
							<p class="text-sm text-muted-foreground">Sem dados no período selecionado.</p>
						</div>
					{/if}
				</div>

				<ScrollArea class={legendHeightClass}>
					<div class={`flex ${legendMinHeightClass} flex-col justify-center gap-2.5 pr-3`}>
						{#each data as item (item.category)}
							<div class="flex items-center gap-3 text-sm">
								<span
									class="inline-block h-2.5 w-2.5 shrink-0 rounded-full"
									style={`background-color: ${item.color}`}
								></span>
								<span class="whitespace-nowrap text-muted-foreground">
									{item.category}
								</span>
								<span class="whitespace-nowrap font-medium tabular-nums text-card-foreground">
									{formatCurrency(item.total)}
								</span>
								<span class="text-xs tabular-nums text-muted-foreground">
									{item.percentage}%
								</span>
							</div>
						{/each}
					</div>
				</ScrollArea>
			</div>
		{/if}
	</div>
{/if}

