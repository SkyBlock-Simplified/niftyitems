package net.netcoding.nifty.items.commands;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.Command;
import net.netcoding.nifty.common.api.plugin.MinecraftListener;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.common.mojang.MinecraftMojangProfile;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.RegexUtil;

public class Nbt extends MinecraftListener {

	public Nbt(MinecraftPlugin plugin) {
		super(plugin);
	}

	private static String filter(String value) {
		return RegexUtil.replace(value, RegexUtil.VANILLA_PATTERN, "&$1");
	}

	@Command(name = "nbt",
			playerOnly = true,
			minimumArgs = 0
	)
	protected void onCommand(CommandSource source, String alias, String[] args) throws Exception {
		MinecraftMojangProfile profile = Nifty.getMojangRepository().searchByPlayer((Player)source);
		Player player = profile.getOfflinePlayer().getPlayer();

		if (player.getItemInHand() == null || Material.AIR == player.getItemInHand().getType()) {
			this.getLog().error(source, "Empty or AIR blocks have no NBT!");
			return;
		}

		ItemStack item = player.getItemInHand();

		if (item.getNbt().notEmpty()) {
			if (ListUtil.notEmpty(args)) {
				String key = args[0];

				if (item.getNbt().containsKey(key))
					this.getLog().message(source, "NBT key {{0}}: {{1}}.", key, filter(item.getNbt().get(key).toString()));
				else if (item.getNbt().containsPath(key))
					this.getLog().message(source, "NBT path {{0}}: {{1}}.", key, filter(item.getNbt().getPath(key).toString()));
				else
					this.getLog().error(source, "NBT key {{0}} does not exist!", key);
			} else
				this.getLog().message(source, "All NBT: {{0}}.", filter(item.getNbt().toString()));
		} else
			this.getLog().message(source, "Item contains no NBT.");
	}

}