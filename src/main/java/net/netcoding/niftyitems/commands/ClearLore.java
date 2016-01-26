package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftyitems.managers.Lore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearLore extends BukkitCommand {

	public ClearLore(JavaPlugin plugin) {
		super(plugin, "clearlore");
		this.setPlayerOnly();
		this.setMinimumArgsLength(0);
		this.setMaximumArgsLength(2);
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		if (isConsole(sender) && args.length < 2) {
			this.getLog().error(sender, "You must pass a player name when clearing an items lore");
			return;
		}

		String action = "hand";

		if (ListUtil.notEmpty(args)) {
			if (args[args.length - 1].matches("(?i)^(hand|all)$")) {
				action = args[args.length - 1];
				args = StringUtil.split(" ", StringUtil.implode(" ", args, 1));
			}
		}

		Player player = args.length == 0 ? (Player)sender : findPlayer(args[0]);
		boolean isSelf = sender.getName().equals(player.getName());

		if (player == null) {
			this.getLog().error(sender, "Unable to locate player matching {{0}}!", args[0]);
			return;
		}

		if ("hand".equalsIgnoreCase(action)) {
			if (Lore.revert(player.getItemInHand()))
				this.getLog().message(sender, "The item {{0}} has had its lore removed.", player.getItemInHand().getType().name());
			else
				this.getLog().error(sender, "The item {{0}} has no lore.", player.getItemInHand().getType().name());
		} else if ("all".equalsIgnoreCase(action)) {
			for (ItemStack item : player.getInventory().getContents())
				Lore.revert(item);

			this.getLog().message(sender, "All items {0} have had their removed.", StringUtil.format("{0} {{1}}", (isSelf ? "in" : "for"), (isSelf ? "your inventory" : player.getName())));
		} else
			this.showUsage(sender);
	}

}