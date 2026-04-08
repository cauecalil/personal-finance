import { browser } from "$app/environment";
import { fetchAccounts, getCredentialsStatus } from "$lib/api";
import type { Account } from "$lib/types";

class SetupState {
	checking = $state(true);
	isSetupComplete = $state(false);
	accounts = $state<Account[]>([]);
	initialized = false;
	private bootstrapPromise: Promise<void> | null = null;

	async bootstrap(): Promise<void> {
		if (this.initialized) return;

		if (!browser) {
			this.initialized = true;
			this.checking = false;
			return;
		}

		if (this.bootstrapPromise) {
			await this.bootstrapPromise;
			return;
		}

		this.bootstrapPromise = (async () => {
			try {
				const { configured } = await getCredentialsStatus();

				if (configured) {
					const accs = await fetchAccounts();
					this.accounts = accs;
					this.isSetupComplete = true;
				}
			} catch (error) {
				// Keep app usable even when backend is offline, but surface diagnostics in browser console.
				console.error("[setupState] Falha no carregamento inicial do setup", error);
			} finally {
				this.checking = false;
				this.initialized = true;
				this.bootstrapPromise = null;
			}
		})();

		await this.bootstrapPromise;
	}

	completeSetup = (newAccounts: Account[]) => {
		this.accounts = newAccounts;
		this.isSetupComplete = true;
	};

	resetSetup = () => {
		this.accounts = [];
		this.isSetupComplete = false;
	};
}

export const setupState = new SetupState();

