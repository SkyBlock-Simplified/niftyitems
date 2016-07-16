package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.List;

public class ItemDb extends MinecraftListener {

	public ItemDb(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "itemdb",
			minimumArgs = 0,
			maximumArgs = 1
	)
	public void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		ItemStack stack = null;
		boolean itemHeld = false;

		if (args.length < 1) {
			if (source instanceof Player) {
				itemHeld = true;
				stack = ((Player)source).getItemInHand();
			}

			if (stack == null) {
				this.getLog().error(source, "Not enough arguments");
				return;
			}
		} else {
			try {
				stack = Nifty.getItemDatabase().get(args[0]);
			} catch (Exception ex) {
				this.getLog().error(source, "{{0}} is an invalid item", args[0]);
				return;
			}
		}

		this.getLog().message(source, "Item: {{0}} - {{1}}:{{2}}.", stack.getType().name(), stack.getTypeId(), stack.getDurability());
		List<String> itemNameList = Nifty.getItemDatabase().names(stack);

		if (itemHeld && Material.AIR != stack.getType() && !stack.getType().isBlock()) {
			int maxuses = stack.getType().getMaxDurability();
			int durability = (maxuses - stack.getDurability());

			if (durability > 0)
				this.getLog().message(source, "This tool has {{0}} uses left.", (durability + 1));
		}

		if (itemNameList.size() > 0) {
			String itemNames = StringUtil.implode((ChatColor.GRAY + ", " + ChatColor.RED), itemNameList);
			this.getLog().message(source, "Item aliases: {{0}}.", itemNames);
		}
	}

}