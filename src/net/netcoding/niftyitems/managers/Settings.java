package net.netcoding.niftyitems.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.netcoding.niftybukkit.minecraft.BukkitHelper;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Settings extends BukkitHelper {

	private int defaultItemStackSize   = 64;
	private int defaultBlockStackSize  = 64;
	private int overstackedSize        = 100;
	private boolean destroyAllDrops    = false;
	private boolean destroySpawnDrops  = false;
	private Map<String, Map<String, String>> localization = new HashMap<>();
	private Map<String, List<String>> blacklists = new HashMap<>();

	public Settings(JavaPlugin plugin) {
		super(plugin);
	}

	public boolean destroyAllDrops() {
		return this.destroyAllDrops;
	}

	public boolean destroySpawnedDrops() {
		return this.destroySpawnDrops;
	}

	public List<String> getBlacklist(String list) {
		return this.blacklists.get(list);
	}

	public String getLocalization(String section, String message) {
		System.out.println(section);
		System.out.println(message);
		return this.localization.get(section).get(message);
	}

	public int getDefaultItemStackSize() {
		return this.defaultItemStackSize;
	}

	public int getDefaultBlockStackSize() {
		return this.defaultBlockStackSize;
	}

	public int getOversizedStackSize() {
		return this.overstackedSize;
	}

	public ItemStack loreItem(Player player, ItemStack item, String lore) {
		if (this.isRestricted(item).equalsIgnoreCase("none")) {
			if (item != null && item.getType() != Material.AIR) {
				ItemMeta itemMeta = item.getItemMeta();
				List<String> lores = new ArrayList<>();

				if (item.getItemMeta().hasLore()) {
					itemMeta = item.getItemMeta();
					lores = itemMeta.getLore();
				}

				lores.add(lore + " | " + player.getName());
				itemMeta.setLore(lores);
				item.setItemMeta(itemMeta);
			}
		}

		return item;
	}

	public String getLore(String type) {
		return (type == "creative" ? this.getLocalization("lore", "creative") : (type == "spawned" ? this.getLocalization("lore", "spawned") : null));
	}

	public String getOwner(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
				String lore = "none";

				if (!(lore = isRestricted(item)).equalsIgnoreCase("none")) {
					List<String> lores = item.getItemMeta().getLore();
					String localized   = this.getLore(lore);

					for (String cLore : lores) {
						if (cLore.startsWith(localized))
							return cLore.replace(localized + " | ", "");
					}
				}
			}
		}

		return null;
	}

	@SuppressWarnings("deprecation")
	public boolean isBlacklisted(Player player, ItemStack stack, String list) {
		if (stack != null && stack.getType() != Material.AIR) {
			String bypassPerm = String.format("niftyitems.bypass.%s.", list);
			String itemName = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");

			try {
				for (String bitem : this.getBlacklist(list)) {
					String[] parts  = ItemDatabase.splitPattern.split(bitem);
					int id          = Integer.parseInt(parts[0]);
					Short data      = Short.parseShort((parts.length == 2 ? parts[1] : 0) + "");
					boolean similar = (stack.getTypeId() == id) && (data == 0 ? true : stack.getDurability() == data);

					if (similar) {
						if (player == null) return true;

						if (!player.hasPermission(bypassPerm + id) && !player.hasPermission(bypassPerm + itemName))
							return true;
					}
				}
			} catch (NullPointerException npe) {
				if (!player.hasPermission(bypassPerm + stack.getTypeId()) && !player.hasPermission(bypassPerm + itemName))
					return true;
			}
		}

		return false;
	}

	public boolean isOwner(ItemStack item, String playerName) {
		return this.getOwner(item) == playerName;
	}

	public String isRestricted(ItemStack i) {
		if (i != null && i.getType() != Material.AIR) {
			if (i.hasItemMeta() && i.getItemMeta().hasLore()) {
				for (String s : i.getItemMeta().getLore()) {
					if (s.startsWith(this.getLore("creative")))
						return "creative";
					else if (s.startsWith(this.getLore("spawned")))
						return "spawned";
				}
			}
		}

		return "none";
	}

	public void reload() {
		FileConfiguration config = this.getPlugin().getConfig();

		ConfigurationSection stackSize = config.getConfigurationSection("stack-size");
		this.defaultItemStackSize = stackSize.getInt("item");
		this.defaultBlockStackSize = stackSize.getInt("block");
		this.overstackedSize = stackSize.getInt("overstacked");

		ConfigurationSection destroyItems = config.getConfigurationSection("destroy-items");
		this.destroySpawnDrops = destroyItems.getBoolean("spawned");
		this.destroyAllDrops = destroyItems.getBoolean("all");

		this.blacklists.clear();
		ConfigurationSection blacklist = config.getConfigurationSection("blacklist");
		String[] blacklistNames = new String[] { "spawning", "creative", "placement" };
		for (String listName : blacklistNames) {
			List<String> blacklistItems = new ArrayList<>();
			String[] blacklistItemArr = blacklist.getString(listName).split(",(?![^\\[]*\\])");

			for (String blacklistItem : blacklistItemArr) {
				String currentItem = blacklistItem;

				if (blacklistItem.contains(":")) {
					String[] split = blacklistItem.split(":");
					String dataValue = split[1];

					if (dataValue.contains(",")) {
						String[] dataValues = dataValue.substring(1, (dataValue.length() - 1)).split(",");
						for (String value : dataValues) blacklistItems.add(String.format("%s:%s", split[0], value));
					}
				}

				blacklistItems.add(currentItem);
			}

			this.blacklists.put(listName, blacklistItems);
		}

		this.localization.clear();
		ConfigurationSection localization = config.getConfigurationSection("localization");
		for (String sectionName : localization.getKeys(false)) {
			ConfigurationSection section = localization.getConfigurationSection(sectionName);
			Map<String, String> messages = new HashMap<>();

			for (String messageName : section.getKeys(false))
				messages.put(messageName, section.getString(messageName));

			this.localization.put(sectionName, messages);
		}
	}

}