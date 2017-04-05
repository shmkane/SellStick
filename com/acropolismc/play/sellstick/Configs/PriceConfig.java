package com.acropolismc.play.sellstick.Configs;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PriceConfig {

	public static PriceConfig instance = new PriceConfig();
	public File conf;

	public void setup(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		this.conf = new File(dir + File.separator + "prices.yml");
		if (!this.conf.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
			config.set("prices.EXAMPLE", 0);
			config.set("prices.STONE", 0);
			config.set("prices.IRON_INGOT", 0);
			try {
				config.save(this.conf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public FileConfiguration getConfig(){
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
		return config;
	}

	public void write(File dir, String loc, Object obj){
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
