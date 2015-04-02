package net.netcoding.niftyitems.managers;

import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftybukkit.util.ListUtil;
import net.netcoding.niftybukkit.util.RegexUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Lore {

	public static ItemStack apply(CommandSender sender, ItemStack stack, String lore) {
		if (!Lore.isRestricted(stack).equalsIgnoreCase("none")) return stack;

		// Fix ItemMeta
		ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(stack.getType());
		itemMeta = stack.hasItemMeta() && stack.getItemMeta() != null ? stack.getItemMeta() : itemMeta;

		// Fix Lore
		List<String> lores = new ArrayList<>();
		lores = itemMeta.hasLore() && ListUtil.notEmpty(itemMeta.getLore()) ? itemMeta.getLore() : lores;

		lores.add(0, String.format("%s%s%s | %s", ChatColor.DARK_GRAY, ChatColor.ITALIC, lore, sender.getName()));
		itemMeta.setLore(lores);
		stack.setItemMeta(itemMeta);
		return stack;
	}

	public static String getLore(String type) {
		return ("creative".equals(type) ? "Creative" : ("spawned".equals(type) ? "Spawned" : null));
	}

	public static String getOwner(ItemStack stack) {
		if (isRestricted(stack).equalsIgnoreCase("none")) return "";
		String lore = "none";

		if (!(lore = isRestricted(stack)).equalsIgnoreCase("none")) {
			List<String> lores = stack.getItemMeta().getLore();
			String localized = getLore(lore);

			for (String cLore : lores) {
				String cLoref = RegexUtil.strip(cLore, RegexUtil.VANILLA_PATTERN);

				if (cLoref.startsWith(localized))
					return cLoref.replace(localized + " | ", "");
			}
		}

		return "";
	}

	public static boolean isOwner(ItemStack stack, String playerName) {
		return getOwner(stack) == playerName;
	}

	public static String isRestricted(ItemStack stack) {
		String none = "none";
		if (stack == null || Material.AIR.equals(stack)) return none;
		if (!stack.hasItemMeta() || !stack.getItemMeta().hasLore()) return none;

		for (String lore : stack.getItemMeta().getLore()) {
			String strippedLore = RegexUtil.strip(lore, RegexUtil.VANILLA_PATTERN);

			if (strippedLore.startsWith(getLore("creative")))
				return "creative";
			else if (strippedLore.startsWith(getLore("spawned")))
				return "spawned";
		}

		return none;
	}

	public static boolean revert(ItemStack stack) {
		if (isRestricted(stack).equalsIgnoreCase("none")) return false;
		ItemMeta itemMeta = stack.getItemMeta();
		List<String> newLore = new ArrayList<>();

		for (String lore : itemMeta.getLore()) {
			if (!lore.startsWith(Lore.getLore("creative")) && !lore.startsWith(Lore.getLore("spawned")))
				newLore.add(lore);
		}

		itemMeta.setLore(newLore);
		stack.setItemMeta(itemMeta);
		return true;
	}

}