package net.netcoding.nifty.items.managers;

import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.common.minecraft.command.CommandSource;

public class Lore {

	public static final String TYPE = "niftyitems.lore.type";
	public static final String OWNER = "niftyitems.lore.owner";

	public static void apply(CommandSource source, ItemStack item, LoreType lore) {
		if (LoreType.NONE != Lore.isRestricted(item)) return;
		item.getNbt().putPath(TYPE, lore.name());
		item.getNbt().putPath(OWNER, source.getName());
	}

	public static String getOwner(ItemStack item) {
		return LoreType.NONE != isRestricted(item) ? item.getNbt().getPath(OWNER) : "";
	}

	public static boolean isOwner(ItemStack item, String playerName) {
		return getOwner(item).equals(playerName);
	}

	public static LoreType isRestricted(ItemStack item) {
		if (item != null) {
			if (Material.AIR != item.getType()) {
				if (item.getNbt().notEmpty()) {
					if (item.getNbt().containsPath(TYPE)) {
						return LoreType.fromName(item.getNbt().getPath(TYPE));
					}
				}
			}
		}

		return LoreType.NONE;
	}

	public static boolean revert(ItemStack item) {
		if (isRestricted(item) != LoreType.NONE) {
			item.getNbt().removePath(TYPE);
			item.getNbt().removePath(OWNER);
			return true;
		}

		return false;
	}

}