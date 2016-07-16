package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.items.managers.Lore;

public class ClearLore extends MinecraftListener {

	public ClearLore(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "clearlore",
			playerOnly = true,
			minimumArgs = 0,
			maximumArgs = 2
	)
	public void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		if (isConsole(source) && args.length < 2) {
			this.getLog().error(source, "You must pass a player name when clearing an items lore");
			return;
		}

		String action = "hand";

		if (ListUtil.notEmpty(args)) {
			if (args[args.length - 1].matches("(?i)^(hand|all)$")) {
				action = args[args.length - 1];
				args = StringUtil.split(" ", StringUtil.implode(" ", args, 1));
			}
		}

		Player player = args.length == 0 ? (Player)source : Nifty.getServer().getPlayer(args[0], true);
		boolean isSelf = source.getName().equals(player.getName());

		if (player == null) {
			this.getLog().error(source, "Unable to locate player matching {{0}}!", args[0]);
			return;
		}

		if ("hand".equalsIgnoreCase(action)) {
			ItemStack item = player.getItemInHand();

			if (Lore.revert(item)) {
				player.setItemInHand(item);
				this.getLog().message(source, "The item {{0}} has had its lore removed.", item.getType().name());
			} else
				this.getLog().error(source, "The item {{0}} has no lore.", player.getItemInHand().getType().name());
		} else if ("all".equalsIgnoreCase(action)) {
			ItemStack[] contents = player.getInventory().getContents();

			for (int i = 0; i < contents.length; i++) {
				if (contents[i] == null)
					continue;

				ItemStack item = contents[i];
				Lore.revert(item);
				player.getInventory().setItem(i, item);
			}

			this.getLog().message(source, "All items {0} have had their removed.", StringUtil.format("{0} {{1}}", (isSelf ? "in" : "for"), (isSelf ? "your inventory" : player.getName())));
		} else
			this.showUsage(source);
	}

}