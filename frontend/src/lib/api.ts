import type {
	Account,
	ApiError,
	ApiViolation,
	BankConnection,
	BankConnectionPayload,
	ConnectTokenResponse,
	CredentialsPayload,
	CredentialsStatus,
	DashboardCashflowResponse,
	DashboardCategoriesResponse,
	DashboardMetricsResponse,
	TransactionsListResponse
} from "./types";

class ApiRequestError extends Error {
	public status: number;
	public type: string;
	public title: string;
	public detail: string;
	public instance: string;
	public violations?: ApiViolation[];

	constructor(apiError: ApiError) {
		super(apiError.detail || apiError.title);
		this.name = "ApiRequestError";
		this.status = apiError.status;
		this.type = apiError.type;
		this.title = apiError.title;
		this.detail = apiError.detail;
		this.instance = apiError.instance;
		this.violations = apiError.violations;
	}
}

function asObject(value: unknown): Record<string, unknown> | null {
	if (typeof value !== "object" || value === null) return null;
	return value as Record<string, unknown>;
}

function parseViolations(raw: unknown): ApiViolation[] | undefined {
	if (!Array.isArray(raw)) return undefined;

	const items = raw
		.map((entry) => {
			const violation = asObject(entry);
			if (!violation) return null;

			const field = violation.field;
			const message = violation.message;
			if (typeof field !== "string" || typeof message !== "string") return null;

			return { field, message };
		})
		.filter((entry): entry is ApiViolation => entry !== null);

	return items.length > 0 ? items : undefined;
}

function normalizeApiError(response: Response, rawBody: unknown): ApiError {
	const body = asObject(rawBody);
	const problemStatus = body?.status;
	const problemTitle = body?.title;
	const problemDetail = body?.detail;
	const problemType = body?.type;
	const problemInstance = body?.instance;
	const problemViolations = parseViolations(body?.violations);

	const status = typeof problemStatus === "number" ? problemStatus : response.status;
	const title =
		typeof problemTitle === "string" && problemTitle.trim()
			? problemTitle
			: response.statusText || "Erro inesperado";
	const detail =
		typeof problemDetail === "string" && problemDetail.trim() ? problemDetail : title;
	const type =
		typeof problemType === "string" && problemType.trim() ? problemType : "about:blank";
	const instance =
		typeof problemInstance === "string" && problemInstance.trim()
			? problemInstance
			: response.url || "unknown";

	return {
		status,
		type,
		title,
		detail,
		instance,
		violations: problemViolations
	};
}

export function getApiErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof ApiRequestError) {
		return error.detail || error.title || fallback;
	}

	if (error instanceof Error && error.message) {
		return error.message;
	}

	return fallback;
}

async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
	const hasBody = options.body != null;
	const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
	const headers = new Headers(options.headers ?? undefined);

	if (!headers.has("Accept")) {
		headers.set("Accept", "application/json");
	}

	if (hasBody && !isFormData && !headers.has("Content-Type")) {
		headers.set("Content-Type", "application/json");
	}

	const response = await fetch(url, {
		headers,
		...options
	});

	const rawText = await response.text();
	const contentType = response.headers.get("content-type") ?? "";
	let parsedBody: unknown = rawText || null;
	if (rawText && contentType.includes("application/json")) {
		try {
			parsedBody = JSON.parse(rawText);
		} catch {
			parsedBody = rawText;
		}
	}

	if (!response.ok) {
		throw new ApiRequestError(normalizeApiError(response, parsedBody));
	}

	if (!rawText) return undefined as T;
	if (contentType.includes("application/json")) {
		return parsedBody as T;
	}

	return rawText as T;
}

function toQueryString(params: Record<string, string | number | null | undefined>): string {
	const sp = new URLSearchParams();
	for (const [key, value] of Object.entries(params)) {
		if (value != null && value !== "") sp.set(key, String(value));
	}
	const qs = sp.toString();
	return qs ? `?${qs}` : "";
}

export async function saveCredentials(payload: CredentialsPayload): Promise<void> {
	await request<void>("/api/credentials", {
		method: "POST",
		body: JSON.stringify(payload)
	});
}

export async function deleteCredentials(): Promise<void> {
	await request<void>("/api/credentials", { method: "DELETE" });
}

export async function getCredentialsStatus(): Promise<CredentialsStatus> {
	return request<CredentialsStatus>("/api/credentials/status");
}

export async function createConnectToken(): Promise<ConnectTokenResponse> {
	return request<ConnectTokenResponse>("/api/connect-token", { method: "POST" });
}

export async function fetchBankConnections(): Promise<BankConnection[]> {
	return request<BankConnection[]>("/api/bank-connections");
}

export async function saveBankConnection(payload: BankConnectionPayload): Promise<void> {
	await request<void>("/api/bank-connections", {
		method: "POST",
		body: JSON.stringify(payload)
	});
}

export async function deleteBankConnection(id: number): Promise<void> {
	await request<void>(`/api/bank-connections/${encodeURIComponent(id)}`, {
		method: "DELETE"
	});
}

export async function fetchAccounts(): Promise<Account[]> {
	return request<Account[]>("/api/accounts");
}

function getDashboardHeaders(): HeadersInit {
	return { "Time-Zone": Intl.DateTimeFormat().resolvedOptions().timeZone };
}

function getDashboardQuery(accountId: string | null, dateFrom: string, dateTo: string): string {
	return toQueryString({ accountId, fromDate: dateFrom, toDate: dateTo });
}

export async function fetchDashboardMetrics(
	accountId: string | null,
	dateFrom: string,
	dateTo: string
): Promise<DashboardMetricsResponse> {
	const qs = getDashboardQuery(accountId, dateFrom, dateTo);
	return request<DashboardMetricsResponse>(`/api/dashboard/metrics${qs}`, {
		headers: getDashboardHeaders()
	});
}

export async function fetchDashboardCashflow(
	accountId: string | null,
	dateFrom: string,
	dateTo: string
): Promise<DashboardCashflowResponse> {
	const qs = getDashboardQuery(accountId, dateFrom, dateTo);
	return request<DashboardCashflowResponse>(`/api/dashboard/cashflow${qs}`, {
		headers: getDashboardHeaders()
	});
}

export async function fetchDashboardCategories(
	accountId: string | null,
	dateFrom: string,
	dateTo: string
): Promise<DashboardCategoriesResponse> {
	const qs = getDashboardQuery(accountId, dateFrom, dateTo);
	return request<DashboardCategoriesResponse>(`/api/dashboard/categories${qs}`, {
		headers: getDashboardHeaders()
	});
}

export async function fetchTransactions(
	accountId: string | null,
	dateFrom: string,
	dateTo: string,
	page: number,
	pageSize: number,
	sort: "DESC" | "ASC" = "DESC"
): Promise<TransactionsListResponse> {
	const backendPage = Math.max(0, page - 1);
	const qs = toQueryString({
		accountId,
		fromDate: dateFrom,
		toDate: dateTo,
		page: backendPage,
		pageSize,
		sort
	});
	return request<TransactionsListResponse>(`/api/transactions${qs}`, {
		headers: getDashboardHeaders()
	});
}

export async function syncData(): Promise<void> {
	await request<void>("/api/sync", { method: "POST" });
}

export async function sendHeartbeat(): Promise<void> {
	await request<void>("/api/heartbeat", {
		method: "POST",
		keepalive: true,
		cache: "no-store"
	});
}
