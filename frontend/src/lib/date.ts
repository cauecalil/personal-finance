function normalizeApiInstant(iso: string): string {
	// JS Date supports milliseconds; backend can send nanoseconds.
	return iso.replace(/\.([0-9]{3})[0-9]+(Z|[+-][0-9]{2}:[0-9]{2})$/, ".$1$2");
}

function parseApiInstant(iso: string): Date | null {
	const normalized = normalizeApiInstant(iso);
	const parsed = new Date(normalized);
	if (Number.isNaN(parsed.getTime())) return null;
	return parsed;
}

export function formatDateTimeShort(iso: string): string {
	const date = parseApiInstant(iso);
	if (!date) return iso;

	const day = String(date.getDate()).padStart(2, "0");
	const month = String(date.getMonth() + 1).padStart(2, "0");
	const year = String(date.getFullYear()).slice(-2);
	const hour = String(date.getHours()).padStart(2, "0");
	const minute = String(date.getMinutes()).padStart(2, "0");

	return `${day}/${month}/${year} - ${hour}:${minute}`;
}

export function formatDateShort(iso: string): string {
	const date = parseApiInstant(iso);
	if (!date) return iso;

	return new Intl.DateTimeFormat("pt-BR", {
		day: "2-digit",
		month: "2-digit"
	}).format(date);
}

export function formatMonthYearShort(iso: string): string {
	const date = parseApiInstant(iso);
	if (!date) return iso;

	return new Intl.DateTimeFormat("pt-BR", {
		month: "short",
		year: "2-digit"
	}).format(date);
}

export function formatYear(iso: string): string {
	const date = parseApiInstant(iso);
	if (!date) return iso;

	return new Intl.DateTimeFormat("pt-BR", {
		year: "numeric"
	}).format(date);
}

