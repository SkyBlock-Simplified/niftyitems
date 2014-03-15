package net.netcoding.niftyitems.commands;

import java.sql.SQLException;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.util.StringUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GameMode extends BukkitCommand {

	public GameMode(JavaPlugin plugin) {
		super(plugin, "clearinventory", false);
	}

	@Override
	public void command(CommandSender sender, String alias, String[] args) throws SQLException, Exception {
		if (args.length < 3) {
			if (isConsole(sender) && args.length != 2) {
				this.getLog().error(sender, "You must provide both the gamemode and player name from console!");
				return;
			}

			org.bukkit.GameMode mode;
			String playerName;
			String arg = StringUtil.isEmpty(args[0]) ? "" : args[0];
			if (arg.matches("(?i)^(0|s|survival)$")) arg = "survival";
			if (arg.matches("(?i)^(1|c|creative)$")) arg = "creative";
			if (arg.matches("(?i)^(2|a|adventure)$")) arg = "adventure";

			if (alias.matches("(?i)^adventure|creative|survival$")) {
				if (args.length == 2) {
					mode = org.bukkit.GameMode.valueOf(arg);
					playerName = findPlayerName(args[1]);
				} else if (args.length == 1) {
					if ((playerName = findPlayerName(arg)) != null)
						mode = org.bukkit.GameMode.valueOf(alias);
					else {
						mode = org.bukkit.GameMode.valueOf(arg);
						playerName = sender.getName();
					}
				} else {
					mode = org.bukkit.GameMode.valueOf(alias);
					playerName = sender.getName();
				}
			} else {
				if (args.length == 2) {
					mode = org.bukkit.GameMode.valueOf(arg);
					playerName = findPlayerName(args[1]);
				} else if (args.length == 1) {
					mode = org.bukkit.GameMode.valueOf(arg);
					playerName = sender.getName();
				} else {
					this.showUsage(sender);
					return;
				}
			}

			if (StringUtil.notEmpty(playerName)) {
				if (isPlayer(sender) && !sender.getName().equals(playerName) && !this.hasPermissions(sender, "gamemode", "other")) 
					return;

				Player player = findPlayer(playerName);
				player.setGameMode(mode);
				this.getLog().message(sender, "Your gamemode has been changed to {%1$s}.", mode.toString().toLowerCase());
				if (sender.getName().equals(player.getName())) this.getLog().message(sender, "Set {%1$s} gamemode for {%2$s}.", mode.toString().toLowerCase(), player.getName());
			} else
				this.getLog().error(sender, "Unable to change gamemode for {%1$s}.", playerName);
		} else
			this.showUsage(sender);
	}

}