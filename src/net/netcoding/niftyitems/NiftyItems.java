package net.netcoding.niftyitems;

import net.netcoding.niftybukkit.minecraft.BukkitPlugin;
import net.netcoding.niftyitems.cache.Config;
import net.netcoding.niftyitems.commands.ClearInventory;
import net.netcoding.niftyitems.commands.ClearLore;
import net.netcoding.niftyitems.commands.Enchant;
import net.netcoding.niftyitems.commands.GameMode;
import net.netcoding.niftyitems.commands.Give;
import net.netcoding.niftyitems.commands.ItemDb;
import net.netcoding.niftyitems.listeners.Blocks;
import net.netcoding.niftyitems.listeners.GameModeFix;
import net.netcoding.niftyitems.listeners.Inventory;
import net.netcoding.niftyitems.listeners.Players;

public class NiftyItems extends BukkitPlugin {

	private static transient Config pluginConfig;

	@Override
	public void onEnable() {
		this.getLog().console("Loading Config");
		(pluginConfig = new Config(this)).init();
		pluginConfig.startWatcher();

		this.getLog().console("Registering Commands");
		new ClearInventory(this);
		new ClearLore(this);
		new Enchant(this);
		new GameMode(this);
		new Give(this);
		new ItemDb(this);

		this.getLog().console("Registering Listeners");
		new Blocks(this);
		new GameModeFix(this);
		new Inventory(this);
		new Players(this);
	}

	public final static Config getPluginConfig() {
		return pluginConfig;
	}

}