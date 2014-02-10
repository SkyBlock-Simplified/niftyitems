package net.netcoding.niftyitems.commands;

import static net.netcoding.niftyitems.managers.Cache.Log;
import static net.netcoding.niftyitems.managers.Cache.Settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearLore extends BukkitCommand {

	public ClearLore(JavaPlugin plugin) {
		super(plugin, "clearlore", false);
	}

	private void revertItem(ItemStack item) {
		if (!Settings.isRestricted(item).equalsIgnoreCase("none") && item.getItemMeta().hasLore()) {
			ItemMeta itemMeta = item.getItemMeta();
			List<String> lores = itemMeta.getLore();
			List<String> notOurs = new ArrayList<>();

			for (String lore : lores) {
				if (!lore.startsWith(Settings.getLore("creative")) && !lore.startsWith(Settings.getLore("spawned")))
					notOurs.add(lore);
			}

			itemMeta.setLore(notOurs);
			item.setItemMeta(itemMeta);
		}
	}

	@Override
	public void command(CommandSender sender, String[] args) throws SQLException, Exception {
		if (sender instanceof Player) {
			Player player = (Player)sender;

			if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("hand"))) {
				this.revertItem(player.getItemInHand());
				Log.message(sender, "The item {%1$s} has had its lore removed", player.getItemInHand().getType().toString().toLowerCase().replace('_', ' '));
			} else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
				for (ItemStack item : player.getInventory().getContents())
					this.revertItem(item);

				Log.message(sender, "All items in your inventory have had their removed");
			} else
				super.showUsage(sender);
		} else
			Log.error(sender, "The command %1$s is not possible from console", super.getCommand().getName());
	}

}