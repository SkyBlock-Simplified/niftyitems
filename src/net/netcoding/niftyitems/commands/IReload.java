package net.netcoding.niftyitems.commands;

import java.sql.SQLException;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftyitems.managers.Cache;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class IReload extends BukkitCommand {

	public IReload(JavaPlugin plugin) {
		super(plugin, "itemreload", false);
	}

	@Override
	public void command(CommandSender sender, String[] args) throws SQLException, Exception {
		this.getPlugin().reloadConfig();
		NiftyBukkit.getItemDatabase().reload();
		Cache.Settings.reload();
		this.getLog().message(sender, "%1$s reloaded", this.getPluginDescription().getName());
	}

}