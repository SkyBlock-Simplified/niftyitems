package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.minecraft.BukkitHelper;
import net.netcoding.niftybukkit.minecraft.inventory.FakeInventoryListener;
import net.netcoding.niftybukkit.minecraft.inventory.events.InventoryClickEvent;
import net.netcoding.niftybukkit.minecraft.inventory.events.InventoryCloseEvent;
import net.netcoding.niftybukkit.minecraft.inventory.events.InventoryItemInteractEvent;
import net.netcoding.niftybukkit.minecraft.inventory.events.InventoryOpenEvent;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftybukkit.reflection.MinecraftProtocol;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentList;
import net.netcoding.niftycore.util.concurrent.ConcurrentMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorInventory extends BukkitHelper implements FakeInventoryListener {

	private static Map<ArmorIndex, List<Material>> ALLOWED = new HashMap<>();

	static {
		ALLOWED.put(ArmorIndex.BOOTS, new ArrayList<>(Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLD_BOOTS, Material.DIAMOND_BOOTS)));
		ALLOWED.put(ArmorIndex.LEGGINGS, new ArrayList<>(Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLD_LEGGINGS, Material.DIAMOND_LEGGINGS)));
		ALLOWED.put(ArmorIndex.CHESTPLATE, new ArrayList<>(Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.DIAMOND_CHESTPLATE)));
		ALLOWED.put(ArmorIndex.HELMET, new ArrayList<>(Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLD_HELMET, Material.DIAMOND_HELMET)));

		if (MinecraftProtocol.getCurrentProtocol() >= MinecraftProtocol.v1_9_pre1.getProtocol())
			ALLOWED.get(ArmorIndex.CHESTPLATE).add(Material.ELYTRA);
	}

	public ArmorInventory(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		if (!this.hasPermissions(event.getProfile(), "invsee", "modify")) {
			event.setCancelled(true);
			return;
		}

		if (this.hasPermissions(event.getTarget(), "invsee", "admin")) {
			if (!this.hasPermissions(event.getProfile(), "invsee", "modify", "admin")) {
				event.setCancelled(true);
				return;
			}
		}

		Player opener = event.getProfile().getOfflinePlayer().getPlayer();
		Player target = event.getTarget().getOfflinePlayer().getPlayer();
		ConcurrentList<ItemStack> contents = new ConcurrentList<>();
		ConcurrentMap<ArmorIndex, Boolean> valid = new ConcurrentMap<>();
		ArmorIndex clickedIndex = ArmorIndex.fromIndex(event.getRawSlot());
		ItemData clicked = event.getClickedItem(false);
		int validCount = 0;

		for (int i = 0; i < ArmorIndex.size(); i++)
			contents.add(opener.getOpenInventory().getTopInventory().getContents()[i]);

		if (event.getRawSlot() >= ArmorIndex.size() && event.getRawSlot() < opener.getOpenInventory().getTopInventory().getSize()) {
			event.setCancelled(true);
			return;
		}

		for (ArmorIndex armorIndex : ArmorIndex.values()) {
			ItemStack itemStack = contents.get(armorIndex.getIndex());
			valid.put(armorIndex, (itemStack == null || ALLOWED.get(armorIndex).contains(itemStack.getType())));

			if (ArmorIndex.HELMET == armorIndex && !valid.get(armorIndex)) {
				if (this.hasPermissions(event.getProfile(), "invsee", "modify", "head")) {
					try {
						target.getInventory().setHelmet(clicked);
						valid.put(armorIndex, true);
					} catch (Exception ignore) { }
				}
			}
		}

		for (ArmorIndex armorIndex : ArmorIndex.values()) {
			if (valid.get(armorIndex))
				validCount++;
		}

		if (validCount == ArmorIndex.size()) {
			if (event.getRawSlot() < ArmorIndex.size())
				contents.set(clickedIndex.getIndex(), clicked);

			target.getInventory().setArmorContents(ListUtil.toArray(contents, ItemStack.class));
		} else
			event.setCancelled(true);
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) { }

	@Override
	public void onInventoryOpen(InventoryOpenEvent event) { }

	@Override
	public void onInventoryItemInteract(InventoryItemInteractEvent event) { }

	public enum ArmorIndex {

		BOOTS(0),
		LEGGINGS(1),
		CHESTPLATE(2),
		HELMET(3);

		private final int index;

		ArmorIndex(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static ArmorIndex fromIndex(int index) {
			for (ArmorIndex armorIndex : values()) {
				if (armorIndex.getIndex() == index)
					return armorIndex;
			}

			return ArmorIndex.HELMET;
		}

		public static int size() {
			return values().length;
		}

	}

}