package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftycore.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ClearInventory extends BukkitCommand {

	public ClearInventory(JavaPlugin plugin) {
		super(plugin, "clear");
		this.setMinimumArgsLength(0);
		this.setMaximumArgsLength(2);
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		if (isConsole(sender) && args.length < 1) {
			this.getLog().error(sender, "You must provide a player name from console!");
			return;
		}

		List<Player> players = new ArrayList<>();
		String action = "inv";

		if (args.length == 0)
			players.add((Player)sender);
		else if (args.length == 1) {
			Player player = findPlayer(args[0]);

			if (player != null)
				players.add(player);
			else {
				action = this.getAction(args[0]);
				players.add((Player)sender);
			}
		} else if (args.length == 2) {
			action = this.getAction(args[1]);

			if (this.getAction(args[0]).matches("^all|\\*$")) {
				for (Player bplayer : this.getPlugin().getServer().getOnlinePlayers())
					players.add(bplayer);
			} else {
				Player player = findPlayer(args[0]);

				if (player == null) {
					this.getLog().error(sender, "Cannot clear inventory of unknown player {{0}}!", args[0]);
					return;
				}

				players.add(player);
			}
		}

		if (players.size() > 1 || !sender.getName().equals(players.get(0).getName())) {
			if (!this.hasPermissions(sender, "clear", "other")) {
				this.getLog().error(sender, "You do not have permission to clear other players' inventories!");
				return;
			}
		}

		for (Player player : players) {
			String removed = "";

			if (action.matches("^all|inv$")) {
				removed += "inventory";
				ItemStack[] armorContents = player.getInventory().getArmorContents();
				player.getInventory().clear();
				player.getInventory().setArmorContents(armorContents);
			}

			if (action.matches("^all|armor$")) {
				removed += (StringUtil.notEmpty(removed) ? " and " : "") + "armor";
				player.getInventory().setArmorContents(null);
			}

			if (!sender.getName().equals(player.getName()))
				this.getLog().message(player, "Your {{0}} has been cleared.", removed);
		}

		String removed = "";
		if (action.matches("^all|inv$")) removed += "inventory";
		if (action.matches("^all|armor$")) removed += (StringUtil.notEmpty(removed) ? " and " : "") + "armor";
		this.getLog().message(sender, "Cleared {{0}} of {{1}}.", removed, (players.size() > 1 ? "all players" : players.get(0).getName()));
	}

	private String getAction(String arg) {
		if (arg.matches("(?i)armou?r"))
			return "armor";
		else if (arg.matches("(?i)all|\\*"))
			return "all";
		else
			return "inv";
	}

}