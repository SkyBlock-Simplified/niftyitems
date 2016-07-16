package net.netcoding.nifty.items.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.inventory.item.meta.ItemMeta;
import net.netcoding.nifty.common.mojang.MinecraftMojangProfile;
import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.Map;

public class BlockMask extends MinecraftListener {

	public static final String BLOCKMASK_KEY = "niftyitems.block.mask";
	public static final String BLOCKMASK_DATA = "niftyitems.block.data";

	public BlockMask(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "blockmask", playerOnly = true, minimumArgs = 0)
	protected void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		MinecraftMojangProfile profile = Nifty.getMojangRepository().searchByPlayer((Player)source);
		Player player = profile.getOfflinePlayer().getPlayer();

		if (player.getItemInHand() == null || !player.getItemInHand().getType().isBlock()) {
			this.getLog().error(source, "Cannot set block mask to empty or non-block items!");
			return;
		}

		ItemStack item = player.getItemInHand();

		if (ListUtil.isEmpty(args) || args[0].matches("^(remov|delet)e$")) {
			if (item.getNbt().containsPath(BLOCKMASK_KEY)) {
				String mask = item.getNbt().getPath(BLOCKMASK_KEY);

				if (StringUtil.notEmpty(mask)) {
					ItemStack previousMaskData = Nifty.getItemDatabase().get(mask);
					mask = StringUtil.format("{{0}}:{{1}}", previousMaskData.getType(), previousMaskData.getDurability());
				}

				item.getNbt().remove("niftyitems");
				ItemMeta itemMeta = item.getItemMeta();
				itemMeta.setDisplayName(null);
				item.setItemMeta(itemMeta);
				player.setItemInHand(item);
				this.getLog().message(source, "The {0} mask has been removed!", mask);
			} else
				this.getLog().error(source, "There is no existing block mask to remove!");
		} else {
			try {
				ItemStack maskData = Nifty.getItemDatabase().get(args[0]);

				if (maskData.getType().isBlock()) {
					String previousMask = item.getNbt().getPath(BLOCKMASK_KEY);

					if (StringUtil.notEmpty(previousMask)) {
						ItemStack previousMaskData = Nifty.getItemDatabase().get(previousMask);
						previousMask = StringUtil.format("{{0}}:{{1}}", previousMaskData.getType(), previousMaskData.getDurability());
					}

					item.getNbt().putPath(BLOCKMASK_KEY, StringUtil.format("{0}:{1}", maskData.getType().name(), maskData.getDurability()));

					if (args.length > 1 && this.hasPermissions(source, "blockmask", "nbt")) {
						String json = StringUtil.implode(" ", args, 1);

						try {
							Map<String, Object> attributes = new Gson().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
							item.getNbt().putPath(BLOCKMASK_DATA, attributes);
						} catch (Exception ex) {
							this.getLog().error(source, "Ignoring invalid NBT json: {{0}}.", json);
						}
					} else if (item.getNbt().containsPath(BLOCKMASK_DATA))
						item.getNbt().removePath(BLOCKMASK_DATA);

					ItemMeta itemMeta = item.getItemMeta();
					itemMeta.setDisplayName(StringUtil.format("{0}Mask{1}: {2}{3}", ChatColor.DARK_AQUA, ChatColor.DARK_GRAY, ChatColor.WHITE, maskData.getType()));
					item.setItemMeta(itemMeta);
					player.setItemInHand(item);
					this.getLog().message(source, "The block in your hand now has a {{0}}:{{1}} block mask!{2}", maskData.getType(), maskData.getDurability(), (StringUtil.notEmpty(previousMask) ? StringUtil.format(" (Previously {0})", previousMask) : ""));
				} else
					this.getLog().error(source, "Cannot set mask to non-block type {{0}}:{{1}}!", maskData.getType(), maskData.getDurability());
			} catch (Exception ex) {
				this.getLog().error(source, "The passed type {{0}} is invalid!", args[0]);
			}
		}
	}

}