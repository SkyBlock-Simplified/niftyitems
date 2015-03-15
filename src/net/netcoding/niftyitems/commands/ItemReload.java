package net.netcoding.niftyitems.commands;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemReload extends BukkitCommand {

	public ItemReload(JavaPlugin plugin) {
		super(plugin, "itemreload");
		this.setMinimumArgsLength(0);
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) {
		NiftyBukkit.getItemDatabase().reload();
		this.getLog().message(sender, "{0} reloaded.", this.getPluginDescription().getName());
	}

}