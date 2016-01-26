package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.enchantments.EnchantmentData;
import net.netcoding.niftybukkit.inventory.items.ItemData;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Enchant extends BukkitCommand {

	public Enchant(JavaPlugin plugin) {
		super(plugin, "enchant");
		this.setPlayerOnly();
		this.setMinimumArgsLength(0);
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		Player player = (Player)sender;
		ItemStack stack = player.getItemInHand();
		List<String> enchants = new ArrayList<>();
		boolean unsafe = alias.matches("^ue(nchant)?$");

		if (stack == null || Material.AIR == stack.getType()) {
			this.getLog().error(sender, "You must be holding an item in order to list or apply enchantments!");
			return;
		}

		if (unsafe && !this.hasPermissions(sender, "enchant", "unsafe")) {
			this.getLog().error(sender, "You are not allowed to set unsafe enchantments!");
			return;
		}

		if (ListUtil.isEmpty(args)) {
			if (unsafe)
				this.getLog().message(sender, "You can apply any enchant using the {{0}} command! See {{1}}", "/uenchant", "/enchant list");
			else {
				List<EnchantmentData> possibleEnchants = NiftyBukkit.getEnchantmentDatabase().getPossibleEnchants(stack);

				if (ListUtil.isEmpty(possibleEnchants))
					this.getLog().error(sender, "The {0} {{1}} in your hand cannot normally be enchanted!", (stack.getType().isBlock() ? "block" : "item"), stack.getType().name());
				else {
					List<String> names = new ArrayList<>();

					for (EnchantmentData enchData : possibleEnchants) {
						List<String> enchNames = enchData.getNames();

						for (int i = 0; i < (enchNames.size() < 2 ? enchNames.size() : 2); i++)
							names.add(enchNames.get(i));
					}

					if (ListUtil.isEmpty(names))
						this.getLog().error(sender, "Unable to locate any possible enchantments for {{0}}!", stack.getType().name());
					else {
						this.getLog().message(sender, "[{{0}}]", "Possible Enchantments");
						this.getLog().message(sender, "{{0}}", StringUtil.implode(ChatColor.GRAY + ", " + ChatColor.RED, names));
					}
				}
			}
		} else {
			if (args[0].matches("^fake|hidden|glow$")) {
				if (unsafe) {
					stack = ItemData.addGlow(stack);
					player.setItemInHand(stack);
					this.getLog().message(sender, "Your item now glows!");
				} else
					this.getLog().error(sender, "You are not allowed to a set fake enchantment!");
			} else if ("list".equalsIgnoreCase(args[0])) {
				List<String> names = NiftyBukkit.getEnchantmentDatabase().primaryNames();
				this.getLog().message(sender, "[{{0}}]", "All Enchantments");
				this.getLog().message(sender, "{{0}}", StringUtil.implode(ChatColor.GRAY + ", " + ChatColor.RED, names));
			} else {
				for (EnchantmentData data : NiftyBukkit.getEnchantmentDatabase().parse(args)) {
					try {
						if (data.apply(stack, unsafe))
							enchants.add(StringUtil.format("{{0}} at level {{1}}.", data.getName(), data.getUserLevel()));
					} catch (Exception ignore) { }
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
	}

}