package net.netcoding.niftyitems.managers;

import net.netcoding.niftybukkit.minecraft.Log;

import org.bukkit.plugin.java.JavaPlugin;

public class Cache {

	public static transient Log Log;
	public static transient Settings Settings;

	public Cache(JavaPlugin plugin) {
		Log = new Log(plugin);
		Settings = new Settings(plugin);
	}

}