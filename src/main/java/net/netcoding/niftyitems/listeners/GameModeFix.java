package net.netcoding.niftyitems.listeners;

import java.util.HashMap;
import java.util.Map;

import net.netcoding.niftybukkit.minecraft.BukkitListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameModeFix extends BukkitListener {

	private static final Map<String, Double> loginTime = new HashMap<>();

	public GameModeFix(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		loginTime.put(player.getName(), System.currentTimeMillis() + 100D);

		if (!player.getGameMode().equals(this.getPlugin().getServer().getDefaultGameMode())) {
			if (!this.hasPermissions(player, "gamemode", "maintain")) {
				this.getPlugin().getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
					@Override
					public void run() {
						player.setGameMode(getPlugin().getServer().getDefaultGameMode());
					}
				}, 5L);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		loginTime.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (System.currentTimeMillis() <= loginTime.get(event.getPlayer().getName()))
			event.setCancelled(true);
	}

}