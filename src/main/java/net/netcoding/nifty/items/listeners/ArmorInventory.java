package net.netcoding.nifty.items.listeners;

import net.netcoding.nifty.common.api.inventory.FakeInventoryListener;
import net.netcoding.nifty.common.api.inventory.events.FakeInventoryClickEvent;
import net.netcoding.nifty.common.api.inventory.events.FakeInventoryCloseEvent;
import net.netcoding.nifty.common.api.inventory.events.FakeInventoryOpenEvent;
import net.netcoding.nifty.common.api.inventory.events.FakeItemInteractEvent;
import net.netcoding.nifty.common.api.plugin.MinecraftHelper;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.common.reflection.MinecraftProtocol;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;
import net.netcoding.nifty.core.util.concurrent.ConcurrentMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArmorInventory extends MinecraftHelper implements FakeInventoryListener {

	private static final ConcurrentMap<ArmorIndex, List<Material>> ALLOWED = Concurrent.newMap();

	static {
		ALLOWED.put(ArmorIndex.BOOTS, new ArrayList<>(Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLD_BOOTS, Material.DIAMOND_BOOTS)));
		ALLOWED.put(ArmorIndex.LEGGINGS, new ArrayList<>(Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLD_LEGGINGS, Material.DIAMOND_LEGGINGS)));
		ALLOWED.put(ArmorIndex.CHESTPLATE, new ArrayList<>(Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.DIAMOND_CHESTPLATE)));
		ALLOWED.put(ArmorIndex.HELMET, new ArrayList<>(Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLD_HELMET, Material.DIAMOND_HELMET)));

		if (MinecraftProtocol.getCurrentProtocol() >= MinecraftProtocol.v1_9_pre1.getProtocol())
			ALLOWED.get(ArmorIndex.CHESTPLATE).add(Material.ELYTRA);
	}

	public ArmorInventory(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onInventoryClick(FakeInventoryClickEvent event) {
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
		ConcurrentList<ItemStack> contents = Concurrent.newList();
		ConcurrentMap<ArmorIndex, Boolean> valid = Concurrent.newMap();
		ArmorIndex clickedIndex = ArmorIndex.fromIndex(event.getRawSlot());
		ItemStack clicked = event.getPlacedItem();
		int validCount = 0;
		//this.getLog().console("Test: {0}:{1}:{2}", event.getRawSlot(), event.getSlot(), clicked);

		for (int i = 0; i < ArmorIndex.size(); i++)
			contents.add(opener.getOpenInventory().getTopInventory().getContents()[i]);

		if (event.getRawSlot() >= ArmorIndex.size() && event.getRawSlot() < opener.getOpenInventory().getTopInventory().getSize()) {
			event.setCancelled(true);
			return;
		}

		for (ArmorIndex armorIndex : ArmorIndex.values()) {
			ItemStack itemStack = contents.get(armorIndex.getIndex());

			if (armorIndex.getIndex() == event.getRawSlot())
				valid.put(armorIndex, ALLOWED.get(armorIndex).contains(clicked.getType()));
			else
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
			//ItemStack itemStack = contents.get(armorIndex.getIndex());
			//itemStack = (armorIndex.getIndex() == event.getRawSlot() ? clicked : itemStack);
			//this.getLog().console("Armor: {0}:{1}:{2}", armorIndex.toString(), itemStack, valid.get(armorIndex));

			if (valid.get(armorIndex))
				validCount++;
		}

		this.getLog().console("Valid: {0}:{1}", validCount, ArmorIndex.size());
		if (validCount == ArmorIndex.size()) {
			if (event.getRawSlot() < ArmorIndex.size())
				contents.set(clickedIndex.getIndex(), clicked);

			this.getLog().console("Armor Set Contents: {0}", contents);
			target.getInventory().setArmorContents(ListUtil.toArray(contents, ItemStack.class));
		} else
			event.setCancelled(true);
		this.getLog().console("Armor Cancelled: {0}", event.isCancelled());
	}

	@Override
	public void onInventoryClose(FakeInventoryCloseEvent event) { }

	@Override
	public void onInventoryOpen(FakeInventoryOpenEvent event) { }

	@Override
	public void onItemInteract(FakeItemInteractEvent event) { }

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