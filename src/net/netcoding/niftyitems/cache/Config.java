package net.netcoding.niftyitems.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.netcoding.niftybukkit.NiftyBukkit;
import net.netcoding.niftybukkit.inventory.items.ItemData;
import net.netcoding.niftybukkit.util.StringUtil;
import net.netcoding.niftybukkit.yaml.annotations.Comment;
import net.netcoding.niftybukkit.yaml.annotations.Path;
import net.netcoding.niftybukkit.yaml.exceptions.InvalidConfigurationException;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Config extends net.netcoding.niftybukkit.yaml.Config {

	private static final int DEFAULT_ITEMSTACK_SIZE = 1;
	private static final int DEFAULT_BLOCKSTACK_SIZE = 64;
	private static final int MAXIMUM_OVERSTACKED_SIZE = 100;

	@Path("stack-size.item")
	private int itemStackSize = DEFAULT_ITEMSTACK_SIZE;

	@Path("stack-size.block")
	private int blockStackSize = DEFAULT_BLOCKSTACK_SIZE;

	@Path("stack-size.overstacked")
	private int overStackedSize = MAXIMUM_OVERSTACKED_SIZE;

	@Path("destroy-items.spawned")
	@Comment("Destroy spawned items when dropped")
	private boolean destroySpawned = true;

	@Comment("Destroy all items when dropped")
	@Path("destroy-items.all")
	private boolean destroyAll = false;

	private Map<String, String> blacklists = new HashMap<>();

	public Config(JavaPlugin plugin) {
		super(plugin, "config");
		String blacklisted = "7,8,9,10,11,14,15,16,19,21,22,30,41,42,43,46,51,52,56,57,62,73,78,79,90,97,99,100,119,120,122,125,127,129,133,137,141,142,155,166,259,263,264,265,266,327,373:[16388,16420,16452,16424,16426,16428,16456,16458,16460],381,383:[51,52,54,55,56,57,58,59,60,61,62,65,66],384,385,388,397,401,407,422";
		this.blacklists.put("spawned", blacklisted);
		this.blacklists.put("creative", blacklisted);
		this.blacklists.put("placement", "7,8,9,10,11,51,52,119,120,137,138,166,259,373:[16388,16420,16452,16424,16426,16428,16456,16458,16460],381,383:[51,52,54-62,65,66],384,385,397:1,401,407,422");
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
	public boolean hasBypass(CommandSender sender, ItemStack stack, String list) {
		if (sender == null) return true;
		if (stack == null) return true;
		if (Material.AIR.equals(stack.getType())) return false;
		if (!(this.blacklists.keySet().contains(list) || "store".equals(list))) return false;
		boolean hasBypass = this.hasPermissions(sender, "bypass", list, String.valueOf(stack.getTypeId())) || this.hasPermissions(sender, "bypass", list, StringUtil.format("{0}:{1}", String.valueOf(stack.getTypeId()), stack.getDurability()));
		List<String> names = NiftyBukkit.getItemDatabase().names(stack);

		if (!hasBypass) {
			for (String name : names) {
				if (hasBypass = this.hasPermissions(sender, "bypass", list, name) || this.hasPermissions(sender, "bypass", list, StringUtil.format("{0}:{1}", name, stack.getDurability())))
					break;
			}
		}

		return hasBypass;
	}

	@SuppressWarnings("deprecation")
	public boolean isBlacklisted(CommandSender sender, ItemStack stack, String list) {
		if (sender == null) return true;
		if (stack == null) return true;
		if (Material.AIR.equals(stack.getType())) return false;
		if (!(this.blacklists.keySet().contains(list) || "store".equals(list))) return false;
		boolean hasBypass = this.hasBypass(sender, stack, list);
		if ("store".equals(list)) return !hasBypass;

		if (!hasBypass) {
			for (ItemData item : NiftyBukkit.getItemDatabase().parse(this.blacklists.get(list))) {
				if (item.getId() == stack.getTypeId() && (item.getData() == 0 || item.getData() == stack.getDurability()))
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

		if (this.overStackedSize < 0 || this.overStackedSize > MAXIMUM_OVERSTACKED_SIZE) {
			this.overStackedSize = MAXIMUM_OVERSTACKED_SIZE;
			save = true;
		}

		if (save) this.save();
	}

}