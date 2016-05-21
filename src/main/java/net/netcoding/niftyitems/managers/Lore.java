package net.netcoding.niftyitems.managers;

import net.netcoding.niftybukkit.minecraft.items.ItemData;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Lore {

	public static final String TYPE = "niftyitems.lore.type";
	public static final String OWNER = "niftyitems.lore.owner";

	public static ItemData apply(CommandSender sender, ItemData itemData, LoreType lore) {
		if (LoreType.NONE != Lore.isRestricted(itemData)) return itemData;
		itemData.getNbt().putPath(TYPE, lore.name());
		itemData.getNbt().putPath(OWNER, sender.getName());
		return itemData;
	}

	public static String getOwner(ItemData itemData) {
		return LoreType.NONE != isRestricted(itemData) ? itemData.getNbt().<String>getPath(OWNER) : "";
	}

	public static boolean isOwner(ItemData itemData, String playerName) {
		return getOwner(itemData).equals(playerName);
	}

	public static LoreType isRestricted(ItemData itemData) {
		if (itemData != null) {
			if (Material.AIR != itemData.getType()) {
				if (!itemData.getNbt().isEmpty()) {
					if (itemData.getNbt().containsPath(TYPE)) {
						return LoreType.fromName(itemData.getNbt().<String>getPath(TYPE));
					}
				}
			}
		}

		return LoreType.NONE;
	}

	public static boolean revert(ItemData itemData) {
		if (isRestricted(itemData) != LoreType.NONE) {
			ItemMeta itemMeta = itemData.getItemMeta();
			List<String> newLore = new ArrayList<>();

			for (String lore : itemMeta.getLore()) {
				if (!lore.startsWith("Creative") && !lore.startsWith("Spawned"))
					newLore.add(lore);
			}

			itemMeta.setLore(newLore);
			itemData.setItemMeta(itemMeta);
		}

		return true;
	}

}