import type { DatePreset } from "$lib/filter-context-value";
import type { DashboardFilters } from "$lib/types";

const DATE_PRESET_LABELS: Record<DatePreset, string> = {
	today: "Hoje",
	"7d": "Últimos 7 dias",
	this_month: "Este Mês",
	"30d": "Últimos 30 dias",
	this_year: "Este Ano",
	custom: "Período específico"
};

function fmtDate(d: Date): string {
	const year = d.getFullYear();
	const month = String(d.getMonth() + 1).padStart(2, "0");
	const day = String(d.getDate()).padStart(2, "0");
	return `${year}-${month}-${day}`;
}

function labelDate(value: string): string {
	const [year, month, day] = value.split("-");
	return `${day}/${month}/${String(year).slice(-2)}`;
}

function resolvePresetDateRange(
	preset: Exclude<DatePreset, "custom">
): { dateFrom: string; dateTo: string } {
	const now = new Date();
	const dateTo = fmtDate(now);

	switch (preset) {
		case "today":
			return { dateFrom: dateTo, dateTo };
		case "7d": {
			const from = new Date(now);
			from.setDate(from.getDate() - 6);
			return { dateFrom: fmtDate(from), dateTo };
		}
		case "this_month":
			return {
				dateFrom: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-01`,
				dateTo
			};
		case "30d": {
			const from = new Date(now);
			from.setDate(from.getDate() - 29);
			return { dateFrom: fmtDate(from), dateTo };
		}
		case "this_year":
			return { dateFrom: `${now.getFullYear()}-01-01`, dateTo };
	}
}

class FiltersState {
	accountId = $state<string | null>(null);
	datePreset = $state<DatePreset>("this_month");
	customDateRange = $state<{ dateFrom: string; dateTo: string }>(resolvePresetDateRange("this_month"));

	filters = $derived<DashboardFilters>(
		this.datePreset === "custom"
			? { accountId: this.accountId, ...this.customDateRange }
			: { accountId: this.accountId, ...resolvePresetDateRange(this.datePreset) }
	);

	datePresetLabel = $derived(
		this.datePreset !== "custom"
			? DATE_PRESET_LABELS[this.datePreset]
			: `${labelDate(this.customDateRange.dateFrom)} - ${labelDate(this.customDateRange.dateTo)}`
	);

	setAccountId = (id: string | null) => {
		this.accountId = id;
	};

	setDatePreset = (preset: DatePreset) => {
		this.datePreset = preset;
	};

	setCustomDateRange = (dateFrom: string, dateTo: string) => {
		this.customDateRange = { dateFrom, dateTo };
		this.datePreset = "custom";
	};
}

export const filtersState = new FiltersState();

