package net.netcoding.nifty.items.listeners;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.Event;
import net.netcoding.nifty.common.api.inventory.FakeInventory;
import net.netcoding.nifty.common.api.inventory.FakeInventoryInstance;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.event.EventResult;
import net.netcoding.nifty.common.minecraft.event.inventory.InventoryClickEvent;
import net.netcoding.nifty.common.minecraft.event.inventory.InventoryCreativeEvent;
import net.netcoding.nifty.common.minecraft.event.inventory.PrepareItemCraftEvent;
import net.netcoding.nifty.common.minecraft.inventory.InventoryType;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.common.mojang.MinecraftMojangProfile;
import net.netcoding.nifty.items.managers.LoreType;
import net.netcoding.nifty.items.NiftyItems;
import net.netcoding.nifty.items.managers.Lore;

import java.util.Arrays;
import java.util.List;

public class Inventory extends MinecraftListener {

	public Inventory(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Event(priority = Event.Priority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player)event.getWhoClicked();
		MinecraftMojangProfile profile = Nifty.getMojangRepository().searchByPlayer(player);

		if (FakeInventory.isOpenAnywhere(profile))
			return;
		else {
			if (NiftyItems.getFakeArmorInventory().isTargeted(profile)) {
				MinecraftMojangProfile targeter = NiftyItems.getFakeArmorInventory().getTargeter(profile);
				FakeInventoryInstance instance = NiftyItems.getFakeArmorInventory().newInstance(targeter);
				ItemStack[] armorContents = player.getInventory().getArmorContents().clone();

				for (int i = 0; i < ArmorInventory.ArmorIndex.size(); i++) {
					ItemStack itemStack = armorContents[i];
					instance.add((itemStack == null ? null : itemStack));
				}

				instance.open(profile);
			}
		}

		/**
		 * InventoryInteractEvent
		 * InventoryDragEvent
		 * InventoryClickEvent
		 * InventoryPickupItemEvent
		 * PlayerItemConsumeEvent
		 * PlayerItemBreakEvent
		 * PlayerPickupItemEvent
		 * PlayerDropItemEvent
		 */

		if (!this.hasPermissions(player, "bypass", "lore")) {
			InventoryType invType = event.getInventory().getType();
			ItemStack item = FakeInventory.getClickedItem(event, false);
			List<InventoryType> allowed = Arrays.asList(InventoryType.CREATIVE, InventoryType.PLAYER, InventoryType.ENDER_CHEST);

			if (LoreType.SPAWNED == Lore.isRestricted(item) && NiftyItems.getPluginConfig().isBlacklisted(player, item, "store")) {
				if (!allowed.contains(invType)) {
					if (event.getClick().isShiftClick()) {
						event.setResult(EventResult.DENY);
						event.setCancelled(true);
					} else if (event.getRawSlot() < event.getInventory().getSize()) {
						event.getInventory().setItem(event.getRawSlot(), ItemStack.of(Material.AIR));
						player.getInventory().addItem(item);
						item.setAmount(0);
						event.setCursor(ItemStack.of(Material.AIR));
						player.updateInventory();
					}
				}
			}
		}
	}

	@Event(priority = Event.Priority.HIGH, ignoreCancelled = true)
	public void onInventoryCreative(InventoryCreativeEvent event) {
		Player player = (Player)event.getWhoClicked();
		MinecraftMojangProfile profile = Nifty.getMojangRepository().searchByPlayer(player);
		ItemStack item = event.getCursor();

		if (FakeInventory.isOpenAnywhere(profile))
			return;

		if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "creative")) {
			if (!NiftyItems.getPluginConfig().isSilent("creative"))
				this.getLog().error(player, "You cannot take {{0}} out of the creative menu!", Nifty.getItemDatabase().name(item));

			event.setCursor(ItemStack.of(Material.AIR));
			event.setCancelled(true);
			event.setResult(EventResult.DENY);
		} else if (!this.hasPermissions(player, "bypass", "lore")) {
			Lore.apply(player, item, LoreType.CREATIVE);
			event.setCursor(item);
		}
	}

	@Event(ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		if (event.getView().getPlayer() instanceof Player) {
			Player player = (Player)event.getView().getPlayer();

			if (!this.hasPermissions(player, "bypass", "lore")) {
				boolean lored = false;

				for (ItemStack item : event.getInventory().getMatrix()) {
					if (LoreType.CREATIVE == Lore.isRestricted(item)) {
						Lore.apply(player, event.getInventory().getItem(0), LoreType.CREATIVE);
						lored = true;
						break;
					}
				}

				if (!lored) {
					for (ItemStack item : event.getInventory().getMatrix()) {
						if (LoreType.SPAWNED == Lore.isRestricted(item)) {
							Lore.apply(player, event.getInventory().getItem(0), LoreType.SPAWNED);
							break;
						}
					}
				}
			}
		}
	}

}