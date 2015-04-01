package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.mojang.MojangProfile;
import net.netcoding.niftybukkit.mojang.exceptions.ProfileNotFoundException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class Invsee extends BukkitCommand {

	public Invsee(JavaPlugin plugin) {
		super(plugin, "invsee");
		this.setPlayerOnly();
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		MojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
		MojangProfile target;

		try {
			target = NiftyBukkit.getMojangRepository().searchByUsername(args[0]);
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(sender, "Unable to locate the profile for {{0}}!", args[0]);
			return;
		}

		if (!target.isOnlineLocally()) {
			this.getLog().error(sender, "Unable to lookup inventory of offline player {{0}}!", target.getName());
			return;
		}

		if (args.length == 1 || !args[1].matches("^inv(entory)?$"))
			profile.getOfflinePlayer().getPlayer().openInventory(target.getOfflinePlayer().getPlayer().getInventory());
		else {
			Inventory inventory = this.getPlugin().getServer().createInventory(target.getOfflinePlayer().getPlayer(), 9, "Equipped");
			inventory.setContents(target.getOfflinePlayer().getPlayer().getInventory().getArmorContents());
			profile.getOfflinePlayer().getPlayer().closeInventory();
			profile.getOfflinePlayer().getPlayer().openInventory(inventory);
			/*FakeInventoryInstance instance = NiftyItems.getFakeArmorInventory().newInstance(profile);
			instance.setTitle("Equipped");
			instance.open(target);*/
		}
	}

}