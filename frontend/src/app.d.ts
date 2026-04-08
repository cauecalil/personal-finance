// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	interface PluggyInstitution {
		name?: string;
	}

	interface PluggyConnector {
		name?: string;
	}

	interface PluggyItem {
		id: string;
		institution?: PluggyInstitution;
		connector?: PluggyConnector;
	}

	interface PluggyConnectSuccessPayload {
		item: PluggyItem;
	}

	interface PluggyConnectErrorPayload {
		message?: string;
	}

	interface PluggyConnectOptions {
		connectToken: string;
		includeSandbox?: boolean;
		theme?: "light" | "dark";
		language?: string;
		onSuccess?: (payload: PluggyConnectSuccessPayload) => void;
		onError?: (payload: PluggyConnectErrorPayload) => void;
	}

	interface PluggyConnectInstance {
		open?: () => void;
		init?: () => void;
	}

	interface PluggyConnectGlobalObject {
		create?: (options: PluggyConnectOptions) => PluggyConnectInstance;
		init?: (options: PluggyConnectOptions) => PluggyConnectInstance;
	}

	interface PluggyConnectGlobalFunction {
		(options: PluggyConnectOptions): PluggyConnectInstance | void;
		new (options: PluggyConnectOptions): PluggyConnectInstance;
	}

	type PluggyConnectGlobal = PluggyConnectGlobalObject | PluggyConnectGlobalFunction;

	interface Window {
		PluggyConnect?: PluggyConnectGlobal;
		Pluggy?: PluggyConnectGlobal;
	}

	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

export {};
