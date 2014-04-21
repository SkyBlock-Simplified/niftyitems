package net.netcoding.niftyitems.cache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.items.ItemData;
import net.netcoding.niftybukkit.yaml.annotations.Path;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Config extends net.netcoding.niftybukkit.yaml.Config {

	@Path("stack-size.item")
	private int itemStackSize = 1;

	@Path("stack-size.block")
	private int blockStackSize = 64;

	@Path("stack-size.overstacked")
	private int overStackedSize = 100;

	@Path("destroy-items.spawned")
	private boolean destroySpawned = true;

	@Path("destroy-items.all")
	private boolean destroyAll = false;

	private Map<String, String> blacklists = new HashMap<>();

	public Config(JavaPlugin plugin) {
		super(plugin, "config");
		String blacklisted = "7,8,9,10,11,14,15,16,19,21,22,30,41,42,43,46,51,52,56,57,62,73,78,79,90,97,99,100,119,120,122,125,127,129,133,137,141,142,155,259,263,264,265,266,327,373:[16388,16420,16452,16424,16426,16428,16456,16458,16460],381,383:[51,52,54,55,56,57,58,59,60,61,62,65,66],384,385,388,397,401,407,422";
		this.blacklists.put("spawned", blacklisted);
		this.blacklists.put("creative", blacklisted);
		this.blacklists.put("placement", "7,8,9,10,11,51,52,119,120,137,138,259,373:[16388,16420,16452,16424,16426,16428,16456,16458,16460],381,383:[51,52,54-62,65,66],384,385,397:1,401,407,422");
	}

	public int getItemStackSize() {
		return this.itemStackSize;
	}

	public int getBlockStackSize() {
		return this.blockStackSize;
	}

	public int getOversizedStackSize() {
		return this.overStackedSize;
	}

	public boolean destroySpawnedDrops() {
		return this.destroySpawned;
	}

	public boolean destroyAllDrops() {
		return this.destroyAll;
	}

	@SuppressWarnings("deprecation")
	public boolean isBlacklisted(Player player, ItemStack stack, String list) {
		if (player == null) return true;

		if (stack != null && stack.getType() != Material.AIR) {
			String itemName = stack.getType().toString().toLowerCase(Locale.ENGLISH);
			boolean blacklisted = (!this.hasPermissions(player, "bypass", list, String.valueOf(stack.getTypeId())) && !this.hasPermissions(player, "bypass", list, itemName));

			if ("store".equals(list))
				return blacklisted;
			else {
				for (ItemData item : NiftyBukkit.getItemDatabase().parse(this.blacklists.get(list))) {
					if (item.getId() == stack.getTypeId() && (item.getData() == 0 || item.getData() == stack.getDurability()))
						return blacklisted;
				}
			}
		}

		return false;
	}

}