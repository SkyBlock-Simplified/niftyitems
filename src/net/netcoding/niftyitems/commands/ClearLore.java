package net.netcoding.niftyitems.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftyitems.managers.Cache;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearLore extends BukkitCommand {

	public ClearLore(JavaPlugin plugin) {
		super(plugin, false);
		this.isPlayerOnly();
	}

	private boolean revertItem(ItemStack item) {
		if (!Cache.Settings.isRestricted(item).equalsIgnoreCase("none") && item.getItemMeta().hasLore()) {
			ItemMeta itemMeta = item.getItemMeta();
			List<String> lores = itemMeta.getLore();
			List<String> notOurs = new ArrayList<>();

			for (String lore : lores) {
				if (!lore.startsWith(Cache.Settings.getLore("creative")) && !lore.startsWith(Cache.Settings.getLore("spawned")))
					notOurs.add(lore);
			}

			itemMeta.setLore(notOurs);
			item.setItemMeta(itemMeta);
			return true;
		}

		return false;
	}

	@Override
	public void command(CommandSender sender, String alias, String[] args) throws SQLException, Exception {
		Player player = (Player)sender;

		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("hand"))) {
			if (this.revertItem(player.getItemInHand()))
				this.getLog().message(sender, "The item {%1$s} has had its lore removed.", player.getItemInHand().getType().name());
			else
				this.getLog().error(sender, "The item {%1$s} has no lore.", player.getItemInHand().getType().name());
		} else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
			for (ItemStack item : player.getInventory().getContents())
				this.revertItem(item);

			this.getLog().message(sender, "All items in your inventory have had their removed.");
		} else
			this.showUsage(sender);
	}

}