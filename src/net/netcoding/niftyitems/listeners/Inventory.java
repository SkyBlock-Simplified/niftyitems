package net.netcoding.niftyitems.listeners;

import java.util.Arrays;
import java.util.List;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.FakeInventory;
import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftybukkit.mojang.MojangProfile;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.managers.Lore;

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

public class Inventory extends BukkitListener {

	public Inventory(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player)event.getWhoClicked();
		MojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer(player);

		if (FakeInventory.isOpenAnywhere(profile)) {
			if (NiftyItems.getFakeArmorInventory().isTargeted(profile)) {
				MojangProfile targeter = NiftyItems.getFakeArmorInventory().getTargeter(profile);
				targeter.getOfflinePlayer().getPlayer().getOpenInventory().getTopInventory().setContents(player.getInventory().getArmorContents());
			}/* else if (NiftyItems.getFakePlayerInventory().isTargeted(profile)) {
				MojangProfile targeter = NiftyItems.getFakePlayerInventory().getTargeter(profile);
				targeter.getOfflinePlayer().getPlayer().getOpenInventory().getTopInventory().setContents(player.getInventory().getContents());
			}*/

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
			return;
		}

		if (!this.hasPermissions(player, "bypass", "lore")) {
			InventoryType invType = event.getInventory().getType();
			final ItemStack currentItem = FakeInventory.getClickedItem(event, false);
			List<InventoryType> allowed = Arrays.asList(InventoryType.CREATIVE, InventoryType.PLAYER, InventoryType.ENDER_CHEST);

			if (Lore.isRestricted(currentItem).equalsIgnoreCase("spawned") && NiftyItems.getPluginConfig().isBlacklisted(player, currentItem, "store")) {
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
		ItemStack item = event.getCursor();

		if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "creative")) {
			if (!NiftyItems.getPluginConfig().isSilent("creative"))
				this.getLog().error(player, "You cannot take {{0}} out of the creative menu!", NiftyBukkit.getItemDatabase().name(item));

			event.setCursor(new ItemStack(Material.AIR));
			event.setCancelled(true);
			event.setResult(Result.DENY);
		} else if (!this.hasPermissions(player, "bypass", "lore"))
			event.setCursor(Lore.apply(player, item, Lore.getLore("creative")));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		if (event.getView().getPlayer() instanceof Player) {
			Player player = (Player)event.getView().getPlayer();

			if (!this.hasPermissions(player, "bypass", "lore")) {
				boolean lored = false;

				for (ItemStack item : event.getInventory().getMatrix()) {
					if (Lore.isRestricted(item).equalsIgnoreCase("creative")) {
						Lore.apply(player, event.getInventory().getItem(0), Lore.getLore("creative"));
						lored = true;
						break;
					}
				}

				if (!lored) {
					for (ItemStack item : event.getInventory().getMatrix()) {
						if (Lore.isRestricted(item).equalsIgnoreCase("spawned")) {
							Lore.apply(player, event.getInventory().getItem(0), Lore.getLore("spawned"));
							break;
						}
					}
				}
			}
		}
	}

}