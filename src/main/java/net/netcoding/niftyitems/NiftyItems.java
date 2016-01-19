package net.netcoding.niftyitems;

import net.netcoding.niftybukkit.inventory.FakeInventory;
import net.netcoding.niftybukkit.minecraft.BukkitPlugin;
import net.netcoding.niftyitems.cache.Config;
import net.netcoding.niftyitems.commands.ClearInventory;
import net.netcoding.niftyitems.commands.ClearLore;
import net.netcoding.niftyitems.commands.Enchant;
import net.netcoding.niftyitems.commands.GameMode;
import net.netcoding.niftyitems.commands.Item;
import net.netcoding.niftyitems.commands.Invsee;
import net.netcoding.niftyitems.commands.ItemDb;
import net.netcoding.niftyitems.listeners.ArmorInventory;
import net.netcoding.niftyitems.listeners.Blocks;
import net.netcoding.niftyitems.listeners.GameModeFix;
import net.netcoding.niftyitems.listeners.Inventory;
import net.netcoding.niftyitems.listeners.PlayerInventory;
import net.netcoding.niftyitems.listeners.Players;

public class NiftyItems extends BukkitPlugin {

	private static transient Config PLUGIN_CONFIG;
	private static transient FakeInventory FAKE_ARMOR_INVENTORY;
	private static transient FakeInventory FAKE_PLAYER_INVENTORY;

	@Override
	public void onEnable() {
		this.getLog().console("Loading Config");
		try {
			(PLUGIN_CONFIG = new Config(this)).init();
			PLUGIN_CONFIG.startWatcher();
		} catch (Exception ex) {
			this.getLog().console("Unable to monitor config! Changes will require a restart!", ex);
		}

		this.getLog().console("Registering Commands");
		new ClearInventory(this);
		new ClearLore(this);
		new Enchant(this);
		new GameMode(this);
		new Item(this);
		new Invsee(this);
		new ItemDb(this);

		this.getLog().console("Registering Listeners");
		new Blocks(this);
		new GameModeFix(this);
		new Inventory(this);
		new Players(this);
		FAKE_ARMOR_INVENTORY = new FakeInventory(this, new ArmorInventory(this));
		FAKE_ARMOR_INVENTORY.setTradingEnabled();
		FAKE_ARMOR_INVENTORY.setTitle("Equipment Inventory");
		FAKE_PLAYER_INVENTORY = new FakeInventory(this, new PlayerInventory(this));
		FAKE_PLAYER_INVENTORY.setTradingEnabled();
		FAKE_ARMOR_INVENTORY.setTitle("Player Inventory");
	}

	@Override
	public void onDisable() {
		if (FAKE_ARMOR_INVENTORY != null)
			FAKE_ARMOR_INVENTORY.closeAll();

		if (FAKE_PLAYER_INVENTORY != null)
			FAKE_PLAYER_INVENTORY.closeAll();
	}

	public final static FakeInventory getFakeArmorInventory() {
		return FAKE_ARMOR_INVENTORY;
	}

	public final static FakeInventory getFakePlayerInventory() {
		return FAKE_PLAYER_INVENTORY;
	}

	public final static Config getPluginConfig() {
		return PLUGIN_CONFIG;
	}

}