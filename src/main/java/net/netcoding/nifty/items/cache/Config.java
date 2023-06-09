package net.netcoding.nifty.items.cache;

import net.netcoding.nifty.common.Nifty;
import net.netcoding.nifty.common.api.plugin.MinecraftPlugin;
import net.netcoding.nifty.common.minecraft.command.CommandSource;
import net.netcoding.nifty.common.minecraft.entity.living.human.Player;
import net.netcoding.nifty.common.minecraft.inventory.item.ItemStack;
import net.netcoding.nifty.common.minecraft.material.Material;
import net.netcoding.nifty.common.yaml.BukkitConfig;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentMap;
import net.netcoding.nifty.core.yaml.ConfigSection;
import net.netcoding.nifty.core.yaml.annotations.Comment;
import net.netcoding.nifty.core.yaml.annotations.Path;
import net.netcoding.nifty.core.yaml.exceptions.InvalidConfigurationException;
import net.netcoding.nifty.items.NiftyItems;

import java.util.List;

public class Config extends BukkitConfig {

	public static final int DEFAULT_ITEMSTACK_SIZE = 1;
	public static final int DEFAULT_BLOCKSTACK_SIZE = 64;
	public static final int MAXIMUM_OVERSTACKED_SIZE = 100;

	@Path("stack-size.item")
	private int itemStackSize = DEFAULT_ITEMSTACK_SIZE;

	@Path("stack-size.block")
	private int blockStackSize = DEFAULT_BLOCKSTACK_SIZE;

	@Path("prevent-drop.spawned")
	private boolean preventSpawned = true;

	@Path("prevent-drop.all")
	private boolean preventAll = false;

	@Path("destroy-items.spawned")
	private boolean destroySpawned = false;

	@Path("destroy-items.all")
	private boolean destroyAll = false;

	@Comment("Give command enforced blacklist for receiving player")
	@Path("give-enforces-blacklist")
	private boolean giveEnforcesBlacklist = false;

	@Comment("Prevent the access, use and breaking of blocks/items")
	private ConcurrentMap<String, String> blacklists = Concurrent.newMap();

	@Comment("Silence messages sent to players when prevented by a blacklist")
	private ConcurrentMap<String, Boolean> silent = Concurrent.newMap();

	public Config(MinecraftPlugin plugin) {
		super(plugin.getDataFolder(), "config");
		String defaultStart = "7,8,9,10,11";
		String harmfulPotions = "373:[16388,16420,16452,16424,16426,16428,16456,16458,16460]";
		String monsterEggs = "383:[51,52,54-62,65-68]";
		String blacklisted = StringUtil.format("{0},14,15,16,19,21,22,30,41,42,43,46,52,56,57,62,73,78,79,90,97,99,100,119,120,122,125,127,129,133,137,141,142,155,166,259,263-266,327,{1},381,{2},384,385,388,397,401,407,422", defaultStart, harmfulPotions, monsterEggs);
		this.blacklists.put("spawned", blacklisted);
		this.blacklists.put("creative", blacklisted);
		this.blacklists.put("place", StringUtil.format("{0},52,119,120,137,138,166,259,{1},381,{2},384,385,397:1,401,407,422", defaultStart, harmfulPotions, monsterEggs));
		this.blacklists.put("break", "7,166");

		for (String blacklist : this.blacklists.keySet())
			this.silent.put(blacklist, false);
	}

	public boolean giveEnforcesBlacklist() {
		return this.giveEnforcesBlacklist;
	}

	public int getItemStackSize() {
		return this.itemStackSize;
	}

	public int getBlockStackSize() {
		return this.blockStackSize;
	}

	public boolean destroySpawnedDrops() {
		return this.destroySpawned;
	}

	public boolean destroySpawnedDrops(Player player, ItemStack stack) {
		return !this.hasBypass(player, stack, "drop") && this.destroySpawnedDrops();
	}

	public boolean destroyAllDrops() {
		return this.destroyAll;
	}

	public boolean destroyAllDrops(Player player, ItemStack stack) {
		return this.hasBypass(player, stack, "drop") && this.destroyAllDrops();
	}

	public boolean isSilent(String blacklist) {
		return this.silent.containsKey(blacklist) ? this.silent.get(blacklist) : false;
	}

	public boolean hasBypass(CommandSource source, ItemStack stack, String blacklist) {
		if (source == null) return true;
		if (stack == null) return true;
		if (Material.AIR == stack.getType()) return false;
		if (!(this.blacklists.keySet().contains(blacklist) || blacklist.matches("^store|drop$"))) return false;
		boolean hasBypass = NiftyItems.getPlugin(NiftyItems.class).hasPermissions(source, "bypass", blacklist, String.valueOf(stack.getTypeId())) || NiftyItems.getPlugin(NiftyItems.class).hasPermissions(source, "bypass", blacklist, StringUtil.format("{0}:{1}", String.valueOf(stack.getTypeId()), stack.getDurability()));
		List<String> names = Nifty.getItemDatabase().names(stack);

		if (!hasBypass) {
			for (String name : names) {
				if (hasBypass = NiftyItems.getPlugin(NiftyItems.class).hasPermissions(source, "bypass", blacklist, name) || NiftyItems.getPlugin(NiftyItems.class).hasPermissions(source, "bypass", blacklist, StringUtil.format("{0}:{1}", name, stack.getDurability())))
					break;
			}
		}

		return hasBypass;
	}

	public boolean isBlacklisted(CommandSource source, ItemStack stack, String blacklist) {
		if (source == null) return true;
		if (stack == null) return true;
		if (Material.AIR == stack.getType()) return false;
		if (!(this.blacklists.keySet().contains(blacklist) || blacklist.matches("^store|drop$"))) return false;
		boolean hasBypass = this.hasBypass(source, stack, blacklist);
		if (blacklist.matches("^store|drop$")) return !hasBypass;

		if (!hasBypass) {
			for (ItemStack item : Nifty.getItemDatabase().parse(String.valueOf(this.blacklists.get(blacklist)))) {
				if (item.getTypeId() == stack.getTypeId() && (item.getDurability() == 0 || item.getDurability() == stack.getDurability()))
					return true;
			}
		}

		return false;
	}

	@Override
	public void load() throws InvalidConfigurationException {
		super.load();
		boolean save = false;

		if (this.itemStackSize < 0 || this.itemStackSize > MAXIMUM_OVERSTACKED_SIZE) {
			this.itemStackSize = DEFAULT_ITEMSTACK_SIZE;
			save = true;
		}

		if (this.blockStackSize < 0 || this.blockStackSize > MAXIMUM_OVERSTACKED_SIZE) {
			this.blockStackSize = DEFAULT_BLOCKSTACK_SIZE;
			save = true;
		}

		if (save) this.save();
	}

	public boolean preventSpawnedDrops() {
		return this.preventSpawned;
	}

	public boolean preventSpawnedDrops(Player player, ItemStack stack) {
		return !this.hasBypass(player, stack, "drop") && this.preventSpawnedDrops();
	}

	public boolean preventAllDrops() {
		return this.preventAll;
	}

	public boolean preventAllDrops(Player player, ItemStack stack) {
		return !this.hasBypass(player, stack, "drop") && this.preventAllDrops();
	}

	@Override
	public boolean update(ConfigSection root) throws InvalidConfigurationException {
		boolean updated = false;

		if (root.has("stack-size.overstacked")) {
			root.remove("stack-size.overstacked");
			updated = true;
		}

		if (root.has("blacklists.placement")) {
			root.set("blacklists.place", root.get("blacklists.placement"));
			root.remove("blacklists.placement");
			updated = true;
		}

		return updated;
	}

}