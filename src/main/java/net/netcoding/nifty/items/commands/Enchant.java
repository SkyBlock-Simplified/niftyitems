package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.inventory.item.enchantment.EnchantmentData;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Enchant extends MinecraftListener {

	public Enchant(MinecraftPlugin plugin) {
		super(plugin);
	}

	@Command(name = "enchant",
			playerOnly = true,
			minimumArgs = 0
	)
	protected void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		Player player = (Player)source;
		ItemStack stack = player.getItemInHand();
		List<String> enchants = new ArrayList<>();
		boolean unsafe = alias.matches("^ue(nchant)?$");

		if (stack == null || Material.AIR == stack.getType()) {
			this.getLog().error(source, "You must be holding an item in order to list or apply enchantments!");
			return;
		}

		if (unsafe && !this.hasPermissions(source, "enchant", "unsafe")) {
			this.getLog().error(source, "You are not allowed to set unsafe enchantments!");
			return;
		}

		if (ListUtil.isEmpty(args)) {
			if (unsafe)
				this.getLog().message(source, "You can apply any enchant using the {{0}} command! See {{1}}", "/uenchant", "/enchant list");
			else {
				List<EnchantmentData> possibleEnchants = Nifty.getEnchantmentDatabase().getPossibleEnchants(stack);

				if (ListUtil.isEmpty(possibleEnchants))
					this.getLog().error(source, "The {0} {{1}} in your hand cannot normally be enchanted!", (stack.getType().isBlock() ? "block" : "item"), stack.getType().name());
				else {
					List<String> names = new ArrayList<>();

					for (EnchantmentData enchData : possibleEnchants) {
						List<String> enchNames = enchData.getNames();

						for (int i = 0; i < (enchNames.size() < 2 ? enchNames.size() : 2); i++)
							names.add(enchNames.get(i));
					}

					if (ListUtil.isEmpty(names))
						this.getLog().error(source, "Unable to locate any possible enchantments for {{0}}!", stack.getType().name());
					else {
						this.getLog().message(source, "[{{0}}]", "Possible Enchantments");
						this.getLog().message(source, "{{0}}", StringUtil.implode(ChatColor.GRAY + ", " + ChatColor.RED, names));
					}
				}
			}
		} else {
			if (args[0].matches("^fake|hidden|glow$")) {
				if (unsafe) {
					stack.addGlow();
					player.setItemInHand(stack);
					this.getLog().message(source, "Your item now glows!");
				} else
					this.getLog().error(source, "You are not allowed to a set fake enchantment!");
			} else if ("list".equalsIgnoreCase(args[0])) {
				List<String> names = Nifty.getEnchantmentDatabase().primaryNames();
				this.getLog().message(source, "[{{0}}]", "All Enchantments");
				this.getLog().message(source, "{{0}}", StringUtil.implode(ChatColor.GRAY + ", " + ChatColor.RED, names));
			} else {
				for (EnchantmentData data : Nifty.getEnchantmentDatabase().parse(args)) {
					try {
						if (data.apply(stack, unsafe))
							enchants.add(StringUtil.format("{{0}} at level {{1}}.", data.getName(), data.getUserLevel()));
					} catch (Exception ignore) { }
				}

				if (enchants.size() == 0)
					this.getLog().error(source, "Unable to apply any enchants, please check your previous command!");
				else {
					this.getLog().message(source, "Your {{0}} has been given the following enchantments:", Nifty.getItemDatabase().name(stack));

					for (String enchant : enchants)
						this.getLog().message(source, enchant);
				}
			}
		}
	}

}