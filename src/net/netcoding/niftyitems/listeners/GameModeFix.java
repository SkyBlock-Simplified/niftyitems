package net.netcoding.niftyitems.listeners;

import java.util.HashMap;
import java.util.Map;

import net.netcoding.niftybukkit.minecraft.BukkitListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameModeFix extends BukkitListener {

	private static final Map<String, Long> loginTime = new HashMap<>();

	public GameModeFix(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		loginTime.put(player.getName(), (System.currentTimeMillis() / 1000) + 1);

		this.getLog().message(player, player.getGameMode().name());
		if (!player.getGameMode().equals(this.getPlugin().getServer().getDefaultGameMode())) {
			this.getLog().console("made it");
			if (!this.hasPermissions(player, "gamemode", "maintain")) {
				this.getLog().console("made it x2");
				Bukkit.getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
					@Override
					public void run() {
						getLog().console("made it x3");
						player.setGameMode(getPlugin().getServer().getDefaultGameMode());
					}
				}, 125L);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		loginTime.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if ((System.currentTimeMillis() / 1000) <= loginTime.get(event.getPlayer().getName())) {
			event.setCancelled(true);
			this.getLog().console("cancelled");
		}
	}

}