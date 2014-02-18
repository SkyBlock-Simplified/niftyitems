package net.netcoding.niftyitems.commands;

import static net.netcoding.niftyitems.managers.Cache.Log;
import static net.netcoding.niftyitems.managers.Cache.Settings;

import java.sql.SQLException;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftyitems.managers.InventoryWorkaround;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Item extends BukkitCommand {

	public Item(JavaPlugin plugin) {
		super(plugin, "item");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void command(CommandSender sender, String[] args) throws SQLException, Exception {
		if (sender instanceof Player) {
			if (args.length > 0) {
				Player player = (Player)sender;
				ItemStack item;

				try {
					item = NiftyBukkit.getItemDatabase().get(args[0]);

					if (item.getType() == Material.AIR) {
						Log.error(sender, "Air cannot be spawned");
						return;
					}
				} catch (Exception ex) {
					Log.error(sender, "{%1$s} is an invalid item name", args[0]);
					return;
				}

				String displayName = item.getType().toString().replace('_', ' ');
				boolean canBypass = player.hasPermission("niftyitems.bypass.spawning." + item.getType().getId());

				if (!canBypass) {
					if (!sender.hasPermission("niftyitems.item")) {
						sender.sendMessage(this.getCommand().getPermissionMessage().replace("<permission>",  "niftyitems.item"));
						return;
					}

					if (Settings.isBlacklisted(player, item, "spawning")) {
						Log.error(sender, Settings.getLocalization("blacklisted", "spawned"), displayName);
						return;
					}
				}

				try {
					if (args.length > 1 && Integer.parseInt(args[1]) > 0)
						item.setAmount(Integer.parseInt(args[1]));
					else {
						if (item.getType().isBlock()) {
							if (Settings.getDefaultBlockStackSize() > 0)
								item.setAmount(Settings.getDefaultBlockStackSize());
							else if (Settings.getOversizedStackSize() > 0 && sender.hasPermission("niftyitems.bypass.stacksize"))
								item.setAmount(Settings.getOversizedStackSize());
						} else {
							if (Settings.getDefaultItemStackSize() > 0)
								item.setAmount(Settings.getDefaultItemStackSize());
						}
					}
				} catch (NumberFormatException ex) {
					Log.error(sender, "{%1$s} is not a number", args[1]);
					return;
				}

				if (args.length > 2) {
					// TODO: Enchantments...
				}

				boolean nolore     = player.hasPermission("niftyitems.bypass.lore");
				if (!nolore) item  = Settings.loreItem(player, item, Settings.getLore("spawned"));

				if (sender.hasPermission("niftyitems.bypass.stacksize"))
					InventoryWorkaround.addOversizedItems(player.getInventory(), Settings.getOversizedStackSize(), item);
				else
					InventoryWorkaround.addItems(player.getInventory(), item);

				Log.message(sender, "Giving {%1$s} of {%2$s}", item.getAmount(), displayName);
				//player.updateInventory();
			} else
				this.showUsage(sender);
		} else
			Log.error(sender, "The command %1$s is not possible from console", this.getCommand().getName());
	}
}