package net.netcoding.niftyitems.commands;

import java.util.List;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftycore.util.StringUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDb extends BukkitCommand {

	public ItemDb(JavaPlugin plugin) {
		super(plugin, "itemdb");
		this.setMinimumArgsLength(0);
		this.setMaximumArgsLength(1);
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		ItemStack stack = null;
		boolean itemHeld = false;

		if (args.length < 1) {
			if (sender instanceof Player) {
				itemHeld = true;
				stack = ((Player)sender).getItemInHand();
			}

			if (stack == null) {
				this.getLog().error(sender, "Not enough arguments");
				return;
			}
		} else {
			try {
				stack = NiftyBukkit.getItemDatabase().get(args[0]);
			} catch (Exception ex) {
				this.getLog().error(sender, "{{0}} is an invalid item", args[0]);
				return;
			}
		}

		this.getLog().message(sender, "Item: {{0}} - {{1}}:{{2}}.", stack.getType().name(), stack.getTypeId(), stack.getDurability());
		List<String> itemNameList = NiftyBukkit.getItemDatabase().names(stack);

		if (itemHeld && Material.AIR != stack.getType() && !stack.getType().isBlock()) {
			int maxuses = stack.getType().getMaxDurability();
			int durability = (maxuses - stack.getDurability());

			if (durability > 0)
				this.getLog().message(sender, "This tool has {{0}} uses left.", (durability + 1));
		}

		if (itemNameList.size() > 0) {
			String itemNames = StringUtil.implode((ChatColor.GRAY + ", " + ChatColor.RED), itemNameList);
			this.getLog().message(sender, "Item aliases: {{0}}.", itemNames);
		}
	}

}