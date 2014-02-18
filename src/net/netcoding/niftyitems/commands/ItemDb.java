package net.netcoding.niftyitems.commands;

import static net.netcoding.niftyitems.managers.Cache.Log;

import java.sql.SQLException;
import java.util.List;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.utilities.StringUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDb extends BukkitCommand {

	public ItemDb(JavaPlugin plugin) {
		super(plugin, "itemdb", false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void command(CommandSender sender, String[] args) throws SQLException, Exception {
		ItemStack stack = null;
		boolean itemHeld = false;

		if (args.length < 1) {
			if (sender instanceof Player) {
				itemHeld = true;
				stack = ((Player)sender).getItemInHand();
			}

			if (stack == null) {
				Log.error(sender, "Not enough arguments");
				return;
			}
		} else {
			try {
				stack = NiftyBukkit.getItemDatabase().get(args[0]);
			} catch (Exception ex) {
				Log.error(sender, "{%1$s} is an invalid item", args[0]);
				return;
			}
		}

		Log.message(sender, "Item: {%1$s} - {%2$s}:{%3$s}", stack.getType().toString(), stack.getTypeId(), stack.getDurability());

		if (itemHeld && stack.getType() != Material.AIR) {
			int maxuses = stack.getType().getMaxDurability();
			int durability = ((maxuses + 1) - stack.getDurability());
			Log.message(sender, "This tool has {%1$s} uses left", Integer.toString(durability));
		}

		List<String> itemNameList = NiftyBukkit.getItemDatabase().names(stack);
		String itemNames = StringUtil.implode((ChatColor.GRAY + ", " + ChatColor.RED), itemNameList);
		if (itemNameList != null) Log.message(sender, "Item aliases: {%1$s}", itemNames);
	}

}