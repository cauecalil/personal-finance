<script lang="ts">
	import { AlertCircle, TrendingDown, TrendingUp, Wallet } from "@lucide/svelte";
	import { Button } from "$lib/components/ui/button";
	import type { DashboardMetrics } from "$lib/types";
	import { formatCurrency } from "$lib/utils";

	interface MetricCardsProps {
		metrics: DashboardMetrics;
		loading?: boolean;
		error?: string | null;
		onRetry?: () => void;
	}

	interface CardDef {
		title: string;
		icon: typeof Wallet;
		getValue: (m: DashboardMetrics) => number;
		colorClass: string;
		iconBgClass: string;
	}

	const cards: CardDef[] = [
		{
			title: "Saldo Atual",
			icon: Wallet,
			getValue: (m) => m.currentBalance,
			colorClass: "text-foreground",
			iconBgClass: "bg-primary/10 text-primary"
		},
		{
			title: "Receitas no Período",
			icon: TrendingUp,
			getValue: (m) => m.totalIncome,
			colorClass: "text-success",
			iconBgClass: "bg-success/10 text-success"
		},
		{
			title: "Despesas no Período",
			icon: TrendingDown,
			getValue: (m) => m.totalExpenses,
			colorClass: "text-destructive",
			iconBgClass: "bg-destructive/10 text-destructive"
		}
	];

	let { metrics, loading = false, error = null, onRetry }: MetricCardsProps = $props();
</script>

{#if loading}
	<div class="grid grid-cols-3 gap-5">
		{#each [0, 1, 2] as item (item)}
			<div class="rounded-xl border border-border bg-card p-6 shadow-sm">
				<div class="h-3 w-28 animate-pulse rounded bg-muted"></div>
				<div class="mt-4 h-8 w-32 animate-pulse rounded bg-muted"></div>
			</div>
		{/each}
	</div>
{:else if error}
	<div class="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">
		<div class="flex items-start justify-between gap-3">
			<div class="flex items-start gap-2">
				<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
				<div>
					<p class="font-medium">Falha ao carregar cartões</p>
					<p class="text-destructive/80">{error}</p>
				</div>
			</div>
			{#if onRetry}
				<Button variant="outline" size="sm" onclick={onRetry}>Tentar novamente</Button>
			{/if}
		</div>
	</div>
{:else}
	<div class="grid grid-cols-3 gap-5">
		{#each cards as card (card.title)}
			{@const Icon = card.icon}
			{@const value = card.getValue(metrics)}
			<div
				class="rounded-xl border border-border bg-card p-6 shadow-sm transition-colors hover:bg-muted/20"
			>
				<div class="flex items-center justify-between">
					<p class="text-xs font-medium tracking-wide text-muted-foreground">{card.title}</p>
					<div class={`flex h-9 w-9 items-center justify-center rounded-lg ${card.iconBgClass}`}>
						<Icon class="h-4 w-4" />
					</div>
				</div>
				<p class={`mt-3 text-2xl font-semibold tracking-tight tabular-nums ${card.colorClass}`}>
					{formatCurrency(value, metrics.currencyCode)}
				</p>
			</div>
		{/each}
	</div>
{/if}

