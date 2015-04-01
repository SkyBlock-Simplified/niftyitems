package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.managers.Lore;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Blocks extends BukkitListener {

	public Blocks(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		for (ItemStack stack : block.getDrops()) {
			if (NiftyItems.getPluginConfig().isBlacklisted(player, stack, "break")) {
				if (!NiftyItems.getPluginConfig().isSilent("break"))
					this.getLog().error(player, "The block {{0}} cannot be broken!", block.getType().toString());

				event.setCancelled(true);
				return;
			}
		}

		if (NiftyItems.getPluginConfig().destroyAllDrops()) {
			block.setType(Material.AIR);
			event.setCancelled(true);
			return;
		}

		if (event.getBlock() instanceof Chest) {
			Chest chest = (Chest)block.getState();

			for (ItemStack item : chest.getInventory().getContents()) {
				if (Lore.isRestricted(item).equalsIgnoreCase("spawned"))
					chest.getInventory().removeItem(item);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent event) {
		if (NiftyItems.getPluginConfig().destroyAllDrops() || (NiftyItems.getPluginConfig().destroySpawnedDrops() && Lore.isRestricted(event.getItem()).equalsIgnoreCase("spawned"))) {
			event.setItem(new ItemStack(Material.AIR));
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();

		if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "place")) {
			if (!NiftyItems.getPluginConfig().isSilent("place"))
				this.getLog().error(player, "The block {{0}} cannot be placed/used!", item.getType().toString());

			event.setCancelled(true);
			return;
		}

		if (!this.hasPermissions(player, "bypass", "lore")) {
			if (Lore.isRestricted(item).equalsIgnoreCase("creative")) {
				if (!(GameMode.CREATIVE.equals(player.getGameMode()) || Lore.isOwner(item, player.getName()))) {
					this.getLog().error(player, "To place {{0}} you must be the owner or in creative mode!", item.getType().toString());
					event.setCancelled(true);
				}
			}
		}
	}

}