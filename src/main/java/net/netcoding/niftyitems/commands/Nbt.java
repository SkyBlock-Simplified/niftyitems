package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftybukkit.minecraft.items.ItemData;
import net.netcoding.niftybukkit.mojang.BukkitMojangProfile;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.RegexUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class Nbt extends BukkitCommand {

	public Nbt(JavaPlugin plugin) {
		super(plugin, "nbt");
		this.setPlayerOnly();
		this.setMinimumArgsLength(0);
	}

	private static String filter(String value) {
		return RegexUtil.replace(value, RegexUtil.VANILLA_PATTERN, "&$1");
	}

	@Override
	protected void onCommand(CommandSender sender, String alias, String[] args) throws Exception {
		BukkitMojangProfile profile = NiftyBukkit.getMojangRepository().searchByPlayer((Player)sender);
		Player player = profile.getOfflinePlayer().getPlayer();

		if (player.getItemInHand() == null || Material.AIR == player.getItemInHand().getType()) {
			this.getLog().error(sender, "Empty or AIR blocks have no NBT!");
			return;
		}

		ItemData itemData = new ItemData(player.getItemInHand());

		if (itemData.getNbt().notEmpty()) {
			if (ListUtil.notEmpty(args)) {
				String key = args[0];

				if (itemData.getNbt().containsKey(key))
					this.getLog().message(sender, "NBT key {{0}}: {{1}}.", key, filter(itemData.getNbt().get(key).toString()));
				else if (itemData.getNbt().containsPath(key))
					this.getLog().message(sender, "NBT path {{0}}: {{1}}.", key, filter(itemData.getNbt().getPath(key).toString()));
				else
					this.getLog().error(sender, "NBT key {{0}} does not exist!", key);
			} else
				this.getLog().message(sender, "All NBT: {{0}}.", filter(itemData.getNbt().toString()));
		} else
			this.getLog().message(sender, "Item contains no NBT.");
	}

}