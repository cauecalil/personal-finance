import type { AnalyticsFlowType, LedgerTransactionType, UiFlowDirection } from "$lib/types";

type TransactionFlowType = LedgerTransactionType | AnalyticsFlowType;

const POSITIVE_FLOW_TYPES = new Set<TransactionFlowType>(["CREDIT", "INCOME"]);
const NEGATIVE_FLOW_TYPES = new Set<TransactionFlowType>(["DEBIT", "EXPENSE"]);

function toUiFlowDirection(type: string): UiFlowDirection {
	if (POSITIVE_FLOW_TYPES.has(type as TransactionFlowType)) return "positive";
	if (NEGATIVE_FLOW_TYPES.has(type as TransactionFlowType)) return "negative";

	// Defensive fallback for unexpected backend values.
	return "negative";
}

export function getUiAmountSign(type: string): "+" | "−" {
	return toUiFlowDirection(type) === "positive" ? "+" : "−";
}

export function getUiAmountColorClass(type: string): "text-success" | "text-destructive" {
	return toUiFlowDirection(type) === "positive" ? "text-success" : "text-destructive";
}

