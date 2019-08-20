package com.shmkane.sellstick.Configs;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.shmkane.sellstick.Configs.PriceConfig;

/**
 * Handles the opeations of the config.yml
 * @author shmkane
 *
 */
public class PriceConfig {
	/** Instance of PriceConfig **/
	public static PriceConfig instance = new PriceConfig();
	/** Instance of the file **/
	public File conf;

	/**
	 * Sets up default Prices file with some arbitrary values.
	 * @param dir Location of the file
	 */
	public void setup(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.conf = new File(dir + File.separator + "prices.yml");
		if (!this.conf.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);

			config.set("prices.SULPHUR", 1.02);
			config.set("prices.RED_ROSE", 0.76);
			config.set("prices.LEATHER", 2.13);
			config.set("prices.COOKED_BEEF", 0.01);
			config.set("prices.BONE", 5.00);
			config.set("prices.1", 0.42);
			config.set("prices.STONE:2", 0.22);
			config.set("prices.STONE:3", 0.02);
			config.set("prices.1:4", 0.01);
			config.set("prices.1:5", 0.07);
			config.set("prices.46", 1.08);

			try {
				config.save(this.conf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the PriceConfig
	 * @return FileConfiguration object of Prices.yml
	 */
	public FileConfiguration getConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
		return config;
	}

	/**
	 * Method writes to the config file
	 * @param dir Location of the file
	 * @param loc Name of the field to edit
	 * @param obj Object with new information to set/replace loc.
	 */
	public void write(File dir, String loc, Object obj) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.conf = new File(dir + File.separator + "prices.yml");

		getConfig().set(loc, obj);
		try {
			getConfig().save(this.conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
