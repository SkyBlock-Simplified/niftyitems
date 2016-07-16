package net.netcoding.nifty.items.listeners;

import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.Event;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.GameMode;
import net.netcoding.nifty.common.minecraft.block.Action;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.event.player.PlayerDeathEvent;
import net.netcoding.nifty.common.minecraft.event.player.PlayerDropItemEvent;
import net.netcoding.nifty.common.minecraft.event.player.PlayerGameModeChangeEvent;
import net.netcoding.nifty.common.minecraft.event.player.PlayerInteractEvent;
import net.netcoding.nifty.common.minecraft.event.player.PlayerPickupItemEvent;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.items.managers.Lore;
import net.netcoding.nifty.items.managers.LoreType;
import net.netcoding.nifty.items.NiftyItems;

public class Players extends MinecraftListener {

	public Players(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Event(priority = Event.Priority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		for (ItemStack item : event.getDrops()) {
			if (NiftyItems.getPluginConfig().preventAllDrops(player, item) || (NiftyItems.getPluginConfig().preventSpawnedDrops(player, item) && Lore.isRestricted(item) == LoreType.SPAWNED))
				item.setAmount(0);
			else if (NiftyItems.getPluginConfig().destroyAllDrops(player, item) || (NiftyItems.getPluginConfig().destroySpawnedDrops(player, item) && Lore.isRestricted(item) == LoreType.SPAWNED))
				item.setAmount(0);
			else if (Lore.isRestricted(item) == LoreType.CREATIVE)
				item.setAmount(0);
		}
	}

	@Event(priority = Event.Priority.HIGH, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();

		if (NiftyItems.getPluginConfig().preventAllDrops(player, item) || (NiftyItems.getPluginConfig().preventSpawnedDrops(player, item) && Lore.isRestricted(item) == LoreType.SPAWNED)) {
			event.setCancelled(true);
			return;
		}

		if (NiftyItems.getPluginConfig().destroyAllDrops(player, item) || (NiftyItems.getPluginConfig().destroySpawnedDrops(player, item) && Lore.isRestricted(item) == LoreType.SPAWNED)) {
			event.getItem().remove();
			item.setAmount(0);
		}
	}

	@Event
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();

		if (GameMode.CREATIVE == player.getGameMode() && GameMode.CREATIVE != event.getNewGameMode()) {
			ItemStack[] items = player.getInventory().getArmorContents();

			for (ItemStack item : items) {
				if (Lore.isRestricted(item) == LoreType.CREATIVE)
					item.setType(Material.AIR); // TODO: Check this
					//item = new ItemStack(Material.AIR);
			}

			player.getInventory().setArmorContents(items);
		}
	}

	@Event
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (Action.PHYSICAL != event.getAction()) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();

			if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "place")) {
				if (!NiftyItems.getPluginConfig().isSilent("place"))
					this.getLog().error(player, "The item {{0}} cannot be used!", item.getType().toString());

				event.setCancelled(true);
			}
		}
	}

	@Event(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		if (!this.hasPermissions(player, "bypass", "lore")) {
			ItemStack item = event.getItem().getItemStack();

			if (Lore.isRestricted(item) == LoreType.CREATIVE) {
				if (!(GameMode.CREATIVE == player.getGameMode() || Lore.isOwner(item, player.getName())))
					event.setCancelled(true);
			}
		}
	}

}