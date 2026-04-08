export interface CredentialsPayload {
	clientId: string;
	clientSecret: string;
}

export interface CredentialsStatus {
	configured: boolean;
}

export interface ConnectTokenResponse {
	connectToken: string;
}

export interface BankConnectionPayload {
	itemId: string;
	bankName: string;
}

export interface BankConnection {
	id: number;
	itemId: string;
	bankName: string;
	status: BankConnectionStatus;
	lastSyncAt: string;
}

export type BankConnectionStatus = "UPDATED" | "PENDING" | "ERROR";

export type LedgerTransactionType = "CREDIT" | "DEBIT";

export type AnalyticsFlowType = "INCOME" | "EXPENSE";

export type UiFlowDirection = "positive" | "negative";

export interface Institution {
	id: string;
	name: string;
	logoUrl: string | null;
	primaryColor: string | null;
}

export interface Account {
	id: string;
	name: string;
	type: string;
	subtype: string;
	number?: string;
	balance: number;
	currency: string;
	bank?: Institution;
}

export interface Transaction {
	id: string;
	description: string;
	currency: string;
	amount: number;
	amountInAccountCurrency?: number;
	type: LedgerTransactionType;
	date: string;
	category: string;
}

export interface DashboardFilters {
	accountId: string | null;
	dateFrom: string;
	dateTo: string;
}

export interface DashboardMetrics {
	currentBalance: number;
	totalIncome: number;
	totalExpenses: number;
	currencyCode: string;
}

export interface DashboardCashflowPointApi {
	periodStart: string;
	periodEnd: string;
	incomeTotal: number;
	expensesTotal: number;
}

export type DashboardCashflowGranularity = "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY";

export interface DashboardCategoryAggregateApi {
	category: string;
	total: number;
}

export type DashboardMetricsResponse = DashboardMetrics;

export interface DashboardCashflowResponse {
	granularity: DashboardCashflowGranularity;
	points: DashboardCashflowPointApi[];
}

export interface DashboardCategoriesResponse {
	expenses: DashboardCategoryAggregateApi[];
	income: DashboardCategoryAggregateApi[];
}


export interface CashflowChartPoint {
	label: string;
	income: number;
	expenses: number;
}

export interface CategoryDonutItem {
	category: string;
	total: number;
	percentage: number;
	color: string;
}

export interface TransactionsListResponse {
	items: Transaction[];
	page: number;
	pageSize: number;
	totalItems: number;
	totalPages: number;
	hasNextPage: boolean;
	hasPrevPage: boolean;
}

export interface ApiViolation {
	field: string;
	message: string;
}

export interface ApiError {
	type: string;
	title: string;
	status: number;
	detail: string;
	instance: string;
	violations?: ApiViolation[];
}

