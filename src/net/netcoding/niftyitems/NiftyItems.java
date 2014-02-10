package net.netcoding.niftyitems;

import static net.netcoding.niftyitems.managers.Cache.Log;
import net.netcoding.niftyitems.commands.*;
import net.netcoding.niftyitems.listeners.Inventory;
import net.netcoding.niftyitems.managers.Cache;

import org.bukkit.plugin.java.JavaPlugin;

public class NiftyItems extends JavaPlugin {

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		new Cache(this);

		Log.console("Registering Commands");
		new ClearLore(this);
		new Item(this);
		new ItemDb(this);

		try {
			new IReload(this).command(this.getServer().getConsoleSender(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.console("Registering Event Listeners");
		new Inventory(this);
	}

}