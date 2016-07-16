package net.netcoding.nifty.items.listeners;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.Event;
import net.netcoding.nifty.common.api.nbt.NbtCompound;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.GameMode;
import net.netcoding.nifty.common.minecraft.block.Block;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.event.block.BlockBreakEvent;
import net.netcoding.nifty.common.minecraft.event.block.BlockDispenseEvent;
import net.netcoding.nifty.common.minecraft.event.block.BlockPlaceEvent;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.items.commands.BlockMask;
import net.netcoding.nifty.items.managers.Lore;
import net.netcoding.nifty.items.managers.LoreType;
import net.netcoding.nifty.items.NiftyItems;

import java.util.Map;

public class Blocks extends MinecraftListener {

	public Blocks(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Event(priority = Event.Priority.HIGH, ignoreCancelled = true)
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
			if (Lore.isRestricted(item) == LoreType.SPAWNED) {
				if (NiftyItems.getPluginConfig().preventSpawnedDrops(player, item)) {
					if (!NiftyItems.getPluginConfig().isSilent("break"))
						this.getLog().error(player, "Unable to break {{0}}! Spawned items cannot be dropped, please remove them first!", item.getType().toString());

					return;
				}
			}
		}

		// Destroy Spawned Drops
		block.getDrops().stream().filter(item -> Lore.isRestricted(item) == LoreType.SPAWNED).filter(item -> NiftyItems.getPluginConfig().destroySpawnedDrops(player, item)).forEach(item -> item.setType(Material.AIR));

		// Destroy All Blocks
		block.getDrops().stream().filter(item -> NiftyItems.getPluginConfig().destroyAllDrops(player, item)).forEach(item -> item.setType(Material.AIR));
	}

	@Event(priority = Event.Priority.HIGH, ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent event) {
		if (NiftyItems.getPluginConfig().destroyAllDrops() || (NiftyItems.getPluginConfig().destroySpawnedDrops() && Lore.isRestricted(event.getItemStack()) == LoreType.SPAWNED)) {
			event.setItem(ItemStack.of(Material.AIR));
			event.setCancelled(true);
		}
	}

	@Event(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();

		if (item.getNbt().containsPath(BlockMask.BLOCKMASK_KEY))
			item = Nifty.getItemDatabase().get(item.getNbt().getPath(BlockMask.BLOCKMASK_KEY));

		if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "place")) {
			if (!NiftyItems.getPluginConfig().isSilent("place"))
				this.getLog().error(player, "You cannot {0} {{1}}!", (item.getType().isBlock() ? "place" : "use"), item.getType().toString());

			event.setCancelled(true);
			return;
		}

		if (!this.hasPermissions(player, "bypass", "lore")) {
			if (Lore.isRestricted(item) == LoreType.CREATIVE) {
				if (!(GameMode.CREATIVE == player.getGameMode() || Lore.isOwner(item, player.getName()))) {
					if (!NiftyItems.getPluginConfig().isSilent("place"))
						this.getLog().error(player, "You must be in creative mode or the owner to place {{0}}!", item.getType().toString());

					event.setCancelled(true);
				}
			}
		}

	}

	@Event(priority = Event.Priority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceMask(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		final ItemStack item = player.getItemInHand();

		if (item.getNbt().containsPath(BlockMask.BLOCKMASK_KEY)) {
			Nifty.getScheduler().schedule(() -> {
				ItemStack mask = Nifty.getItemDatabase().get(item.getNbt().getPath(BlockMask.BLOCKMASK_KEY));
				event.getBlock().setTypeIdAndData(mask.getTypeId(), (byte)mask.getDurability(), false);

				if (item.getNbt().containsPath(BlockMask.BLOCKMASK_DATA)) {
					try {
						Map<String, Object> map = item.getNbt().getPath(BlockMask.BLOCKMASK_DATA);
						NbtCompound compound = Nifty.getNbtFactory().fromBlockTag(event.getBlock());
						compound.putAll(map);
					} catch (Exception ignore) { }
				}
			});
		}
	}

}