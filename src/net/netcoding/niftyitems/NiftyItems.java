package net.netcoding.niftyitems;

import net.netcoding.niftybukkit.minecraft.BukkitPlugin;
import net.netcoding.niftyitems.cache.Config;
import net.netcoding.niftyitems.commands.ClearInventory;
import net.netcoding.niftyitems.commands.ClearLore;
import net.netcoding.niftyitems.commands.GameMode;
import net.netcoding.niftyitems.commands.Item;
import net.netcoding.niftyitems.commands.ItemDb;
import net.netcoding.niftyitems.listeners.GameModeFix;
import net.netcoding.niftyitems.listeners.Inventory;

public class NiftyItems extends BukkitPlugin {

	private static transient Config pluginConfig;

	@Override
	public void onEnable() {
		this.getLog().console("Loading Config");
		pluginConfig = new Config(this);
		pluginConfig.startWatcher();

		this.getLog().console("Registering Commands");
		new ClearInventory(this);
		new ClearLore(this);
		new GameMode(this);
		new Item(this);
		new ItemDb(this);

		this.getLog().console("Registering Listeners");
		new Inventory(this);
		new GameModeFix(this);
	}

	public final static Config getPluginConfig() {
		return pluginConfig;
	}

}