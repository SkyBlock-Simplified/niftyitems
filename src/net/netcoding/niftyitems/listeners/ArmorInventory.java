package net.netcoding.niftyitems.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.netcoding.niftybukkit.inventory.FakeInventoryListener;
import net.netcoding.niftybukkit.inventory.events.InventoryClickEvent;
import net.netcoding.niftybukkit.inventory.events.InventoryCloseEvent;
import net.netcoding.niftybukkit.inventory.events.InventoryItemInteractEvent;
import net.netcoding.niftybukkit.inventory.events.InventoryOpenEvent;
import net.netcoding.niftybukkit.minecraft.BukkitHelper;
import net.netcoding.niftycore.util.ListUtil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ArmorInventory extends BukkitHelper implements FakeInventoryListener {

	public ArmorInventory(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		if (!this.hasPermissions(event.getProfile(), "invsee", "modify")) {
			event.setCancelled(true);
			return;
		}

		if (this.hasPermissions(event.getTarget(), "invsee", "admin")) {
			if (!this.hasPermissions(event.getProfile(), "invsee", "modify", "admin")) {
				event.setCancelled(true);
				return;
			}
		}
		
		Player opener = event.getProfile().getOfflinePlayer().getPlayer();
		Player target = event.getTarget().getOfflinePlayer().getPlayer();
		List<ItemStack> stack = Arrays.asList(target.getInventory().getArmorContents());
		ItemStack clicked = event.getClickedItem(false);

		if (stack.contains(clicked)) {
			if (event.getRawSlot() < event.getInventory().getSize()) {
				if (event.getRawSlot() > 3) {
					event.setCancelled(true);
					return;
				}

				Map<Integer, List<Material>> match = new HashMap<>();
				match.put(0, Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLD_HELMET, Material.DIAMOND_HELMET));
				match.put(1, Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.DIAMOND_CHESTPLATE));
				match.put(2, Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLD_LEGGINGS, Material.DIAMOND_LEGGINGS));
				match.put(3, Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLD_BOOTS, Material.DIAMOND_BOOTS));

				if (match.get(event.getRawSlot()).contains(clicked.getType()))
					stack.set(event.getRawSlot(), clicked);
				else if (event.getRawSlot() == 1) {
					try {
						ItemStack previous = opener.getInventory().getHelmet();
						opener.getInventory().setHelmet(clicked);
						opener.getInventory().setHelmet(previous);
						stack.set(event.getRawSlot(), clicked);
					} catch (Exception ex) {
						event.setCancelled(true);
						return;
					}
				}
			} else
				stack.remove(clicked);
	
			target.getInventory().setArmorContents(ListUtil.toArray(stack, ItemStack.class));
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) { }

	@Override
	public void onInventoryOpen(InventoryOpenEvent event) { }

	@Override
	public void onInventoryItemInteract(InventoryItemInteractEvent event) { }

}