package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.mojang.MojangRepository;
import net.netcoding.niftybukkit.util.ListUtil;
import net.netcoding.niftybukkit.util.StringUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GameMode extends BukkitCommand {

	public GameMode(JavaPlugin plugin) {
		super(plugin, "gamemode");
		this.setMinimumArgsLength(0);
		this.setMaximumArgsLength(2);
		this.editUsage(0, "creative", "[player]");
		this.editUsage(0, "survival", "[player]");
		this.editUsage(0, "adventure", "[player]");
		this.editUsage(0, "spectator", "[player]");
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		if (isConsole(sender) && args.length != 2) {
			this.getLog().error(sender, "You must provide both the gamemode and player name from console!");
			return;
		}

		MojangRepository repository = NiftyBukkit.getMojangRepository();
		org.bukkit.GameMode mode;
		String playerName;
		String arg = ListUtil.isEmpty(args) ? "" : args[0];
		if (arg.matches("(?i)^(0|s|survival)$")) arg = "SURVIVAL";
		if (arg.matches("(?i)^(1|c|creative)$")) arg = "CREATIVE";
		if (arg.matches("(?i)^(2|a|adventure)$")) arg = "ADVENTURE";
		if (arg.matches("(?i)^(3|e|spectator)$")) arg = "SPECTATOR";

		try {
			if (alias.matches("^adventure|creative|survival|spectator$")) {
				alias = alias.toUpperCase();

				if (args.length == 2) {
					mode = org.bukkit.GameMode.valueOf(arg);
					playerName = repository.searchByUsername(args[1]).getName();
				} else if (args.length == 1) {
					if ((playerName = repository.searchByUsername(arg).getName()) != null)
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
					playerName = repository.searchByUsername(args[1]).getName();
				} else if (args.length == 1) {
					mode = org.bukkit.GameMode.valueOf(arg);
					playerName = sender.getName();
				} else {
					this.showUsage(sender);
					return;
				}
			}

			if (StringUtil.notEmpty(playerName)) {
				boolean self = sender.getName().equals(playerName);
				Player player = findPlayer(playerName);

				if (isPlayer(sender) && !self && !this.hasPermissions(sender, "gamemode", "other")) {
					this.getLog().error(sender, "You are not allowed to change the gamemode of others!");
					return;
				}

				if (player != null) {
					player.setGameMode(mode);
					this.getLog().message(player, "Your gamemode has been changed to {{0}}.", mode.toString().toLowerCase());
					if (!self) this.getLog().message(sender, "Set {{0}} gamemode for {{1}}.", mode.toString().toLowerCase(), player.getName());
				} else
					this.getLog().error(sender, "Unable to locate player {{0}}!", playerName);
			} else
				this.getLog().error(sender, "Unable to change gamemode for {{0}}.", playerName);
		} catch (IllegalArgumentException ex) {
			this.showUsage(sender);
		}
	}

}