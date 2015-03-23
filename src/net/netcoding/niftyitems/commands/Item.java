package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.InventoryWorkaround;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.managers.Lore;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Item extends BukkitCommand {

	public Item(JavaPlugin plugin) {
		super(plugin, "item");
		this.setPlayerOnly();
		this.setCheckPerms(false);
		this.setCheckHelp(false);
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		Player player = (Player)sender;
		ItemStack item;

		try {
			item = NiftyBukkit.getItemDatabase().get(args[0]);

			if (Material.AIR.equals(item.getType())) {
				this.getLog().error(sender, "You cannot spawn {0}!", "AIR");
				return;
			}
		} catch (Exception ex) {
			this.getLog().error(sender, "{{0}} is an invalid item name!", args[0]);
			return;
		}

		String displayName = item.getType().toString().replace('_', ' ');

		if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "spawned") || (!this.hasPermissions(sender, "item") && !NiftyItems.getPluginConfig().hasBypass(player, item, "spawned"))) {
			this.getLog().error(sender, "You cannot spawn {{0}}!", displayName);
			return;
		}

		try {
			if (args.length > 1 && Integer.parseInt(args[1]) > 0)
				item.setAmount(Integer.parseInt(args[1]));
			else {
				if (item.getType().isBlock()) {
					if (NiftyItems.getPluginConfig().getBlockStackSize() > 0)
						item.setAmount(NiftyItems.getPluginConfig().getBlockStackSize());
					else if (NiftyItems.getPluginConfig().getOversizedStackSize() > 0 && this.hasPermissions(sender, "bypass", "stacksize"))
						item.setAmount(NiftyItems.getPluginConfig().getOversizedStackSize());
				} else {
					if (NiftyItems.getPluginConfig().getItemStackSize() > 0)
						item.setAmount(NiftyItems.getPluginConfig().getItemStackSize());
				}
			}
		} catch (NumberFormatException ex) {
			this.getLog().error(sender, "{{0}} is not a number!", args[1]);
			return;
		}

		if (args.length > 2) {
			// TODO: Enchantments...
		}

		boolean nolore = this.hasPermissions(sender, "bypass", "lore");
		if (!nolore) item = Lore.apply(player, item, Lore.getLore("spawned"));

		if (this.hasPermissions(sender, "bypass", "stacksize"))
			InventoryWorkaround.addOversizedItems(player.getInventory(), NiftyItems.getPluginConfig().getOversizedStackSize(), item);
		else
			InventoryWorkaround.addItems(player.getInventory(), item);

		this.getLog().message(sender, "Giving {{0}} of {{1}}.", item.getAmount(), displayName);
	}
}