package net.netcoding.niftyitems;

import net.netcoding.niftybukkit.minecraft.BukkitPlugin;
import net.netcoding.niftyitems.commands.ClearLore;
import net.netcoding.niftyitems.commands.IReload;
import net.netcoding.niftyitems.commands.Item;
import net.netcoding.niftyitems.commands.ItemDb;
import net.netcoding.niftyitems.listeners.Inventory;
import net.netcoding.niftyitems.managers.Cache;
import net.netcoding.niftyitems.managers.Settings;

public class NiftyItems extends BukkitPlugin {

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		Cache.Settings = new Settings(this);
		Cache.Settings.reload();

		this.getLog().console("Registering Commands");
		new ClearLore(this);
		new Item(this);
		new ItemDb(this);
		new IReload(this);

		this.getLog().console("Registering Listeners");
		new Inventory(this);
	}

}