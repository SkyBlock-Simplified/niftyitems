package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.niftycore.util.ListUtil;

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

		BukkitMojangProfile profile;
		org.bukkit.GameMode mode;
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
					profile = NiftyBukkit.getMojangRepository().searchByUsername(args[1]);
				} else if (args.length == 1) {
					try {
						profile = NiftyBukkit.getMojangRepository().searchByUsername(arg);
						mode = org.bukkit.GameMode.valueOf(alias);
					} catch (ProfileNotFoundException pfne) {
						mode = org.bukkit.GameMode.valueOf(arg);
						profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
					}
				} else {
					mode = org.bukkit.GameMode.valueOf(alias);
					profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
				}
			} else {
				if (args.length == 2) {
					mode = org.bukkit.GameMode.valueOf(arg);
					profile = NiftyBukkit.getMojangRepository().searchByUsername(args[1]);
				} else if (args.length == 1) {
					mode = org.bukkit.GameMode.valueOf(arg);
					profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
				} else {
					this.showUsage(sender);
					return;
				}
			}

			if (profile.getOfflinePlayer().isOnline()) {
				boolean self = sender.getName().equals(profile.getName());

				if (isPlayer(sender) && !self && !this.hasPermissions(sender, "gamemode", "other")) {
					this.getLog().error(sender, "You are not allowed to change the gamemode of others!");
					return;
				}

				profile.getOfflinePlayer().getPlayer().setGameMode(mode);
				this.getLog().message(profile.getOfflinePlayer().getPlayer(), "Your gamemode has been changed to {{0}}.", mode.toString().toLowerCase());
				if (!self) this.getLog().message(sender, "Set {{0}} gamemode for {{1}}.", mode.toString().toLowerCase(), profile.getName());
			} else
				this.getLog().error(sender, "Unable to change gamemode for offline player {{0}}.", profile.getName());
		} catch (IllegalArgumentException ex) {
			this.showUsage(sender);
		}
	}

}