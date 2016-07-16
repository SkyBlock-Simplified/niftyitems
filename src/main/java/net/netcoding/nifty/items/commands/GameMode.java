package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.mojang.MinecraftMojangProfile;
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.ListUtil;

public class GameMode extends MinecraftListener {

	public GameMode(MinecraftPlugin plugin) {
		super(plugin);
	}

	private String parseArg(String[] args, Integer index) {
		String arg = ListUtil.isEmpty(args) ? "" : args[index];
		if (arg.matches("(?i)^(0|s|survival)$")) arg = "SURVIVAL";
		if (arg.matches("(?i)^(1|c|creative)$")) arg = "CREATIVE";
		if (arg.matches("(?i)^(2|a|adventure)$")) arg = "ADVENTURE";
		if (arg.matches("(?i)^(3|e|spectator)$")) arg = "SPECTATOR";
		return arg;
	}

	@Command(name = "gamemode",
			minimumArgs = 0,
			maximumArgs = 2,
			usages = {
					@Command.Usage(match = "(creativ|adventur)e|survival|spectator", replace = "[player]")
			}
	)
	public void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		if (isConsole(source) && args.length != 2) {
			this.getLog().error(source, "You must provide both the gamemode and player name from console!");
			return;
		}

		MinecraftMojangProfile profile;
		String name = "";
		net.netcoding.nifty.common.minecraft.GameMode mode;

		try {
			if (alias.matches("^(gamemode|gm)$")) {
				if (args.length == 0) {
					this.showUsage(source);
					return;
				}

				mode = net.netcoding.nifty.common.minecraft.GameMode.valueOf(this.parseArg(args, args.length - 1));
				profile = Nifty.getMojangRepository().searchByUsername(name = (args.length == 2 ? args[0] : source.getName()));
			} else {
				alias = alias.toUpperCase();
				mode = net.netcoding.nifty.common.minecraft.GameMode.valueOf(alias);

				if (args.length == 2) {
					this.showUsage(source);
					return;
				} else
					profile = Nifty.getMojangRepository().searchByUsername(name = (args.length == 1 ? args[0] : source.getName()));
			}

			boolean self = source.getName().equals(profile.getName());

			if (isPlayer(source) && !self && !this.hasPermissions(source, "gamemode", "other")) {
				this.getLog().error(source, "You are not allowed to change the gamemode of others!");
				return;
			}

			if (!profile.getOfflinePlayer().isOnline()) {
				this.getLog().error(source, "Cannot change gamemode for offline player {{0}}!", profile.getName());
				return;
			}

			if (this.hasPermissions(source, "gamemode", mode.toString().toLowerCase())) {
				profile.getOfflinePlayer().getPlayer().setGameMode(mode);
				this.getLog().message(profile.getOfflinePlayer().getPlayer(), "Your gamemode has been changed to {{0}}.", mode.toString().toLowerCase());
				if (!self) this.getLog().message(source, "Set {{0}} gamemode for {{1}}.", mode.toString().toLowerCase(), profile.getName());
			} else
				this.getLog().error(source, "You are not allowed to change your gamemode to {{0}}!", mode);
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(source, "Unable to locate profile for {{0}}!", name);
		} catch (IllegalArgumentException iaex) {
			this.showUsage(source);
		}
	}

}