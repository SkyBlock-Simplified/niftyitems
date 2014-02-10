package net.netcoding.niftyitems.managers;

import static net.netcoding.niftyitems.managers.Cache.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.netcoding.niftybukkit.minecraft.BukkitHelper;

import org.bukkit.plugin.java.JavaPlugin;

public class CSVFile extends BukkitHelper {

	private final transient File file;

	private byte[] createChecksum(InputStream fileStream) throws Exception {
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fileStream.read(buffer);
			if (numRead > 0) complete.update(buffer, 0, numRead);
		} while (numRead != -1);

		fileStream.close();
		return complete.digest();
	}

	private byte[] createChecksum(File fileName) throws Exception {
		return createChecksum(new FileInputStream(fileName));
	}

	private String getMD5Checksum() {
		try {
			byte[] b = createChecksum(super.getPlugin().getResource(this.file.getName()));
			String result = "";

			for (int i=0; i < b.length; i++)
				result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );

			return result;
		} catch (Exception ex) {
			return null;
		}
	}

	private String getMD5Checksum(File fileName) {
		try {
			byte[] b = createChecksum(fileName);
			String result = "";

			for (int i=0; i < b.length; i++)
				result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );

			return result;
		} catch (Exception ex) {
			return null;
		}
	}

	public CSVFile(JavaPlugin plugin, final String fileName) {
		this(plugin, fileName, false);
	}

	public CSVFile(JavaPlugin plugin, final String fileName, final boolean replace) {
		super(plugin);
		this.file = new File(super.getPlugin().getDataFolder(), fileName);

		if (!file.exists()) {
			super.getPlugin().saveResource(fileName, replace);
			Log.console("Saving %s (MD5: %s)", fileName, getMD5Checksum(this.file));
		} else {
			if (replace && !getMD5Checksum().equals(getMD5Checksum(this.file)))
				super.getPlugin().saveResource(fileName, replace);
		}
	}

	public List<String> getLines() {
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(this.file));

			try {
				final List<String> lines = new ArrayList<String>();

				do {
					final String line = reader.readLine();
					if (line == null) break; else lines.add(line);
				} while (true);

				return lines;
			} finally {
				reader.close();
			}
		} catch (IOException ex) {
			Log.console(ex);
			return Collections.emptyList();
		}
	}

}