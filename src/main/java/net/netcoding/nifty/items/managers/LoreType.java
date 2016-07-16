package net.netcoding.nifty.items.managers;

public enum LoreType {

	CREATIVE,
	SPAWNED,
	NONE;

	public static LoreType fromName(String name) {
		for (LoreType type : values()) {
			if (type.name().equals(name.toUpperCase()))
				return type;
		}

		return NONE;
	}

}