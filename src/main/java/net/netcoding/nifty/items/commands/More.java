package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.inventory.InventoryWorkaround;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.common.mojang.MinecraftMojangProfile;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.NumberUtil;
import net.netcoding.nifty.items.NiftyItems;
import net.netcoding.nifty.items.cache.Config;

public class More extends MinecraftListener {

	public More(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "more",
			minimumArgs = 0,
			playerOnly = true
	)
	protected void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		MinecraftMojangProfile profile = Nifty.getMojangRepository().searchByPlayer((Player)source);
		Player player = profile.getOfflinePlayer().getPlayer();

		if (player.getItemInHand() == null || Material.AIR == player.getItemInHand().getType()) {
			this.getLog().error(source, "Cannot give more of empty or Air!");
			return;
		}

		ItemStack item = player.getItemInHand();

		if (ListUtil.isEmpty(args) || !NumberUtil.isNumber(args[0]))
			item.setAmount(NiftyItems.getPluginConfig().getBlockStackSize());
		else {
			item.setAmount(Integer.parseInt(args[0]));
			InventoryWorkaround.addOversizedItems(player.getInventory(), Config.MAXIMUM_OVERSTACKED_SIZE, item);
		}

		this.getLog().message(source, "You now have more of {{0}}!", item.getType());
	}

}