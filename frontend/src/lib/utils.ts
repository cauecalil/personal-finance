import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

export function formatCurrency(amount: number, currencyCode = "BRL"): string {
	return new Intl.NumberFormat("pt-BR", {
		style: "currency",
		currency: currencyCode
	}).format(amount);
}

export type WithoutChild<T> = T extends { child?: unknown } ? Omit<T, "child"> : T;
export type WithoutChildren<T> = T extends { children?: unknown }
	? Omit<T, "children">
	: T;
export type WithoutChildrenOrChild<T> = WithoutChildren<WithoutChild<T>>;
export type WithElementRef<T, U extends HTMLElement = HTMLElement> = T & { ref?: U | null };
