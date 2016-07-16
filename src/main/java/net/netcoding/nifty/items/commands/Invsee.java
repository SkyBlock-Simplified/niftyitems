package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.inventory.FakeInventoryInstance;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.mojang.MinecraftMojangProfile;
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.items.NiftyItems;

public class Invsee extends MinecraftListener {

	public Invsee(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "invsee",
			playerOnly = true,
			playerTabComplete = true
	)
	protected void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		MinecraftMojangProfile profile = Nifty.getMojangRepository().searchByPlayer((Player)source);
		MinecraftMojangProfile target;

		try {
			target = Nifty.getMojangRepository().searchByUsername(args[0]);
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(source, "Unable to locate the profile for {{0}}!", args[0]);
			return;
		}

		if (!target.isOnlineLocally()) {
			this.getLog().error(source, "Unable to lookup inventory of offline player {{0}}!", target.getName());
			return;
		}

		String action = args.length > 1 ? args[1] : "inventory";
		Player profilePlayer = profile.getOfflinePlayer().getPlayer();
		Player targetPlayer = target.getOfflinePlayer().getPlayer();

		if (action.matches("^inv(entory)?$"))
			profilePlayer.openInventory(targetPlayer.getInventory());
        else if (action.matches("^(ender)?chest$"))
			profilePlayer.openInventory(targetPlayer.getEnderChest());
		else if (action.matches("^armou?r$")) {
			FakeInventoryInstance instance = NiftyItems.getFakeArmorInventory().newInstance(profile);
			instance.setTitle(StringUtil.format("Equipment of {0}", target.getName()));
			ItemStack[] armorContents = targetPlayer.getInventory().getArmorContents();

			for (int i = 0; i < armorContents.length; i++) {
				if (armorContents[i] != null)
					instance.add(i, armorContents[i]);
			}

			instance.open(target);
		} else
            this.showUsage(source);
	}

}