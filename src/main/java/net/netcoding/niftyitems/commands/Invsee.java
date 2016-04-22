package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.minecraft.inventory.FakeInventoryInstance;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftyitems.NiftyItems;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Invsee extends BukkitCommand {

	public Invsee(JavaPlugin plugin) {
		super(plugin, "invsee");
		this.setPlayerOnly();
		this.setPlayerTabComplete();
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		BukkitMojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
		BukkitMojangProfile target;

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

		String action = args.length > 1 ? args[1] : "inventory";
		Player profilePlayer = profile.getOfflinePlayer().getPlayer();
		Player targetPlayer = target.getOfflinePlayer().getPlayer();

		if (action.matches("^inv(entory)?$"))
			profilePlayer.openInventory(targetPlayer.getInventory()); // Todo, maybe
        else if (action.matches("^(ender)?chest$"))
			profilePlayer.openInventory(targetPlayer.getEnderChest());
		else if (action.matches("^armou?r$")) {
			FakeInventoryInstance instance = NiftyItems.getFakeArmorInventory().newInstance(profile);
			instance.setTitle(StringUtil.format("Equipment of {0}", profile.getName()));
			ItemStack[] armorContents = targetPlayer.getInventory().getArmorContents();

			for (int i = 0; i < armorContents.length; i++) {
				if (armorContents[i] != null)
					instance.add(i, armorContents[i]);
			}

			instance.open(target);
		} else
            this.showUsage(sender);
	}

}