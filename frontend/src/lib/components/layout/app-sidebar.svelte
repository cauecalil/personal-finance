<script lang="ts">
	import { page } from "$app/state";
	import { ArrowLeftRight, LayoutDashboard, Settings } from "@lucide/svelte";
	import { Separator } from "$lib/components/ui/separator";
	import { cn } from "$lib/utils";

	const navItems = [
		{
			label: "Painel",
			path: "/",
			icon: LayoutDashboard
		},
		{
			label: "Transações",
			path: "/transactions",
			icon: ArrowLeftRight
		},
		{
			label: "Configurações",
			path: "/settings",
			icon: Settings
		}
	] as const;

	function isActive(path: string): boolean {
		const pathname = page.url.pathname;
		if (path === "/") return pathname === "/";
		return pathname === path || pathname.startsWith(`${path}/`);
	}
</script>

<aside class="flex h-screen w-60 shrink-0 flex-col border-r border-sidebar-border bg-sidebar">
	<div class="flex h-16 items-center gap-2.5 border-b border-sidebar-border px-6">
		<div class="flex h-8 w-8 items-center justify-center rounded-lg">
			<img src="src/lib/assets/favicon.svg" alt="">
		</div>
		<span class="text-base font-semibold tracking-tight text-sidebar-foreground">Personal Finance</span>
	</div>


	<nav class="flex-1 space-y-1 px-3 py-4">
		{#each navItems as item (item.path)}
			<a
				href={item.path}
				class={cn(
					"flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
					isActive(item.path)
						? "bg-sidebar-accent text-sidebar-accent-foreground"
						: "text-sidebar-foreground/70 hover:bg-sidebar-accent/60 hover:text-sidebar-foreground"
				)}
			>
				<item.icon class="h-4 w-4 shrink-0" />
				{item.label}
			</a>
		{/each}
	</nav>

	<div class="px-3 pb-4">
		<Separator class="mb-4 bg-sidebar-border" />
		<p class="px-3 text-xs text-sidebar-foreground/60">Criado por: cauecalil</p>
	</div>
</aside>

