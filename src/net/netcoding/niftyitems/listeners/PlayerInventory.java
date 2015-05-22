package net.netcoding.niftyitems.listeners;

import java.util.Arrays;
import java.util.List;

import net.netcoding.niftybukkit.inventory.FakeInventoryListener;
import net.netcoding.niftybukkit.inventory.events.InventoryClickEvent;
import net.netcoding.niftybukkit.inventory.events.InventoryCloseEvent;
import net.netcoding.niftybukkit.inventory.events.InventoryItemInteractEvent;
import net.netcoding.niftybukkit.inventory.events.InventoryOpenEvent;
import net.netcoding.niftybukkit.minecraft.BukkitHelper;
import net.netcoding.niftycore.util.ListUtil;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerInventory extends BukkitHelper implements FakeInventoryListener {

	public PlayerInventory(JavaPlugin plugin) {
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

		Player target = event.getTarget().getOfflinePlayer().getPlayer();
		List<ItemStack> stack = Arrays.asList(event.getInventory().getContents());
		ItemStack clicked = event.getClickedItem(false);

		if (stack.contains(clicked)) {
			if (event.getRawSlot() < event.getInventory().getSize())
				stack.set(event.getRawSlot(), clicked);
			else
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