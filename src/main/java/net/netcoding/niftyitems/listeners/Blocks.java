package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.commands.BlockMask;
import net.netcoding.niftyitems.managers.Lore;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
					this.getLog().error(player, "You cannot break {{0}}!", block.getType().toString());

				event.setCancelled(true);
				return;
			}
		}

		// Prevent Spawned Drops
		for (ItemStack stack : block.getDrops()) {
			if (Lore.isRestricted(stack).equalsIgnoreCase("spawned")) {
				if (NiftyItems.getPluginConfig().preventSpawnedDrops(player, stack)) {
					if (!NiftyItems.getPluginConfig().isSilent("break"))
						this.getLog().error(player, "Unable to break {{0}}! Spawned items cannot be dropped, please remove them first!", stack.getType().toString());

					return;
				}
			}
		}

		// Destroy Spawned Drops
		for (ItemStack stack : block.getDrops()) {
			if (Lore.isRestricted(stack).equalsIgnoreCase("spawned")) {
				if (NiftyItems.getPluginConfig().destroySpawnedDrops(player, stack))
					stack.setType(Material.AIR);
			}
		}

		// Destroy All Blocks
		for (ItemStack stack : block.getDrops()) {
			if (NiftyItems.getPluginConfig().destroyAllDrops(player, stack))
				stack.setType(Material.AIR);
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
		ItemData itemData = new ItemData(event.getItemInHand());

		if (itemData.containsNbtPath(BlockMask.BLOCKMASK_KEY))
			itemData = new ItemData(NiftyBukkit.getItemDatabase().get(itemData.<String>getNbtPath(BlockMask.BLOCKMASK_KEY)));

		if (NiftyItems.getPluginConfig().isBlacklisted(player, itemData, "place")) {
			if (!NiftyItems.getPluginConfig().isSilent("place"))
				this.getLog().error(player, "You cannot {0} {{1}}!", (itemData.getType().isBlock() ? "place" : "use"), itemData.getType().toString());

			event.setCancelled(true);
			return;
		}

		if (!this.hasPermissions(player, "bypass", "lore")) {
			if (Lore.isRestricted(itemData).equalsIgnoreCase("creative")) {
				if (!(GameMode.CREATIVE == player.getGameMode() || Lore.isOwner(itemData, player.getName()))) {
					if (!NiftyItems.getPluginConfig().isSilent("place"))
						this.getLog().error(player, "You must be in creative mode or the owner to place {{0}}!", itemData.getType().toString());

					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlaceMask(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		ItemData itemData = new ItemData(player.getItemInHand());

		if (itemData.containsNbtPath(BlockMask.BLOCKMASK_KEY)) {
			ItemData maskData = new ItemData(NiftyBukkit.getItemDatabase().get(itemData.<String>getNbtPath(BlockMask.BLOCKMASK_KEY)));
			event.getBlock().setType(maskData.getType());
			event.getBlock().setData(itemData.<Byte>getNbtPath(BlockMask.BLOCKMASK_DATA));
		}
	}

}