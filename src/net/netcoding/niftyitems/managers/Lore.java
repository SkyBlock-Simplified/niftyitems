package net.netcoding.niftyitems.managers;

import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftybukkit.util.RegexUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Lore {

	public static ItemStack apply(Player player, ItemStack item, String lore) {
		if (isRestricted(item).equalsIgnoreCase("none")) {
			if (item != null && !Material.AIR.equals(item.getType())) {
				ItemMeta itemMeta = item.getItemMeta();
				List<String> lores = new ArrayList<>();

				if (item.getItemMeta().hasLore()) {
					itemMeta = item.getItemMeta();
					lores = itemMeta.getLore();
				}

				lores.add(String.format("%s%s%s | %s", ChatColor.DARK_GRAY, ChatColor.ITALIC, lore, player.getName()));
				itemMeta.setLore(lores);
				item.setItemMeta(itemMeta);
			}
		}

		return item;
	}

	public static String getLore(String type) {
		return ("creative".equals(type) ? "Creative" : ("spawned".equals(type) ? "Spawned" : null));
	}

	public static String getOwner(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
				String lore = "none";

				if (!(lore = isRestricted(item)).equalsIgnoreCase("none")) {
					List<String> lores = item.getItemMeta().getLore();
					String localized = getLore(lore);

					for (String cLore : lores) {
						String cLoref = RegexUtil.strip(cLore, RegexUtil.VANILLA_PATTERN);
						if (cLoref.startsWith(localized)) return cLoref.replace(localized + " | ", "");
					}
				}
			}
		}

		return null;
	}

	public static boolean isOwner(ItemStack item, String playerName) {
		return getOwner(item) == playerName;
	}

	public static String isRestricted(ItemStack item) {
		if (item != null && !Material.AIR.equals(item.getType())) {
			if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
				for (String lore : item.getItemMeta().getLore()) {
					String safeLore = RegexUtil.strip(lore, RegexUtil.VANILLA_PATTERN);

					if (safeLore.startsWith(getLore("creative")))
						return "creative";
					else if (safeLore.startsWith(getLore("spawned")))
						return "spawned";
				}
			}
		}

		return "none";
	}

}