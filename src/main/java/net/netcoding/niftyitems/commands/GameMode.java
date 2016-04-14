package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.niftycore.util.ListUtil;
import org.bukkit.command.CommandSender;
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

	private String parseArg(String[] args, Integer index) {
		String arg = ListUtil.isEmpty(args) ? "" : args[index];
		if (arg.matches("(?i)^(0|s|survival)$")) arg = "SURVIVAL";
		if (arg.matches("(?i)^(1|c|creative)$")) arg = "CREATIVE";
		if (arg.matches("(?i)^(2|a|adventure)$")) arg = "ADVENTURE";
		if (arg.matches("(?i)^(3|e|spectator)$")) arg = "SPECTATOR";
		return arg;
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		if (isConsole(sender) && args.length != 2) {
			this.getLog().error(sender, "You must provide both the gamemode and player name from console!");
			return;
		}

		BukkitMojangProfile profile;
		String name = "";
		org.bukkit.GameMode mode;

		try {
			if (alias.matches("^(gamemode|gm)$")) {
				if (args.length == 0) {
					this.showUsage(sender);
					return;
				}

				mode = org.bukkit.GameMode.valueOf(this.parseArg(args, args.length - 1));
				profile = NiftyBukkit.getMojangRepository().searchByUsername(name = (args.length == 2 ? args[0] : sender.getName()));
			} else {
				alias = alias.toUpperCase();
				mode = org.bukkit.GameMode.valueOf(alias);

				if (args.length == 2) {
					this.showUsage(sender);
					return;
				} else
					profile = NiftyBukkit.getMojangRepository().searchByUsername(name = (args.length == 1 ? args[0] : sender.getName()));
			}

			boolean self = sender.getName().equals(profile.getName());

			if (isPlayer(sender) && !self && !this.hasPermissions(sender, "gamemode", "other")) {
				this.getLog().error(sender, "You are not allowed to change the gamemode of others!");
				return;
			}

			if (!profile.getOfflinePlayer().isOnline()) {
				this.getLog().error(sender, "Cannot change gamemode for offline player {{0}}!", profile.getName());
				return;
			}

			if (this.hasPermissions(sender, "gamemode", mode.toString().toLowerCase())) {
				profile.getOfflinePlayer().getPlayer().setGameMode(mode);
				this.getLog().message(profile.getOfflinePlayer().getPlayer(), "Your gamemode has been changed to {{0}}.", mode.toString().toLowerCase());
				if (!self) this.getLog().message(sender, "Set {{0}} gamemode for {{1}}.", mode.toString().toLowerCase(), profile.getName());
			} else
				this.getLog().error(sender, "You are not allowed to change your gamemode to {{0}}!", mode);
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(sender, "Unable to locate profile for {{0}}!", name);
		} catch (IllegalArgumentException iaex) {
			this.showUsage(sender);
		}
	}

}