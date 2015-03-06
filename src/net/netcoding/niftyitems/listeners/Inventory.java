package net.netcoding.niftyitems.listeners;

import static net.netcoding.niftyitems.cache.Cache.Config;
import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.FakeInventory;
import net.netcoding.niftybukkit.minecraft.BukkitListener;
import net.netcoding.niftyitems.cache.Cache;
import net.netcoding.niftyitems.managers.Lore;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
					if (Lore.isRestricted(item).equalsIgnoreCase("spawned"))
						chest.getInventory().removeItem(item);
				}
			}
		} catch (Exception ex) { }
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();

		if (!this.hasPermissions(player, "bypass", "lore")) {
			if (Lore.isRestricted(item).equalsIgnoreCase("creative")) {
				if (!(player.getGameMode() == GameMode.CREATIVE || Lore.isOwner(item, player.getName()))) {
					this.getLog().error(player, "To place {{0}} you must be the owner or in creative mode!", item.getType().toString());
					event.setCancelled(true);
				}
			}
		}

		if (Config.isBlacklisted(player, item, "placement")) {
			this.getLog().error(player, "The item/block {{0}} cannot be placed/used!", item.getType().toString());
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		final Player player = (Player)event.getWhoClicked();
		if (FakeInventory.isOpenAnywhere(NiftyBukkit.getMojangRepository().searchByPlayer(player))) return;

		if (!this.hasPermissions(player, "bypass", "lore")) {
			InventoryType invType = event.getInventory().getType();
			final ItemStack currentItem = FakeInventory.getClickedItem(event, false);

			if (Lore.isRestricted(currentItem).equalsIgnoreCase("spawned") && Cache.Config.isBlacklisted(player, currentItem, "store")) {
				if (!(InventoryType.CREATIVE.equals(invType) || InventoryType.PLAYER.equals(invType))) {
					if (event.getClick().isShiftClick()) {
						final int amount = currentItem.getAmount();

						this.getPlugin().getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
							@Override
							public void run() {
								for (ItemStack items : event.getInventory().getContents()) {
									if (items != null && items.isSimilar(currentItem)) {
										if (items.getAmount() == amount)
											event.getInventory().removeItem(items);
										else if (items.getAmount() > amount)
											items.setAmount(items.getAmount() - amount);

										ItemStack in = new ItemStack(currentItem.getType(), amount, currentItem.getDurability());
										if (Lore.isRestricted(items).equalsIgnoreCase("spawned")) in = Lore.apply(player, in, Lore.getLore("spawned"));
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

		if (Config.isBlacklisted(player, item, "creative")) {
			this.getLog().error(player, "You cannot take {{0}} out of the creative menu!", item.getType().toString());
			event.setCursor(new ItemStack(Material.AIR));
			event.setCancelled(true);
		} else {
			if (!this.hasPermissions(player, "bypass", "lore"))
				event.setCursor(Lore.apply(player, item, Lore.getLore("creative")));
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();

		for (ItemStack item : e.getDrops()) {
			if (Lore.isRestricted(item).equalsIgnoreCase("spawned") && Cache.Config.isBlacklisted(player, item, "store"))
				item.setAmount(0);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemDrop().getItemStack();

		if (Config.destroyAllDrops() || (Config.destroySpawnedDrops() && Lore.isRestricted(item).equalsIgnoreCase("spawned"))) {
			event.getItemDrop().remove();
			item.setAmount(0);
		} else if (Lore.isRestricted(item).equalsIgnoreCase("spawned")) {
			if (Config.isBlacklisted(player, item, "store"))
				event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();

		if (GameMode.CREATIVE.equals(player.getGameMode()) && GameMode.CREATIVE.equals(event.getNewGameMode())) {
			ItemStack[] items = player.getInventory().getArmorContents();

			for (ItemStack item : items) {
				if (!Lore.isRestricted(item).equalsIgnoreCase("none"))
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

			if (Cache.Config.isBlacklisted(player, item, "placement")) {
				this.getLog().error(player, "The item/block {{0}} cannot be used/placed!", item.getType().toString());
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