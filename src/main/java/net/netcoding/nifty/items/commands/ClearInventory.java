package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.inventory.item.FakeItem;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.inventory.types.PlayerInventory;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ClearInventory extends MinecraftListener {

	public ClearInventory(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "clear",
			minimumArgs = 0,
			maximumArgs = 2
	)
	public void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		if (isConsole(source) && args.length < 1) {
			this.getLog().error(source, "You must provide a player name from console!");
			return;
		}

		List<Player> players = new ArrayList<>();
		String action = "inv";

		if (args.length == 0)
			players.add((Player)source);
		else if (args.length == 1) {
			Player player = Nifty.getServer().getPlayer(args[0], true);

			if (player != null)
				players.add(player);
			else {
				action = this.getAction(args[0]);
				players.add((Player)source);
			}
		} else if (args.length == 2) {
			action = this.getAction(args[1]);

			if (this.getAction(args[0]).matches("^all|\\*$"))
				players.addAll(this.getPlugin().getServer().getPlayerList());
			else {
				Player player = Nifty.getServer().getPlayer(args[0], true);

				if (player == null) {
					this.getLog().error(source, "Cannot clear inventory of unknown player {{0}}!", args[0]);
					return;
				}

				players.add(player);
			}
		}

		if (players.size() > 1 || !source.getName().equals(players.get(0).getName())) {
			if (!this.hasPermissions(source, "clear", "other")) {
				this.getLog().error(source, "You do not have permission to clear other players' inventories!");
				return;
			}
		}

		StringBuilder message = new StringBuilder();
		message.append(action.matches("^all|inv$") ? "inventory" : "");

		if (action.matches("^all|armor$"))
			message.append(StringUtil.notEmpty(message) ? " and " : "").append("armor");

		for (Player player : players) {
			PlayerInventory playerInventory = player.getInventory();

			if (action.matches("^all|inv$")) {

				for (int i = 0; i < playerInventory.getSize(); i++) {
					if (i >= 36 && i <= 39) continue;
					if (playerInventory.getItem(i) == null) continue;
					ItemStack item = playerInventory.getItem(i);
					if (FakeItem.isAnyItemOpener(item)) continue;
					playerInventory.setItem(i, null);
				}
			}

			if (action.matches("^all|armor$")) {
				playerInventory.setArmorContents(null);
			}

			if (!source.getName().equals(player.getName()))
				this.getLog().message(player, "Your {{0}} has been cleared.", message.toString());
		}

		this.getLog().message(source, "Cleared {{0}} of {{1}}.", message.toString(), (players.size() > 1 ? "all players" : players.get(0).getName()));
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