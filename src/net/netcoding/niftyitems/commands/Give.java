package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.InventoryWorkaround;
import net.netcoding.niftybukkit.inventory.enchantments.EnchantmentData;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.util.NumberUtil;
import net.netcoding.niftybukkit.util.StringUtil;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.cache.Config;
import net.netcoding.niftyitems.managers.Lore;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Give extends BukkitCommand {

	public Give(JavaPlugin plugin) {
		super(plugin, "item");
		this.setCheckPerms(false);
		this.setCheckHelp(false);
		this.editUsage(0, "item", "<type>[:datavalue] [amount] [enchantments]");
		this.editUsage(0, "i", "<type>[:datavalue] [amount] [enchantments]");
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		boolean isGive = alias.matches("^g(ive)?$");
		boolean isItem = alias.matches("^i(tem)?$");

		if (isGive && findPlayer(args[0]) == null) {
			this.getLog().error(sender, "You must pass the player name when giving items!");
			return;
		} else if (isItem && findPlayer(args[0]) != null)
			args = StringUtil.implode(",", args, 1, args.length - 1).split(",");

		String user = (isGive ? args[0] : sender.getName());
		Player player = findPlayer(user);
		if (isGive) args = StringUtil.implode(",", args, 1, args.length - 1).split(",");
		ItemStack stack;

		if (isGive && !sender.getName().equalsIgnoreCase(player.getName()) && !this.hasPermissions(sender, "item", "give")) {
			this.getLog().error(sender, "You are not allowed to give items!");
			return;
		}

		if (isConsole(user)) {
			this.getLog().error(sender, "You cannot give the console items!");
			return;
		}

		try {
			stack = NiftyBukkit.getItemDatabase().get(args[0]);

			if (Material.AIR.equals(stack.getType())) {
				this.getLog().error(sender, "You cannot spawn {0}!", "AIR");
				return;
			}
		} catch (Exception ex) {
			this.getLog().error(sender, "{{0}} is an invalid item name!", args[0]);
			return;
		}

		String displayName = NiftyBukkit.getItemDatabase().name(stack);
		args = StringUtil.implode(",", args, 1, args.length - 1).split(",");
		int amount = 1;

		if (isItem || (NiftyItems.getPluginConfig().giveEnforcesBlacklist() && !this.hasPermissions(sender, "bypass", "give"))) {
			if (NiftyItems.getPluginConfig().isBlacklisted(player, stack, "spawned") || (!this.hasPermissions(player, "item") && !NiftyItems.getPluginConfig().hasBypass(player, stack, "spawned"))) {
				if (!NiftyItems.getPluginConfig().isSilent("spawned"))
					this.getLog().error(sender, "You cannot spawn {{0}}{1}!", displayName, (isGive ? StringUtil.format(" for {{0}}", player.getName()) : ""));

				return;
			}
		}

		if (args.length == 0)
			amount = stack.getType().isBlock() ? NiftyItems.getPluginConfig().getBlockStackSize() : NiftyItems.getPluginConfig().getItemStackSize();
		else if (NumberUtil.isInt(args[0])) {
			int passedAmount = Integer.parseInt(args[0]);
			amount = passedAmount < 0 ? amount : passedAmount;
		}

		stack.setAmount(amount);
		args = StringUtil.implode(",", args, 1, args.length - 1).split(",");
		if (!this.hasPermissions(sender, "bypass", "lore")) stack = Lore.apply(sender, stack, Lore.getLore("spawned"));

		if (args.length > 1) {
			for (EnchantmentData data : NiftyBukkit.getEnchantmentDatabase().parse(args))
				data.apply(stack);
		}

		if (this.hasPermissions(sender, "bypass", "stacksize"))
			InventoryWorkaround.addOversizedItems(player.getInventory(), Config.MAXIMUM_OVERSTACKED_SIZE, stack);
		else
			InventoryWorkaround.addItems(player.getInventory(), stack);

		this.getLog().message(sender, "Giving {{0}} of {{1}}.", stack.getAmount(), displayName);
	}
}