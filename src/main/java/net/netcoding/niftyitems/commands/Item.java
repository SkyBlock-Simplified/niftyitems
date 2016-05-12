package net.netcoding.niftyitems.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.minecraft.inventory.InventoryWorkaround;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Map;

public class Item extends BukkitCommand {

	public Item(JavaPlugin plugin) {
		super(plugin, "item");
		this.setCheckPerms(false);
		this.editUsage(0, "give", "<player> <type>[:datavalue] [amount] [itemdata]");
		this.editUsage(0, "g", "<player> <type>[:datavalue] [amount] [itemdata]");
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		boolean isGive = alias.matches("^g(ive)?$");
		boolean isItem = !isGive;
		BukkitMojangProfile receiver;
		String name = isGive ? args[0] : sender.getName();

		if (!isGive && isConsole(sender)) {
			this.getLog().error(sender, "You cannot use the item command from console!");
			return;
		}

		if (isGive && !this.hasPermissions(sender, "item", "give")) {
			this.getLog().error(sender, "You are not allowed to give items!");
			return;
		}

		try {
			receiver = NiftyBukkit.getMojangRepository().searchByUsername(name);
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(sender, "Unable to locate profile for {{0}}!", name);
			return;
		}

		if (isGive || !receiver.getName().equals(sender.getName())) {
			args = StringUtil.split("__", StringUtil.implode("__", args, 1));

			if (args.length == 0) {
				this.showUsage(sender);
				return;
			}
		}

		ItemData itemData;
		Player player = receiver.getOfflinePlayer().getPlayer();

		try {
			itemData = NiftyBukkit.getItemDatabase().get(args[0]);

			if (Material.AIR == itemData.getType()) {
				this.getLog().error(sender, "You cannot spawn {0}!", "AIR");
				return;
			}
		} catch (Exception ex) {
			this.getLog().error(sender, "{{0}} is an invalid item name!", args[0]);
			return;
		}

		String displayName = NiftyBukkit.getItemDatabase().name(itemData);
		displayName = StringUtil.isEmpty(displayName) ? itemData.getType().name() : displayName;
		args = StringUtil.split("__", StringUtil.implode("__", args, 1));
		int amount = -1;

		if (isItem || (NiftyItems.getPluginConfig().giveEnforcesBlacklist() && !this.hasPermissions(sender, "bypass", "give"))) {
			if (NiftyItems.getPluginConfig().isBlacklisted(player, itemData, "spawned") || (!this.hasPermissions(player, "item") && !NiftyItems.getPluginConfig().hasBypass(player, itemData, "spawned"))) {
				if (!NiftyItems.getPluginConfig().isSilent("spawned"))
					this.getLog().error(sender, "You cannot spawn {{0}}{1}!", displayName, (isGive ? StringUtil.format(" for {{0}}", player.getName()) : ""));

				return;
			}
		}

		if (args.length > 0) {
			if (NumberUtil.isInt(args[0])) {
				amount = Integer.parseInt(args[0]);
				args = StringUtil.split("__", StringUtil.implode("__", args, 1));
			}
		}

		if (amount < 0)
			amount = itemData.getType().isBlock() ? NiftyItems.getPluginConfig().getBlockStackSize() : NiftyItems.getPluginConfig().getItemStackSize();

		itemData.setAmount(amount);

		if (!this.hasPermissions(sender, "bypass", "lore"))
			itemData = Lore.apply(sender, itemData, Lore.getLore("spawned"));

		if (args.length > 0 && this.hasPermissions(sender, "item", "nbt")) {
			System.out.println("NBT Args: " + Arrays.toString(args));
			String json = StringUtil.implode(" ", args, 0);
			System.out.println("NBT Json: " + json);

			try {
				Map<String, Object> attributes = new Gson().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
				itemData.putAllNbt(attributes);
			} catch (Exception ex) {
				this.getLog().error(sender, "Ignoring invalid NBT json: {{0}}.", json);
			}
		}

		if (this.hasPermissions(sender, "bypass", "stacksize"))
			InventoryWorkaround.addOversizedItems(player.getInventory(), Config.MAXIMUM_OVERSTACKED_SIZE, itemData);
		else
			InventoryWorkaround.addItems(player.getInventory(), itemData);

		this.getLog().message(sender, "Giving {{0}} of {{1}}.", itemData.getAmount(), displayName);
	}
}