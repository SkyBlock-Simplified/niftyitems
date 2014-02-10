package net.netcoding.niftyitems.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import net.netcoding.niftybukkit.minecraft.BukkitHelper;
import net.netcoding.niftybukkit.utilities.NumberUtil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDatabase extends BukkitHelper {

	private final transient CSVFile file;
	private final transient Map<String, Integer> items = new HashMap<String, Integer>();
	private final transient Map<ItemData, List<String>> names = new HashMap<ItemData, List<String>>();
	private final transient Map<ItemData, String> primaryName = new HashMap<ItemData, String>();
	private final transient Map<String, Short> durabilities = new HashMap<String, Short>();
	private final static transient String strSplitPattern = ":+',;.";
	final static transient Pattern splitPattern = Pattern.compile(String.format("[%s]", strSplitPattern));
	final static transient Pattern notSplitPattern = Pattern.compile(String.format("[^%s]", strSplitPattern));

	public ItemDatabase(JavaPlugin plugin) {
		super(plugin);
		this.file = new CSVFile(this.getPlugin(), "items.csv", true);
	}

	public void reload() {
		final List<String> lines = this.file.getLines();
		if (lines.isEmpty()) return;

		this.durabilities.clear();
		this.items.clear();
		this.names.clear();
		this.primaryName.clear();

		for (String line : lines) {
			line = line.trim().toLowerCase(Locale.ENGLISH);
			if (line.length() > 0 && line.charAt(0) == '#') continue;
			final String[] parts = line.split("[^a-z0-9]");
			if (parts.length < 2) continue;
			final int numeric = Integer.parseInt(parts[1]);
			final short data = parts.length > 2 && !parts[2].equals("0") ? Short.parseShort(parts[2]) : 0;
			String itemName = parts[0].toLowerCase(Locale.ENGLISH);
			this.durabilities.put(itemName, data);
			this.items.put(itemName, numeric);
			ItemData itemData = new ItemData(numeric, data);

			if (this.names.containsKey(itemData)) {
				List<String> nameList = this.names.get(itemData);
				nameList.add(itemName);
				Collections.sort(nameList, new LengthCompare());
			} else {
				List<String> nameList = new ArrayList<String>();
				nameList.add(itemName);
				this.names.put(itemData, nameList);
				this.primaryName.put(itemData, itemName);
			}
		}
	}

	public ItemStack get(final String id, final int quantity) throws Exception {
		final ItemStack retval = get(id.toLowerCase(Locale.ENGLISH));
		retval.setAmount(quantity);
		return retval;
	}

	@SuppressWarnings("deprecation")
	public ItemStack get(final String id) throws Exception {
		int itemid = 0;
		String itemname = null;
		short metaData = 0;
		String[] parts = splitPattern.split(id);

		if (id.matches(String.format("^\\d+%s\\d+$", splitPattern.pattern()))) {
			itemid = Integer.parseInt(parts[0]);
			metaData = Short.parseShort(parts[1]);
		} else if (NumberUtil.isInt(id))
			itemid = Integer.parseInt(id);
		else if (id.matches(String.format("^%1$s+%2$s\\d+$", notSplitPattern.pattern(), splitPattern.pattern()))) {
			itemname = parts[0].toLowerCase(Locale.ENGLISH);
			metaData = Short.parseShort(parts[1]);
		} else
			itemname = id.toLowerCase(Locale.ENGLISH);

		if (itemname != null) {
			if (this.items.containsKey(itemname)) {
				itemid = this.items.get(itemname);

				if (this.durabilities.containsKey(itemname) && metaData == 0)
					metaData = this.durabilities.get(itemname);
			} else if (Material.getMaterial(itemname.toUpperCase(Locale.ENGLISH)) != null) {
				itemid = Material.getMaterial(itemname.toUpperCase(Locale.ENGLISH)).getId();
				metaData = 0;
			} else
				throw new Exception("Unknown item name: " + id);
		}

		final Material mat = Material.getMaterial(itemid);
		if (mat == null) throw new Exception("Unknown item id: " + itemid);
		final ItemStack retval = new ItemStack(mat);
		retval.setAmount(mat.getMaxStackSize());
		retval.setDurability(metaData);
		return retval;
	}

	@SuppressWarnings("deprecation")
	public List<ItemStack> getMatching(Player player, String[] args) throws Exception
	{
		List<ItemStack> is = new ArrayList<ItemStack>();

		if (args.length < 1)
			is.add(player.getItemInHand());
		else if (args[0].equalsIgnoreCase("hand"))
			is.add(player.getItemInHand());
		else if (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("invent") || args[0].equalsIgnoreCase("all")) {
			for (ItemStack stack : player.getInventory().getContents()) {
				if (stack == null || stack.getType() == Material.AIR) continue;
				is.add(stack);
			}
		} else if (args[0].equalsIgnoreCase("blocks")) {
			for (ItemStack stack : player.getInventory().getContents()) {
				if (stack == null || stack.getTypeId() > 255 || stack.getType() == Material.AIR) continue;
				is.add(stack);
			}
		} else
			is.add(get(args[0]));

		if (is.isEmpty() || is.get(0).getType() == Material.AIR)
			throw new Exception("No item found!");

		return is;
	}

	@SuppressWarnings("deprecation")
	public List<String> names(ItemStack item) {
		ItemData itemData     = new ItemData(item.getTypeId(), item.getDurability());
		List<String> nameList = this.names.get(itemData);

		if (nameList == null) {
			itemData = new ItemData(item.getTypeId(), (short)0);
			nameList = this.names.get(itemData);
			if (nameList == null) return Collections.emptyList();
		}

		if (nameList.size() > 15) nameList = nameList.subList(0, 14);
		return nameList;
	}

	@SuppressWarnings("deprecation")
	public String name(ItemStack item) {
		ItemData itemData = new ItemData(item.getTypeId(), item.getDurability());
		String name       = this.primaryName.get(itemData);

		if (name == null) {
			itemData = new ItemData(item.getTypeId(), (short)0);
			name     = this.primaryName.get(itemData);
			if (name == null) return null;
		}

		return name;
	}

	static class ItemData {

		private final int itemNo;
		private final short itemData;

		public ItemData(int itemNo, short itemData) {
			this.itemNo = itemNo;
			this.itemData = itemData;
		}

		public int getItemNo() {
			return itemNo;
		}

		public short getItemData() {
			return itemData;
		}

		@Override
		public int hashCode() {
			return (31 * itemNo) ^ itemData;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof ItemData)) return false;
			ItemData pairo = (ItemData)o;
			return this.itemNo == pairo.getItemNo() && this.itemData == pairo.getItemData();
		}

	}

	static class LengthCompare implements Comparator<String> {

		public LengthCompare() {
			super();
		}

		@Override
		public int compare(String s1, String s2) {
			return s1.length() - s2.length();
		}

	}

}