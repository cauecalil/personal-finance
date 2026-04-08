<script lang="ts">
	import { Moon, Sun } from "@lucide/svelte";
	import { mode, setMode } from "mode-watcher";
	import { Button } from "$lib/components/ui/button";
	import { Tooltip, TooltipContent, TooltipTrigger } from "$lib/components/ui/tooltip";

	function toggleTheme() {
		setMode(mode.current === "dark" ? "light" : "dark");
	}
</script>

<Tooltip>
	<TooltipTrigger>
		{#snippet child({ props })}
			<Button
				{...props}
				variant="ghost"
				size="icon"
				onclick={toggleTheme}
				class="h-9 w-9 text-muted-foreground hover:text-foreground"
			>
				<Sun class="h-4 w-4 scale-100 rotate-0 transition-all dark:scale-0 dark:-rotate-90" />
				<Moon class="absolute h-4 w-4 scale-0 rotate-90 transition-all dark:scale-100 dark:rotate-0" />
				<span class="sr-only">Alternar tema</span>
			</Button>
		{/snippet}
	</TooltipTrigger>
	<TooltipContent>
		<p>Alternar tema</p>
	</TooltipContent>
</Tooltip>

