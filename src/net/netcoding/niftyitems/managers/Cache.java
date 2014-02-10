package net.netcoding.niftyitems.managers;

import net.netcoding.niftybukkit.minecraft.Log;

import org.bukkit.plugin.java.JavaPlugin;

public class Cache {

	public static transient ItemDatabase ItemDatabase = null;
	public static transient Settings Settings = null;
	public static Log Log = null;

	public Cache(JavaPlugin plugin) {
		Log = new Log(plugin);
		Settings = new Settings(plugin);
		ItemDatabase = new ItemDatabase(plugin);
	}

}