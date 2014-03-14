package net.netcoding.niftyitems.listeners;

import static net.netcoding.niftyitems.managers.Cache.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.netcoding.niftybukkit.minecraft.BukkitListener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Inventory extends BukkitListener {

	public Inventory(JavaPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		try {
			InventoryType.valueOf(event.getBlock().getType().name());

			if (event.getBlock() instanceof Chest) {
				Chest chest = (Chest)event.getBlock().getState();

				for (ItemStack item : chest.getInventory().getContents()) {
					if (Settings.isRestricted(item).equalsIgnoreCase("spawned"))
						chest.getInventory().removeItem(item);
				}
			}
		} catch (Exception ex) { }
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();

		if (!player.hasPermission("niftyitems.bypass.lore")) {
			if (Settings.isRestricted(item).equalsIgnoreCase("creative")) {
				if (!(player.getGameMode() == GameMode.CREATIVE || Settings.isOwner(item, player.getName()))) {
					this.getLog().error(player, "To place {%1$s} you must be the owner or in creative mode", item.getType().toString());
					event.setCancelled(true);
				}
			}
		}

		if (Settings.isBlacklisted(player, item, "placement")) {
			this.getLog().error(player, Settings.getLocalization("blacklisted", "placement"), item.getType().toString());
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		final Player player = (Player)event.getWhoClicked();

		if (!player.hasPermission("niftyitems.bypass.lore")) {
			InventoryType invType = event.getInventory().getType();
			final ItemStack currentItem = event.getClick().isShiftClick() ? event.getCurrentItem() : event.getCursor();

			if (Settings.isRestricted(currentItem).equalsIgnoreCase("spawned") && Settings.isBlacklisted(player, currentItem, "store")) {
				List<InventoryType> inventorys = new ArrayList<InventoryType>(
					Arrays.asList(
						InventoryType.CHEST,
						InventoryType.ENDER_CHEST,
						InventoryType.FURNACE,
						InventoryType.DISPENSER,
						InventoryType.DROPPER,
						InventoryType.HOPPER
					)
				);

				if (inventorys.contains(invType)) {
					if (event.getClick().isShiftClick()) {
						// TODO: COOLDOWN
						final int amount = currentItem.getAmount();

						this.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(this.getPlugin(), new Runnable() {
							@Override
							public void run() {
								for (ItemStack items : event.getInventory().getContents()) {
									if (items != null && items.isSimilar(currentItem)) {
										if (items.getAmount() == amount)
											event.getInventory().removeItem(items);
										else if (items.getAmount() > amount)
											items.setAmount(items.getAmount() - amount);

										ItemStack in = new ItemStack(currentItem.getType(), amount, currentItem.getDurability());
										if (Settings.isRestricted(items).equalsIgnoreCase("spawned")) in = Settings.loreItem(player, in, Settings.getLore("spawned"));
										player.getInventory().addItem(in);
										break;
									}
								}
							}
						}, 2L);
					} else {
						if (event.getRawSlot() < event.getInventory().getSize()) {
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
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryCreative(InventoryCreativeEvent event) {
		Player player = (Player)event.getWhoClicked();
		ItemStack item = event.getCursor();

		if (Settings.isBlacklisted(player, item, "creative")) {
			this.getLog().error(player, Settings.getLocalization("blacklisted", "creative"), item.getType().toString());
			event.setCursor(new ItemStack(Material.AIR));
			event.setCancelled(true);
		} else {
			if (!player.hasPermission("niftyitems.bypass.lore"))
				event.setCursor(Settings.loreItem(player, item, Settings.getLore("creative")));
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();

		for (ItemStack item : e.getDrops()) {
			if (Settings.isRestricted(item).equalsIgnoreCase("spawned") && Settings.isBlacklisted(player, item, "store"))
				item.setAmount(0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player   = event.getPlayer();
		boolean destroy = Settings.destroyAllDrops();
		ItemStack item  = event.getItemDrop().getItemStack();

		if (destroy || Settings.isRestricted(item).equalsIgnoreCase("spawned")) {
			if (destroy || Settings.destroySpawnedDrops() || (Settings.isBlacklisted(player, item, "store") && Settings.destroySpawnedDrops())) {
				event.getItemDrop().remove();
				item.setAmount(0);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE && event.getNewGameMode() != GameMode.CREATIVE) {
			ItemStack[] items = player.getInventory().getArmorContents();

			for (ItemStack item : items) {
				if (!Settings.isRestricted(item).equalsIgnoreCase("none"))
					item = new ItemStack(Material.AIR);
			}

			player.getInventory().setArmorContents(items);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();

			if (Settings.isBlacklisted(player, item, "placement")) {
				this.getLog().error(player, Settings.getLocalization("blacklisted", "placement"), item.getType().toString());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		if (!player.hasPermission("niftyitems.bypass.lore")) {
			ItemStack item = event.getItem().getItemStack();

			if (Settings.isRestricted(item).equalsIgnoreCase("creative")) {
				if (!(player.getGameMode() == GameMode.CREATIVE || Settings.isOwner(item, player.getName())))
					event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		if (event.getView().getPlayer() instanceof Player) {
			Player player = (Player)event.getView().getPlayer();

			if (!player.hasPermission("niftyitems.bypass.lore")) {
				boolean lored = false;

				for (ItemStack item : event.getInventory().getMatrix()) {
					if (Settings.isRestricted(item).equalsIgnoreCase("creative")) {
						Settings.loreItem(player, event.getInventory().getItem(0), Settings.getLore("creative"));
						lored = true;
						break;
					}
				}

				if (!lored) {
					for (ItemStack item : event.getInventory().getMatrix()) {
						if (Settings.isRestricted(item).equalsIgnoreCase("spawned")) {
							Settings.loreItem(player, event.getInventory().getItem(0), Settings.getLore("spawned"));
							break;
						}
					}
				}
			}
		}
	}
}