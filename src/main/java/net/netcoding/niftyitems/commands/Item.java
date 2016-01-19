package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.InventoryWorkaround;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.niftycore.util.NumberUtil;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftyitems.NiftyItems;
import net.netcoding.niftyitems.cache.Config;
import net.netcoding.niftyitems.managers.Lore;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Item extends BukkitCommand {

	public Item(JavaPlugin plugin) {
		super(plugin, "item");
		this.setCheckPerms(false);
		this.setCheckHelp(false);
		this.editUsage(0, "give", "<player> <type>[:datavalue] [amount]");
		this.editUsage(0, "g", "<player> <type>[:datavalue] [amount]");
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		boolean isGive = alias.matches("^g(ive)?$");
		boolean isItem = !isGive;
		BukkitMojangProfile receiver;

		if (!isGive && isConsole(sender)) {
			this.getLog().error(sender, "You cannot use the item command from console!");
			return;
		}

		if (isGive && !this.hasPermissions(sender, "item", "give")) {
			this.getLog().error(sender, "You are not allowed to give items!");
			return;
		}

		try {
			receiver = NiftyBukkit.getMojangRepository().searchByUsername(isGive ? args[0] : sender.getName());
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(sender, "Unable to locate profile for {{0}}!", args[0]);
			return;
		}

		if (isGive || !receiver.getName().equals(sender.getName())) {
			args = StringUtil.split(",", StringUtil.implode(",", args, 1));

			if (args.length == 0) {
				this.showUsage(sender);
				return;
			}
		}

		ItemStack stack;
		Player player = receiver.getOfflinePlayer().getPlayer();

		try {
			stack = NiftyBukkit.getItemDatabase().get(args[0]);

			if (Material.AIR == stack.getType()) {
				this.getLog().error(sender, "You cannot spawn {0}!", "AIR");
				return;
			}
		} catch (Exception ex) {
			this.getLog().error(sender, "{{0}} is an invalid item name!", args[0]);
			return;
		}

		String displayName = NiftyBukkit.getItemDatabase().name(stack);
		displayName = StringUtil.isEmpty(displayName) ? stack.getType().name() : displayName;
		args = StringUtil.split(",", StringUtil.implode(",", args, 1));
		int amount = -1;

		if (isItem || (NiftyItems.getPluginConfig().giveEnforcesBlacklist() && !this.hasPermissions(sender, "bypass", "give"))) {
			if (NiftyItems.getPluginConfig().isBlacklisted(player, stack, "spawned") || (!this.hasPermissions(player, "item") && !NiftyItems.getPluginConfig().hasBypass(player, stack, "spawned"))) {
				if (!NiftyItems.getPluginConfig().isSilent("spawned"))
					this.getLog().error(sender, "You cannot spawn {{0}}{1}!", displayName, (isGive ? StringUtil.format(" for {{0}}", player.getName()) : ""));

				return;
			}
		}

		if (args.length > 0) {
			if (NumberUtil.isInt(args[0])) {
				amount = Integer.parseInt(args[0]);
				args = StringUtil.split(",", StringUtil.implode(",", args, 1));
			}
		}

		if (amount < 0)
			amount = stack.getType().isBlock() ? NiftyItems.getPluginConfig().getBlockStackSize() : NiftyItems.getPluginConfig().getItemStackSize();

		stack.setAmount(amount);
		if (!this.hasPermissions(sender, "bypass", "lore")) stack = Lore.apply(sender, stack, Lore.getLore("spawned"));

		if (args.length > 0 && this.hasPermissions(sender, "item", "data")) {
			// TODO: Item Data
		}

		if (this.hasPermissions(sender, "bypass", "stacksize"))
			InventoryWorkaround.addOversizedItems(player.getInventory(), Config.MAXIMUM_OVERSTACKED_SIZE, stack);
		else
			InventoryWorkaround.addItems(player.getInventory(), stack);

		this.getLog().message(sender, "Giving {{0}} of {{1}}.", stack.getAmount(), displayName);
	}
}