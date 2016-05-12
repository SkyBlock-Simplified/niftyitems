package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.minecraft.inventory.InventoryWorkaround;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.NumberUtil;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.cache.Config;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class More extends BukkitCommand {

	public More(JavaPlugin plugin) {
		super(plugin, "more");
		this.setMinimumArgsLength(0);
		this.setPlayerOnly();
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		BukkitMojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
		Player player = profile.getOfflinePlayer().getPlayer();

		if (player.getItemInHand() == null || Material.AIR == player.getItemInHand().getType()) {
			this.getLog().error(sender, "Cannot give more of empty or Air!");
			return;
		}

		ItemStack itemStack = player.getItemInHand();

		if (ListUtil.isEmpty(args) || !NumberUtil.isInt(args[0]))
			itemStack.setAmount(NiftyItems.getPluginConfig().getBlockStackSize());
		else {
			ItemData itemData = new ItemData(itemStack);
			itemData.setAmount(Integer.parseInt(args[0]));
			InventoryWorkaround.addOversizedItems(player.getInventory(), Config.MAXIMUM_OVERSTACKED_SIZE, itemData);
		}

		this.getLog().message(sender, "You now have more of {{0}}!", itemStack.getType());
	}

}