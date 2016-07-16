package net.netcoding.nifty.items.listeners;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.Event;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.event.player.PlayerGameModeChangeEvent;
import net.netcoding.nifty.common.minecraft.event.player.PlayerJoinEvent;
import net.netcoding.nifty.common.minecraft.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class GameModeFix extends MinecraftListener {

	private static final Map<String, Double> loginTime = new HashMap<>();

	public GameModeFix(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Event
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		loginTime.put(player.getName(), System.currentTimeMillis() + 100D);

		if (player.getGameMode() != Nifty.getServer().getDefaultGameMode()) {
			if (!this.hasPermissions(player, "gamemode", "maintain"))
				Nifty.getScheduler().schedule(this.getPlugin(), () -> player.setGameMode(Nifty.getServer().getDefaultGameMode()), 5L);
		}
	}

	@Event
	public void onPlayerQuit(PlayerQuitEvent event) {
		loginTime.remove(event.getPlayer().getName());
	}

	@Event
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (System.currentTimeMillis() <= loginTime.get(event.getPlayer().getName()))
			event.setCancelled(true);
	}

}