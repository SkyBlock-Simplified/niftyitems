package net.netcoding.niftyitems.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Lore {

	public static ItemStack apply(Player player, ItemStack item, String lore) {
		if (isRestricted(item).equalsIgnoreCase("none")) {
			if (item != null && item.getType() != Material.AIR) {
				ItemMeta itemMeta = item.getItemMeta();
				List<String> lores = new ArrayList<>();

				if (item.getItemMeta().hasLore()) {
					itemMeta = item.getItemMeta();
					lores = itemMeta.getLore();
				}

				lores.add(lore + " | " + player.getName());
				itemMeta.setLore(lores);
				item.setItemMeta(itemMeta);
			}
		}

		return item;
	}

	public static String getLore(String type) {
		return (type == "creative" ? "Creative" : (type == "spawned" ? "Spawned" : null));
	}

	public static String getOwner(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
				String lore = "none";

				if (!(lore = isRestricted(item)).equalsIgnoreCase("none")) {
					List<String> lores = item.getItemMeta().getLore();
					String localized   = getLore(lore);

					for (String cLore : lores) {
						if (cLore.startsWith(localized))
							return cLore.replace(localized + " | ", "");
					}
				}
			}
		}

		return null;
	}

	public static boolean isOwner(ItemStack item, String playerName) {
		return getOwner(item) == playerName;
	}

	public static String isRestricted(ItemStack i) {
		if (i != null && i.getType() != Material.AIR) {
			if (i.hasItemMeta() && i.getItemMeta().hasLore()) {
				for (String s : i.getItemMeta().getLore()) {
					if (s.startsWith(getLore("creative")))
						return "creative";
					else if (s.startsWith(getLore("spawned")))
						return "spawned";
				}
			}
		}

		return "none";
	}

}