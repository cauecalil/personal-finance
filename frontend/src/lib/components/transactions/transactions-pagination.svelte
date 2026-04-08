<script lang="ts">
	import { ChevronLeft, ChevronRight } from "@lucide/svelte";
	import { Button } from "$lib/components/ui/button";

	interface TransactionsPaginationProps {
		page: number;
		totalPages: number;
		totalItems: number;
		pageSize: number;
		onPrev: () => void;
		onNext: () => void;
	}

	let { page, totalPages, totalItems, pageSize, onPrev, onNext }: TransactionsPaginationProps =
		$props();

	const safePage = $derived(Math.min(page, Math.max(1, totalPages)));
	const canGoPrev = $derived(safePage > 1);
	const canGoNext = $derived(safePage < totalPages);
	const rangeStart = $derived((safePage - 1) * pageSize + 1);
	const rangeEnd = $derived(Math.min(safePage * pageSize, totalItems));
</script>

<div class="flex items-center justify-between border-t border-border px-6 py-4">
	<p class="text-sm text-muted-foreground">
		{#if totalItems === 0}
			Nenhum resultado
		{:else}
			Mostrando {rangeStart}–{rangeEnd} de {totalItems}
		{/if}
	</p>
	<div class="flex items-center gap-2">
		<Button variant="outline" size="icon" class="h-8 w-8" disabled={!canGoPrev} onclick={onPrev}>
			<ChevronLeft class="h-4 w-4" />
		</Button>
		<span class="min-w-12 text-center text-sm tabular-nums text-muted-foreground">
			{safePage} / {totalPages}
		</span>
		<Button variant="outline" size="icon" class="h-8 w-8" disabled={!canGoNext} onclick={onNext}>
			<ChevronRight class="h-4 w-4" />
		</Button>
	</div>
</div>

