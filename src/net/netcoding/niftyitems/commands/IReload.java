package net.netcoding.niftyitems.commands;

import static net.netcoding.niftyitems.managers.Cache.Log;

import java.sql.SQLException;

import net.netcoding.niftybukkit.minecraft.BukkitCommand;
import net.netcoding.niftyitems.managers.Cache;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class IReload extends BukkitCommand {

	public IReload(JavaPlugin plugin) {
		super(plugin, "ireload", false);
	}

	@Override
	public void command(CommandSender sender, String[] args) throws SQLException, Exception {
		this.getPlugin().reloadConfig();
		Cache.ItemDatabase.reload();
		Cache.Settings.reload();

		Log.message(sender, "%1$s reloaded", this.getPluginDescription().getName());
	}

}