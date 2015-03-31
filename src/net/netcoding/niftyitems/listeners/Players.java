package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.managers.Lore;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Players extends BukkitListener {

	public Players(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		for (ItemStack item : event.getDrops()) {
			if (NiftyItems.getPluginConfig().destroyAllDrops() || (NiftyItems.getPluginConfig().destroySpawnedDrops() && Lore.isRestricted(item).equalsIgnoreCase("spawned"))) {
				item.setAmount(0);
			} else if (Lore.isRestricted(item).equalsIgnoreCase("spawned")) {
				if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "store"))
					item.setAmount(0);
			} else if (Lore.isRestricted(item).equalsIgnoreCase("creative"))
				item.setAmount(0);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemDrop().getItemStack();

		if (NiftyItems.getPluginConfig().destroyAllDrops() || (NiftyItems.getPluginConfig().destroySpawnedDrops() && Lore.isRestricted(item).equalsIgnoreCase("spawned"))) {
			event.getItemDrop().setItemStack(new ItemStack(Material.AIR));
			event.getItemDrop().remove();
			item.setAmount(0);
			event.setCancelled(true);
		} else if (Lore.isRestricted(item).equalsIgnoreCase("spawned")) {
			if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "store"))
				event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();

		if (GameMode.CREATIVE.equals(player.getGameMode()) && !GameMode.CREATIVE.equals(event.getNewGameMode())) {
			ItemStack[] items = player.getInventory().getArmorContents();

			for (ItemStack item : items) {
				if (Lore.isRestricted(item).equalsIgnoreCase("creative"))
					item = new ItemStack(Material.AIR);
			}

			player.getInventory().setArmorContents(items);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (Action.RIGHT_CLICK_AIR.equals(event.getAction()) || Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();

			if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "placement")) {
				this.getLog().error(player, "The {0} {{1}} cannot be used/placed!", (item.getType().isBlock() ? "block" : "item"), item.getType().toString());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		if (!this.hasPermissions(player, "bypass", "lore")) {
			ItemStack item = event.getItem().getItemStack();

			if (Lore.isRestricted(item).equalsIgnoreCase("creative")) {
				if (!(GameMode.CREATIVE.equals(player.getGameMode()) || Lore.isOwner(item, player.getName())))
					event.setCancelled(true);
			}
		}
	}

}