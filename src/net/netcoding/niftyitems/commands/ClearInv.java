package net.netcoding.niftyitems.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.util.StringUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearInv extends BukkitCommand {

	public ClearInv(JavaPlugin plugin) {
		super(plugin, "clearinventory", false);
	}

	@Override
	public void command(CommandSender sender, String alias, String[] args) throws SQLException, Exception {
		if (isConsole(sender) && args.length == 0) {
			this.getLog().error(sender, "You must provide a player name from console!");
			return;
		}

		List<Player> players = new ArrayList<>();
		String action = "inv";

		if (args.length == 0)
			players.add((Player)sender);
		else if (args.length == 1) {
			Player player = findPlayer(args[0]);

			if (player != null) {
				if (!sender.getName().equals(player.getName()) && !this.hasPermissions(sender, "clear", "other"))
					return;

				players.add(player);
			} else {
				action = this.getAction(args[0]);

				if (action.equals("all")) {
					for (Player bplayer : this.getPlugin().getServer().getOnlinePlayers())
						players.add(bplayer);
				} else
					players.add((Player)sender);
			}
		} else if (args.length == 2) {
			action = this.getAction(args[1]);

			if (this.getAction(args[0]).equals("all")) {
				for (Player bplayer : this.getPlugin().getServer().getOnlinePlayers())
					players.add(bplayer);
			} else {
				Player player = findPlayer(args[0]);

				if (player == null) {
					this.getLog().error(sender, "Cannot clear inventory of unknown player {%1$s}!", args[0]);
					return;
				} else {
					if (!sender.getName().equals(player.getName()) && !this.hasPermissions(sender, "clear", "other"))
						return;

					players.add(player);
				}
			}
		}

		for (Player player : players) {
			String removed = "";

			if (action.matches("^all|inv$")) {
				removed += "inventory";
				player.getInventory().clear();
			}

			if (action.matches("^all|armor$")) {
				removed += (StringUtil.notEmpty(removed) ? " and " : "") + "armor";
				player.getInventory().setArmorContents(null);
			}

			if (!sender.getName().equals(player.getName()))
				this.getLog().message(player, "Your %1$s have been cleared.", "");
		}

		String removed = "";
		if (action.matches("^all|inv$")) removed += "inventory";
		if (action.matches("^all|armor$")) removed += (StringUtil.notEmpty(removed) ? " and " : "") + "armor";
		this.getLog().message(sender, "Cleared %1$s of %2$s.", removed, (players.size() > 1 ? "all players" : players.get(0).getName()));
	}

	private String getAction(String arg) {
		if (arg.matches("(?i)^inv(?:entory)?$"))
			return "inv";
		else if (arg.matches("(?i)armou?r"))
			return "armor";
		else if (arg.matches("(?i)all|\\*"))
			return "all";
		else
			return "inv";
	}

}