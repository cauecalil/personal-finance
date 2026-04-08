<script lang="ts">
	import { Badge } from "$lib/components/ui/badge";
	import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "$lib/components/ui/table";
	import { formatDateTimeShort } from "$lib/date";
	import { getUiAmountColorClass, getUiAmountSign } from "$lib/transaction-flow";
	import type { Transaction } from "$lib/types";
	import { cn, formatCurrency } from "$lib/utils";

	interface TransactionsTableProps {
		transactions: Transaction[];
		currencyCode: string;
	}

	let { transactions, currencyCode }: TransactionsTableProps = $props();
</script>

<Table>
	<TableHeader>
		<TableRow class="hover:bg-transparent">
			<TableHead class="w-28 pl-6 text-xs font-medium tracking-wide text-muted-foreground">Data</TableHead>
			<TableHead class="text-xs font-medium tracking-wide text-muted-foreground">Descrição</TableHead>
			<TableHead class="text-xs font-medium tracking-wide text-muted-foreground">Categoria</TableHead>
			<TableHead class="pr-6 text-right text-xs font-medium tracking-wide text-muted-foreground">Valor</TableHead>
		</TableRow>
	</TableHeader>
	<TableBody>
		{#if transactions.length === 0}
			<TableRow>
				<TableCell colspan={4} class="h-32 text-center text-muted-foreground">
					Nenhuma transação encontrada para os filtros selecionados.
				</TableCell>
			</TableRow>
		{:else}
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
						class={cn("pr-6 text-right font-medium tabular-nums", getUiAmountColorClass(tx.type))}
					>
						{getUiAmountSign(tx.type)}
						{formatCurrency(Math.abs(tx.amountInAccountCurrency ?? tx.amount), currencyCode)}
					</TableCell>
				</TableRow>
			{/each}
		{/if}
	</TableBody>
</Table>


