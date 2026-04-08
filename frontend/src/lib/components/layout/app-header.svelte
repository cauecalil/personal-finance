<script lang="ts">
	import { Building2, CalendarDays, ChevronsUpDown } from "@lucide/svelte";
	import { Button } from "$lib/components/ui/button";
	import { Input } from "$lib/components/ui/input";
	import {
		DropdownMenu,
		DropdownMenuContent,
		DropdownMenuItem,
		DropdownMenuLabel,
		DropdownMenuSeparator,
		DropdownMenuTrigger
	} from "$lib/components/ui/dropdown-menu";
	import ThemeToggle from "$lib/components/layout/theme-toggle.svelte";
	import type { DatePreset } from "$lib/filter-context-value";

	const DATE_PRESETS = [
		{ label: "Hoje", value: "today" },
		{ label: "Últimos 7 dias", value: "7d" },
		{ label: "Este Mês", value: "this_month" },
		{ label: "Últimos 30 dias", value: "30d" },
		{ label: "Este Ano", value: "this_year" }
	] as const satisfies ReadonlyArray<{ label: string; value: Exclude<DatePreset, "custom"> }>;

	const DATE_FIELD_LABEL_CLASS = "text-[11px] font-medium text-muted-foreground";
	const DATE_RANGE_ERROR_CLASS = "text-[11px] text-destructive";

	interface AppHeaderProps {
		selectedAccountLabel: string;
		selectedDateLabel: string;
		accounts: { id: string; name: string; institutionName: string }[];
		onAccountChange: (accountId: string | null) => void;
		onDatePresetChange: (preset: Exclude<DatePreset, "custom">) => void;
		customDateFrom: string;
		customDateTo: string;
		onCustomDateRangeChange: (dateFrom: string, dateTo: string) => void;
	}

	let {
		selectedAccountLabel,
		selectedDateLabel,
		accounts,
		onAccountChange,
		onDatePresetChange,
		customDateFrom,
		customDateTo,
		onCustomDateRangeChange
	}: AppHeaderProps = $props();

	let dateFrom = $state("");
	let dateTo = $state("");
	const customDateFromId = "header-custom-date-from";
	const customDateToId = "header-custom-date-to";
	const dateRangeErrorId = "header-custom-date-range-error";

	$effect(() => {
		dateFrom = customDateFrom;
		dateTo = customDateTo;
	});

	const invalidCustomRange = $derived(!dateFrom || !dateTo || dateFrom > dateTo);
</script>

<header class="flex h-16 shrink-0 items-center justify-between border-b border-border bg-background px-8">
	<div class="flex items-center gap-3">
		<DropdownMenu>
			<DropdownMenuTrigger>
				{#snippet child({ props })}
					<Button variant="outline" class="h-8 gap-2 px-3 text-sm font-medium" {...props}>
						<Building2 class="h-4 w-4 text-muted-foreground" />
						{selectedAccountLabel}
						<ChevronsUpDown class="ml-1 h-3.5 w-3.5 text-muted-foreground" />
					</Button>
				{/snippet}
			</DropdownMenuTrigger>
			<DropdownMenuContent align="start" class="w-56">
				<DropdownMenuLabel class="text-xs font-medium tracking-wide text-muted-foreground">
					Conta
				</DropdownMenuLabel>
				<DropdownMenuSeparator />
				<DropdownMenuItem onSelect={() => onAccountChange(null)}>Todas as Contas</DropdownMenuItem>
				<DropdownMenuSeparator />
				{#each accounts as acc (acc.id)}
					<DropdownMenuItem onSelect={() => onAccountChange(acc.id)}>
						<div class="flex flex-col">
							<span class="text-sm">{acc.name}</span>
							<span class="text-xs text-muted-foreground">{acc.institutionName}</span>
						</div>
					</DropdownMenuItem>
				{/each}
			</DropdownMenuContent>
		</DropdownMenu>

		<DropdownMenu>
			<DropdownMenuTrigger>
				{#snippet child({ props })}
					<Button variant="outline" class="h-8 gap-2 px-3 text-sm font-medium" {...props}>
						<CalendarDays class="h-4 w-4 text-muted-foreground" />
						{selectedDateLabel}
						<ChevronsUpDown class="ml-1 h-3.5 w-3.5 text-muted-foreground" />
					</Button>
				{/snippet}
			</DropdownMenuTrigger>
			<DropdownMenuContent align="start" class="w-48">
				<DropdownMenuLabel class="text-xs font-medium tracking-wide text-muted-foreground">
					Período
				</DropdownMenuLabel>
				<DropdownMenuSeparator />
				{#each DATE_PRESETS as preset (preset.value)}
					<DropdownMenuItem onSelect={() => onDatePresetChange(preset.value)}>
						{preset.label}
					</DropdownMenuItem>
				{/each}
				<DropdownMenuSeparator />
				<div class="space-y-2.5 rounded-md bg-muted/30 p-2" role="group">
					<p class="text-xs font-medium text-muted-foreground">Período específico</p>
					<div class="space-y-1">
						<label class={DATE_FIELD_LABEL_CLASS} for={customDateFromId}>Data inicial</label>
						<Input
							id={customDateFromId}
							type="date"
							bind:value={dateFrom}
							class="h-8 text-xs"
							aria-invalid={invalidCustomRange}
							aria-describedby={invalidCustomRange ? dateRangeErrorId : undefined}
							onkeydown={(event) => event.stopPropagation()}
						/>
					</div>
					<div class="space-y-1">
						<label class={DATE_FIELD_LABEL_CLASS} for={customDateToId}>Data final</label>
						<Input
							id={customDateToId}
							type="date"
							bind:value={dateTo}
							class="h-8 text-xs"
							aria-invalid={invalidCustomRange}
							aria-describedby={invalidCustomRange ? dateRangeErrorId : undefined}
							onkeydown={(event) => event.stopPropagation()}
						/>
					</div>
					{#if dateFrom && dateTo && dateFrom > dateTo}
						<p id={dateRangeErrorId} class={DATE_RANGE_ERROR_CLASS} role="alert">
							A data final deve ser maior ou igual a inicial.
						</p>
					{/if}
					<Button
						type="button"
						variant="secondary"
						size="sm"
						class="h-8 w-full text-xs"
						disabled={invalidCustomRange}
						onclick={() => onCustomDateRangeChange(dateFrom, dateTo)}
					>
						Aplicar período
					</Button>
				</div>
			</DropdownMenuContent>
		</DropdownMenu>
	</div>

	<div class="flex items-center gap-2">
		<ThemeToggle />
	</div>
</header>


