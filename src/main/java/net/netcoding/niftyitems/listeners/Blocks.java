package net.netcoding.niftyitems.listeners;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftybukkit.minecraft.nbt.NbtCompound;
import net.netcoding.niftybukkit.minecraft.nbt.NbtFactory;
import net.netcoding.niftycore.minecraft.scheduler.MinecraftScheduler;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.commands.BlockMask;
import net.netcoding.niftyitems.managers.Lore;
import net.netcoding.niftyitems.managers.LoreType;
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

import java.util.Map;

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
		for (ItemStack item : block.getDrops()) {
			ItemData itemData = new ItemData(item);

			if (Lore.isRestricted(itemData) == LoreType.SPAWNED) {
				if (NiftyItems.getPluginConfig().preventSpawnedDrops(player, item)) {
					if (!NiftyItems.getPluginConfig().isSilent("break"))
						this.getLog().error(player, "Unable to break {{0}}! Spawned items cannot be dropped, please remove them first!", item.getType().toString());

					return;
				}
			}
		}

		// Destroy Spawned Drops
		for (ItemStack item : block.getDrops()) {
			ItemData itemData = new ItemData(item);

			if (Lore.isRestricted(itemData) == LoreType.SPAWNED) {
				if (NiftyItems.getPluginConfig().destroySpawnedDrops(player, item))
					item.setType(Material.AIR);
			}
		}

		// Destroy All Blocks
		for (ItemStack item : block.getDrops()) {
			if (NiftyItems.getPluginConfig().destroyAllDrops(player, item))
				item.setType(Material.AIR);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent event) {
		ItemData itemData = new ItemData(event.getItem());

		if (NiftyItems.getPluginConfig().destroyAllDrops() || (NiftyItems.getPluginConfig().destroySpawnedDrops() && Lore.isRestricted(itemData) == LoreType.SPAWNED)) {
			event.setItem(new ItemStack(Material.AIR));
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemData itemData = new ItemData(event.getItemInHand());

		if (itemData.getNbt().containsPath(BlockMask.BLOCKMASK_KEY))
			itemData = new ItemData(NiftyBukkit.getItemDatabase().get(itemData.getNbt().<String>getPath(BlockMask.BLOCKMASK_KEY)));

		if (NiftyItems.getPluginConfig().isBlacklisted(player, itemData, "place")) {
			if (!NiftyItems.getPluginConfig().isSilent("place"))
				this.getLog().error(player, "You cannot {0} {{1}}!", (itemData.getType().isBlock() ? "place" : "use"), itemData.getType().toString());

			event.setCancelled(true);
			return;
		}

		if (!this.hasPermissions(player, "bypass", "lore")) {
			if (Lore.isRestricted(itemData) == LoreType.CREATIVE) {
				if (!(GameMode.CREATIVE == player.getGameMode() || Lore.isOwner(itemData, player.getName()))) {
					if (!NiftyItems.getPluginConfig().isSilent("place"))
						this.getLog().error(player, "You must be in creative mode or the owner to place {{0}}!", itemData.getType().toString());

					event.setCancelled(true);
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceMask(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		final ItemData itemData = new ItemData(player.getItemInHand());

		if (itemData.getNbt().containsPath(BlockMask.BLOCKMASK_KEY)) {
			MinecraftScheduler.schedule(new Runnable() {
				@Override
				public void run() {
					ItemData maskData = new ItemData(NiftyBukkit.getItemDatabase().get(itemData.getNbt().<String>getPath(BlockMask.BLOCKMASK_KEY)));
					event.getBlock().setTypeIdAndData(maskData.getTypeId(), (byte)maskData.getDurability(), false);

					if (itemData.getNbt().containsPath(BlockMask.BLOCKMASK_DATA)) {
						try {
							Map<String, Object> map = itemData.getNbt().getPath(BlockMask.BLOCKMASK_DATA);
							NbtCompound compound = NbtFactory.fromBlockTag(event.getBlock());
							compound.putAll(map);
						} catch (Exception ignore) { }
					}
				}
			});
		}
	}

}