package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.minecraft.ChatColor;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class BlockMask extends BukkitCommand {

	public static final String BLOCKMASK_KEY = "niftyitems.blockmask";

	public BlockMask(JavaPlugin plugin) {
		super(plugin, "blockmask");
		this.setPlayerOnly();
		this.setMinimumArgsLength(0);
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		BukkitMojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
		Player player = profile.getOfflinePlayer().getPlayer();

		if (player.getItemInHand() == null || !player.getItemInHand().getType().isBlock()) {
			this.getLog().error(sender, "Cannot set block mask to empty or non-block items!");
			return;
		}

		ItemData itemData = new ItemData(player.getItemInHand());

		if (ListUtil.isEmpty(args) || args[0].matches("^(remov|delet)e$")) {
			if (itemData.containsNbtPath(BLOCKMASK_KEY)) {
				String mask = itemData.getNbtPath(BLOCKMASK_KEY);

				if (StringUtil.notEmpty(mask)) {
					ItemData previousMaskData = NiftyBukkit.getItemDatabase().get(mask);
					mask = StringUtil.format("{{0}}:{{1}}", previousMaskData.getType(), previousMaskData.getDurability());
				}

				itemData.removeNbt("niftyitems");
				ItemMeta itemMeta = itemData.getItemMeta();
				itemMeta.setDisplayName(null);
				itemData.setItemMeta(itemMeta);
				player.setItemInHand(itemData);
				this.getLog().message(sender, "The {0} mask has been removed!", mask);
			} else
				this.getLog().error(sender, "There is no existing block mask to remove!");
		} else {
			try {
				ItemData maskData = NiftyBukkit.getItemDatabase().get(args[0]);

				if (maskData.getType().isBlock()) {
					String previousMask = itemData.getNbtPath(BLOCKMASK_KEY);

					if (StringUtil.notEmpty(previousMask)) {
						ItemData previousMaskData = NiftyBukkit.getItemDatabase().get(previousMask);
						previousMask = StringUtil.format("{{0}}:{{1}}", previousMaskData.getType(), previousMaskData.getDurability());
					}

					itemData.putNbtPath(BLOCKMASK_KEY, StringUtil.format("{0}:{1}", maskData.getType().name(), maskData.getDurability()));
					ItemMeta itemMeta = itemData.getItemMeta();
					itemMeta.setDisplayName(StringUtil.format("{0}Mask{1}: {2}", ChatColor.DARK_AQUA, ChatColor.WHITE, maskData.getType()));
					itemData.setItemMeta(itemMeta);
					player.setItemInHand(itemData);
					this.getLog().message(sender, "The block in your hand now has a {{0}}:{{1}} block mask!{2}", maskData.getType(), maskData.getDurability(), (StringUtil.notEmpty(previousMask) ? StringUtil.format(" (Previously {0})", previousMask) : ""));
				} else
					this.getLog().error(sender, "Cannot set mask to non-block type {{0}}:{{1}}!", maskData.getType(), maskData.getDurability());
			} catch (Exception ex) {
				this.getLog().error(sender, "The passed type {{0}} is invalid!", args[0]);
			}
		}
	}

}