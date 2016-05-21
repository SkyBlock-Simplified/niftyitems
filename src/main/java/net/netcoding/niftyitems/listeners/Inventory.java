package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftybukkit.minecraft.inventory.FakeInventory;
import net.netcoding.niftybukkit.minecraft.inventory.FakeInventoryInstance;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.managers.Lore;
import net.netcoding.niftyitems.managers.LoreType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class Inventory extends BukkitListener {

	public Inventory(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player)event.getWhoClicked();
		BukkitMojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer(player);

		if (FakeInventory.isOpenAnywhere(profile))
			return;
		else {
			if (NiftyItems.getFakeArmorInventory().isTargeted(profile)) {
				BukkitMojangProfile targeter = NiftyItems.getFakeArmorInventory().getTargeter(profile);
				FakeInventoryInstance instance = NiftyItems.getFakeArmorInventory().newInstance(targeter);
				ItemStack[] armorContents = player.getInventory().getArmorContents().clone();

				for (int i = 0; i < ArmorInventory.ArmorIndex.size(); i++) {
					ItemStack itemStack = armorContents[i];
					instance.add((itemStack == null ? null : new ItemData(itemStack)));
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
			final ItemData currentItem = FakeInventory.getClickedItem(event, false);
			List<InventoryType> allowed = Arrays.asList(InventoryType.CREATIVE, InventoryType.PLAYER, InventoryType.ENDER_CHEST);

			if (LoreType.SPAWNED == Lore.isRestricted(currentItem) && NiftyItems.getPluginConfig().isBlacklisted(player, currentItem, "store")) {
				if (!allowed.contains(invType)) {
					if (event.getClick().isShiftClick()) {
						event.setResult(Result.DENY);
						event.setCancelled(true);
					} else if (event.getRawSlot() < event.getInventory().getSize()) {
						event.getInventory().setItem(event.getRawSlot(), new ItemStack(Material.AIR));
						player.getInventory().addItem(currentItem);
						currentItem.setAmount(0);
						event.setCursor(new ItemStack(Material.AIR));
						player.updateInventory();
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryCreative(InventoryCreativeEvent event) {
		Player player = (Player)event.getWhoClicked();
		BukkitMojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer(player);
		ItemData itemData = new ItemData(event.getCursor());

		if (FakeInventory.isOpenAnywhere(profile))
			return;

		if (NiftyItems.getPluginConfig().isBlacklisted(player, itemData, "creative")) {
			if (!NiftyItems.getPluginConfig().isSilent("creative"))
				this.getLog().error(player, "You cannot take {{0}} out of the creative menu!", NiftyBukkit.getItemDatabase().name(itemData));

			event.setCursor(new ItemStack(Material.AIR));
			event.setCancelled(true);
			event.setResult(Result.DENY);
		} else if (!this.hasPermissions(player, "bypass", "lore"))
			event.setCursor(Lore.apply(player, itemData, LoreType.CREATIVE));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		if (event.getView().getPlayer() instanceof Player) {
			Player player = (Player)event.getView().getPlayer();

			if (!this.hasPermissions(player, "bypass", "lore")) {
				boolean lored = false;

				for (ItemStack item : event.getInventory().getMatrix()) {
					ItemData itemData = new ItemData(item);

					if (LoreType.CREATIVE == Lore.isRestricted(itemData)) {
						Lore.apply(player, new ItemData(event.getInventory().getItem(0)), LoreType.CREATIVE);
						lored = true;
						break;
					}
				}

				if (!lored) {
					for (ItemStack item : event.getInventory().getMatrix()) {
						ItemData itemData = new ItemData(item);

						if (LoreType.SPAWNED == Lore.isRestricted(itemData)) {
							Lore.apply(player, new ItemData(event.getInventory().getItem(0)), LoreType.SPAWNED);
							break;
						}
					}
				}
			}
		}
	}

}