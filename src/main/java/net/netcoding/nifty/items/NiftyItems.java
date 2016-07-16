package net.netcoding.nifty.items;

import net.netcoding.nifty.common.api.inventory.FakeInventory;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.region.World;
import net.netcoding.nifty.items.cache.Config;
import net.netcoding.nifty.items.commands.*;
import net.netcoding.nifty.items.listeners.ArmorInventory;
import net.netcoding.nifty.items.listeners.Blocks;
import net.netcoding.nifty.items.listeners.GameModeFix;
import net.netcoding.nifty.items.listeners.Inventory;
import net.netcoding.nifty.items.listeners.Players;

public class NiftyItems extends MinecraftPlugin {

	private static transient Config PLUGIN_CONFIG;
	private static transient FakeInventory FAKE_ARMOR_INVENTORY;

	@Override
	public void onEnable() {
		this.getLog().console("Loading Config");
		try {
			(PLUGIN_CONFIG = new Config(this)).init();
			PLUGIN_CONFIG.startWatcher();
		} catch (Exception ex) {
			this.getLog().console("Unable to monitor config! Changes will require a restart!", ex);
		}

		this.getLog().console("Registering Gamerules");
		String destroyAll = String.valueOf(!getPluginConfig().destroyAllDrops());
		for (World world : this.getServer().getWorlds()) {
			world.setGameRuleValue("doEntityDrops", destroyAll);
			world.setGameRuleValue("doTileDrops", destroyAll);
			world.setGameRuleValue("doMobLoot", destroyAll);
		}

		this.getLog().console("Registering Commands");
		new BlockMask(this);
		new ClearInventory(this);
		new ClearLore(this);
		new Enchant(this);
		new GameMode(this);
		new Invsee(this);
		new Item(this);
		new ItemDb(this);
		new More(this);
		new Nbt(this);

		this.getLog().console("Registering Listeners");
		new Blocks(this);
		new GameModeFix(this);
		new Inventory(this);
		new Players(this);
		FAKE_ARMOR_INVENTORY = new FakeInventory(this, new ArmorInventory(this));
		FAKE_ARMOR_INVENTORY.setAllowEmpty(true);
		FAKE_ARMOR_INVENTORY.setTradingEnabled();
		FAKE_ARMOR_INVENTORY.setTitle("Equipment Inventory");
	}

	@Override
	public void onDisable() {
		if (FAKE_ARMOR_INVENTORY != null)
			FAKE_ARMOR_INVENTORY.closeAll();
	}

	public static FakeInventory getFakeArmorInventory() {
		return FAKE_ARMOR_INVENTORY;
	}

	public static Config getPluginConfig() {
		return PLUGIN_CONFIG;
	}

}