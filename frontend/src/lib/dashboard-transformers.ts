import type {
	CashflowChartPoint,
	CategoryDonutItem,
	DashboardCashflowGranularity,
	DashboardCashflowPointApi,
	DashboardCategoryAggregateApi
} from "$lib/types";
import { formatDateShort, formatMonthYearShort, formatYear } from "$lib/date";

const CATEGORY_COLORS = [
	"#3b82f6",
	"#06b6d4",
	"#6366f1",
	"#8b5cf6",
	"#ec4899",
	"#f97316",
	"#eab308",
	"#22c55e",
	"#14b8a6",
	"#f43f5e"
] as const;

const HASH_MULTIPLIER = 31;
const DEFAULT_PERCENT_DECIMALS = 2;
const PERCENT_TOTAL = 100;

function kahanSum(values: number[]): number {
	let sum = 0;
	let compensation = 0;

	for (const value of values) {
		const adjusted = value - compensation;
		const next = sum + adjusted;
		compensation = next - sum - adjusted;
		sum = next;
	}

	return sum;
}

function sanitizeAmount(value: number): number {
	if (!Number.isFinite(value)) return 0;
	return value;
}

function cashflowLabel(
	granularity: DashboardCashflowGranularity,
	periodStart: string,
	periodEnd: string
): string {
	if (granularity === "MONTHLY") return formatMonthYearShort(periodStart);
	if (granularity === "YEARLY") return formatYear(periodStart);

	if (granularity === "DAILY") return formatDateShort(periodStart);

	return `${formatDateShort(periodStart)} - ${formatDateShort(periodEnd)}`;
}

function hashString(input: string): number {
	let hash = 0;
	for (let i = 0; i < input.length; i += 1) {
		hash = (hash * HASH_MULTIPLIER + input.charCodeAt(i)) | 0;
	}

	return Math.abs(hash);
}

function categoryColor(category: string): string {
	const index = hashString(category) % CATEGORY_COLORS.length;
	return CATEGORY_COLORS[index];
}

function calculatePercentages(values: number[], decimals = DEFAULT_PERCENT_DECIMALS): number[] {
	const normalized = values.map((value) => Math.max(0, sanitizeAmount(value)));
	const total = kahanSum(normalized);

	if (total <= 0) {
		return normalized.map(() => 0);
	}

	const factor = 10 ** decimals;
	const targetUnits = PERCENT_TOTAL * factor;
	const rawUnits = normalized.map((value) => (value / total) * targetUnits);
	const baseUnits = rawUnits.map((value) => Math.floor(value));

	let remainder = targetUnits - baseUnits.reduce((sum, value) => sum + value, 0);
	if (remainder > 0) {
		const fractionalOrder = rawUnits
			.map((value, index) => ({
				index,
				fraction: value - baseUnits[index]
			}))
			.sort((a, b) => b.fraction - a.fraction);

		for (let i = 0; i < fractionalOrder.length && remainder > 0; i += 1) {
			baseUnits[fractionalOrder[i].index] += 1;
			remainder -= 1;
		}
	}

	return baseUnits.map((value) => value / factor);
}

export function mapCashflowPointsToChart(
	granularity: DashboardCashflowGranularity,
	points: DashboardCashflowPointApi[]
): CashflowChartPoint[] {
	return points.map((point) => ({
		label: cashflowLabel(granularity, point.periodStart, point.periodEnd),
		income: sanitizeAmount(point.incomeTotal),
		expenses: sanitizeAmount(point.expensesTotal)
	}));
}

export function mapCategoryAggregatesToDonut(items: DashboardCategoryAggregateApi[]): CategoryDonutItem[] {
	const totals = items.map((item) => sanitizeAmount(item.total));
	const percentages = calculatePercentages(totals);

	return items.map((item, index) => ({
		category: item.category,
		total: totals[index],
		percentage: percentages[index],
		color: categoryColor(item.category)
	}));
}

