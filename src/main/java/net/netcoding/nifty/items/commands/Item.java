package net.netcoding.nifty.items.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.NumberUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.items.NiftyItems;
import net.netcoding.nifty.items.cache.Config;
import net.netcoding.nifty.items.managers.Lore;
import net.netcoding.nifty.items.managers.LoreType;

import java.util.Arrays;
import java.util.Map;

public class Item extends MinecraftListener {

	public Item(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "item",
			checkPerms = false,
			usages = {
					@Command.Usage(index = 0, match = "g(ive)?", replace = "<player> <type>[:datavalue] [amount] [itemdata]")
			}
	)
	public void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		boolean isGive = alias.matches("^g(ive)?$");
		boolean isItem = !isGive;
		MinecraftMojangProfile receiver;
		String name = isGive ? args[0] : source.getName();

		if (!isGive && isConsole(source)) {
			this.getLog().error(source, "You cannot use the item command from console!");
			return;
		}

		if (isGive && !this.hasPermissions(source, "item", "give")) {
			this.getLog().error(source, "You are not allowed to give items!");
			return;
		}

		try {
			receiver = Nifty.getMojangRepository().searchByUsername(name);
		} catch (ProfileNotFoundException pnfex) {
			this.getLog().error(source, "Unable to locate profile for {{0}}!", name);
			return;
		}

		if (isGive || !receiver.getName().equals(source.getName())) {
			args = StringUtil.split("__", StringUtil.implode("__", args, 1));

			if (args.length == 0) {
				this.showUsage(source);
				return;
			}
		}

		ItemStack item;
		Player player = receiver.getOfflinePlayer().getPlayer();

		try {
			item = Nifty.getItemDatabase().get(args[0]);

			if (Material.AIR == item.getType()) {
				this.getLog().error(source, "You cannot spawn {0}!", "AIR");
				return;
			}
		} catch (Exception ex) {
			this.getLog().error(source, "{{0}} is an invalid item name!", args[0]);
			return;
		}

		String displayName = Nifty.getItemDatabase().name(item);
		displayName = StringUtil.isEmpty(displayName) ? item.getType().name() : displayName;
		args = StringUtil.split("__", StringUtil.implode("__", args, 1));
		int amount = -1;

		if (isItem || (NiftyItems.getPluginConfig().giveEnforcesBlacklist() && !this.hasPermissions(source, "bypass", "give"))) {
			if (NiftyItems.getPluginConfig().isBlacklisted(player, item, "spawned") || (!this.hasPermissions(player, "item") && !NiftyItems.getPluginConfig().hasBypass(player, item, "spawned"))) {
				if (!NiftyItems.getPluginConfig().isSilent("spawned"))
					this.getLog().error(source, "You cannot spawn {{0}}{1}!", displayName, (isGive ? StringUtil.format(" for {{0}}", player.getName()) : ""));

				return;
			}
		}

		if (args.length > 0) {
			if (NumberUtil.isNumber(args[0])) {
				amount = Integer.parseInt(args[0]);
				args = StringUtil.split("__", StringUtil.implode("__", args, 1));
			}
		}

		if (amount < 0)
			amount = item.getType().isBlock() ? NiftyItems.getPluginConfig().getBlockStackSize() : NiftyItems.getPluginConfig().getItemStackSize();

		item.setAmount(amount);

		if (!this.hasPermissions(source, "bypass", "lore"))
			Lore.apply(source, item, LoreType.SPAWNED);

		if (args.length > 0 && this.hasPermissions(source, "item", "nbt")) {
			System.out.println("NBT Args: " + Arrays.toString(args));
			String json = StringUtil.implode(" ", args, 0);
			System.out.println("NBT Json: " + json);

			try {
				Map<String, Object> attributes = new Gson().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
				item.getNbt().putAll(attributes);
			} catch (Exception ex) {
				this.getLog().error(source, "Ignoring invalid NBT json: {{0}}.", json);
			}
		}

		if (this.hasPermissions(source, "bypass", "stacksize"))
			InventoryWorkaround.addOversizedItems(player.getInventory(), Config.MAXIMUM_OVERSTACKED_SIZE, item);
		else
			InventoryWorkaround.addItems(player.getInventory(), item);

		this.getLog().message(source, "Giving {{0}} of {{1}}.", item.getAmount(), displayName);
	}
}