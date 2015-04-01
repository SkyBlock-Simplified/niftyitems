package net.netcoding.niftyitems.commands;

import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.enchantments.EnchantmentData;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.util.StringUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Enchant extends BukkitCommand {

	public Enchant(JavaPlugin plugin) {
		super(plugin, "enchant");
		this.setPlayerOnly();
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		ItemStack stack = ((Player)sender).getItemInHand();
		List<String> enchants = new ArrayList<>();

		if (alias.matches("^ue(nchant)?$") && !this.hasPermissions(sender, "enchant", "unsafe")) {
			this.getLog().error(sender, "You are not allowed to set unsafe enchantments!");
			return;
		}

		for (EnchantmentData data : NiftyBukkit.getEnchantmentDatabase().parse(args)) {
			try {
				if (data.apply(stack, alias.matches("^ue(nchant)?$")))
					enchants.add(StringUtil.format("{{0}} at level {{1}}.", data.getName(), data.getUserLevel()));
			} catch (Exception ex) { }
		}

		if (enchants.size() == 0)
			this.getLog().error(sender, "Unable to apply any enchants, please check your previous command!");
		else {
			this.getLog().message(sender, "Your {{0}} has been given the following enchantments:", NiftyBukkit.getItemDatabase().name(stack));
			for (String enchant : enchants)
				this.getLog().message(sender, enchant);
		}
	}

}