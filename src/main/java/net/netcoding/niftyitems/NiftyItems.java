package net.netcoding.niftyitems;

import net.netcoding.niftybukkit.inventory.FakeInventory;
import net.netcoding.niftybukkit.minecraft.BukkitPlugin;
import net.netcoding.niftyitems.cache.Config;
import net.netcoding.niftyitems.commands.ClearInventory;
import net.netcoding.niftyitems.commands.ClearLore;
import net.netcoding.niftyitems.commands.Enchant;
import net.netcoding.niftyitems.commands.GameMode;
import net.netcoding.niftyitems.commands.Give;
import net.netcoding.niftyitems.commands.Invsee;
import net.netcoding.niftyitems.commands.ItemDb;
import net.netcoding.niftyitems.listeners.ArmorInventory;
import net.netcoding.niftyitems.listeners.Blocks;
import net.netcoding.niftyitems.listeners.GameModeFix;
import net.netcoding.niftyitems.listeners.Inventory;
import net.netcoding.niftyitems.listeners.PlayerInventory;
import net.netcoding.niftyitems.listeners.Players;

public class NiftyItems extends BukkitPlugin {

	private static transient Config pluginConfig;
	private static transient FakeInventory fakeArmorInventory;
	private static transient FakeInventory fakePlayerInventory;

	@Override
	public void onEnable() {
		this.getLog().console("Loading Config");
		try {
			(pluginConfig = new Config(this)).init();
			pluginConfig.startWatcher();
		} catch (Exception ex) {
			this.getLog().console("Unable to monitor config! Changes will require a restart!", ex);
		}

		this.getLog().console("Registering Commands");
		new ClearInventory(this);
		new ClearLore(this);
		new Enchant(this);
		new GameMode(this);
		new Give(this);
		new Invsee(this);
		new ItemDb(this);

		this.getLog().console("Registering Listeners");
		new Blocks(this);
		new GameModeFix(this);
		new Inventory(this);
		new Players(this);
		fakeArmorInventory = new FakeInventory(this, new ArmorInventory(this));
		fakeArmorInventory.setTradingEnabled();
		fakeArmorInventory.setTitle("Equipment Inventory");
		fakePlayerInventory = new FakeInventory(this, new PlayerInventory(this));
		fakePlayerInventory.setTradingEnabled();
		fakeArmorInventory.setTitle("Player Inventory");
	}

	@Override
	public void onDisable() {
		if (fakeArmorInventory != null)
			fakeArmorInventory.closeAll();

		if (fakePlayerInventory != null)
			fakePlayerInventory.closeAll();
	}

	public final static FakeInventory getFakeArmorInventory() {
		return fakeArmorInventory;
	}

	public final static FakeInventory getFakePlayerInventory() {
		return fakePlayerInventory;
	}

	public final static Config getPluginConfig() {
		return pluginConfig;
	}

}