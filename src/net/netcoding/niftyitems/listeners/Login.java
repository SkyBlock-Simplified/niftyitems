package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.minecraft.BukkitListener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Login extends BukkitListener {

	public Login(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode().equals(GameMode.CREATIVE)) {
			if (!this.hasPermissions(player, "gamemode", "maintain"))
				player.setGameMode(this.getPlugin().getServer().getDefaultGameMode());
		}
	}

}