package net.netcoding.niftyitems.commands;

import java.sql.SQLException;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftyitems.cache.Cache;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemReload extends BukkitCommand {

	public ItemReload(JavaPlugin plugin) {
		super(plugin, "itemreload");
		this.setRequireArgs(false);
	}

	@Override
	public void onCommand(CommandSender sender, String alias, String[] args) throws SQLException, Exception {
		NiftyBukkit.getItemDatabase().reload();
		Cache.Config.reload();
		this.getLog().message(sender, "{0} reloaded.", this.getPluginDescription().getName());
	}

}