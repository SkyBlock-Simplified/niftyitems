package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
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
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		if (isConsole(sender) && args.length < 2) {
			this.getLog().error(sender, "You must pass a player name when clearing an items lore");
			return;
		}

		Player player = args.length == 1 ? (Player)sender : findPlayer(args[0]);

		if (player == null) {
			this.getLog().error(sender, "Unable to locate player matching {{0}}!", args[0]);
			return;
		}

		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("hand"))) {
			if (Lore.revert(player.getItemInHand()))
				this.getLog().message(sender, "The item {{0}} has had its lore removed.", player.getItemInHand().getType().name());
			else
				this.getLog().error(sender, "The item {{0}} has no lore.", player.getItemInHand().getType().name());
		} else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
			for (ItemStack item : player.getInventory().getContents())
				Lore.revert(item);

			this.getLog().message(sender, "All items in your inventory have had their removed.");
		} else
			this.showUsage(sender);
	}

}