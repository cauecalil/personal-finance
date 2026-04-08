<script lang="ts">
	import { AlertCircle } from "@lucide/svelte";
	import { Badge } from "$lib/components/ui/badge";
	import { Button } from "$lib/components/ui/button";
	import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "$lib/components/ui/table";
	import { formatDateTimeShort } from "$lib/date";
	import { getUiAmountColorClass, getUiAmountSign } from "$lib/transaction-flow";
	import type { Transaction } from "$lib/types";
	import { cn, formatCurrency } from "$lib/utils";

	interface RecentTransactionsTableProps {
		transactions: Transaction[];
		accountCurrencyCode: string;
		loading?: boolean;
		error?: string | null;
		onRetry?: () => void;
	}

	let {
		transactions,
		accountCurrencyCode,
		loading = false,
		error = null,
		onRetry
	}: RecentTransactionsTableProps = $props();
</script>

{#if loading}
	<div class="rounded-xl border border-border bg-card shadow-sm">
		<div class="px-6 pt-6 pb-4">
			<div class="h-3 w-44 animate-pulse rounded bg-muted"></div>
		</div>
		<div class="space-y-3 px-6 pb-6">
			{#each [0, 1, 2, 3] as item (item)}
				<div class="h-10 w-full animate-pulse rounded bg-muted"></div>
			{/each}
		</div>
	</div>
{:else if error}
	<div class="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive shadow-sm">
		<div class="flex items-start justify-between gap-3">
			<div class="flex items-start gap-2">
				<AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
				<div>
					<p class="font-medium">Falha ao carregar transações recentes</p>
					<p class="text-destructive/80">{error}</p>
				</div>
			</div>
			{#if onRetry}
				<Button variant="outline" size="sm" onclick={onRetry}>Tentar novamente</Button>
			{/if}
		</div>
	</div>
{:else}
	<div class="rounded-xl border border-border bg-card shadow-sm">
		<div class="px-6 pt-6 pb-4">
			<p class="text-xs font-medium tracking-wide text-muted-foreground">Transações Recentes</p>
		</div>

		<Table>
			<TableHeader>
				<TableRow class="hover:bg-transparent">
					<TableHead class="w-24 pl-6 text-xs font-medium tracking-wide text-muted-foreground"
						>Data</TableHead
					>
					<TableHead class="text-xs font-medium tracking-wide text-muted-foreground"
						>Descrição</TableHead
					>
					<TableHead class="text-xs font-medium tracking-wide text-muted-foreground"
						>Categoria</TableHead
					>
					<TableHead class="pr-6 text-right text-xs font-medium tracking-wide text-muted-foreground"
						>Valor</TableHead
					>
				</TableRow>
			</TableHeader>
			<TableBody>
				{#each transactions as tx (tx.id)}
					<TableRow class="hover:bg-muted/30">
						<TableCell class="pl-6 tabular-nums text-muted-foreground">
							{formatDateTimeShort(tx.date)}
						</TableCell>
						<TableCell class="font-medium text-card-foreground">{tx.description}</TableCell>
						<TableCell>
							<Badge variant="secondary" class="gap-1.5 font-normal">{tx.category}</Badge>
						</TableCell>
						<TableCell
							class={cn(
								"pr-6 text-right font-medium tabular-nums",
								getUiAmountColorClass(tx.type)
							)}
						>
							{getUiAmountSign(tx.type)}
							{formatCurrency(Math.abs(tx.amountInAccountCurrency ?? tx.amount), accountCurrencyCode)}
						</TableCell>
					</TableRow>
				{/each}
				{#if transactions.length === 0}
					<TableRow>
						<TableCell class="px-6 py-8 text-center text-muted-foreground" colspan={4}>
							Sem transações recentes para o período selecionado.
						</TableCell>
					</TableRow>
				{/if}
			</TableBody>
		</Table>
	</div>
{/if}

