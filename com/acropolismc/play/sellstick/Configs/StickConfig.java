package com.acropolismc.play.sellstick.Configs;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StickConfig {
	public static StickConfig instance = new StickConfig();
	public File conf;

	public String servername;
	public String prefix;
	public String noPermission;
	public String item;
	public String itemName;
	public String itemLore;
	public int uses;
	public boolean infiniteUses;
	public boolean onlyOwn;
	public String notYourTerritory;

	public void loadValues() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
		this.servername = config.getString("servername").replace("&", "§");
		this.prefix = config.getString("prefix").replace("&", "§");
		this.noPermission = config.getString("noPermission").replace("&", "§");
		this.item = config.getString("sellstick.item");
		this.itemName = config.getString("sellstick.displayname").replace("&", "§");
		this.itemLore = config.getString("sellstick.lore").replace("&", "§");
		this.uses = config.getInt("sellstick.uses");
		this.infiniteUses = config.getBoolean("sellstick.infinite");
		this.onlyOwn = config.getBoolean("sellstick.onlyOwn");
		this.notYourTerritory = config.getString("sellstick.notYourTerritory").replace("&", "§");
	}

	public void setup(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.conf = new File(dir + File.separator + "config.yml");
		if (!this.conf.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
			config.set("servername", "AcropolisMC");
			config.set("prefix", "&8[&a&lAcropolis&8] &a");
			config.set("noPermission", "&cSorry, you don't have permission for this!");
			config.set("sellstick.item", "STICK");
			config.set("sellstick.displayname", "&cSellStick");
			config.set("sellstick.lore", "&cLeft click on a chest to sell items inside!");
			config.set("sellstick.infinite", false);
			config.set("sellstick.uses", 10);
			config.set("sellstick.onlyOwn", true);
			config.set("sellstick.notYourTerritory", "&cYou can't use sell stick outside your territory!");
			try {
				config.save(this.conf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		loadValues();
	}

	public FileConfiguration getConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
		return config;
	}

	public void write(File dir, String loc, Object obj) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.conf = new File(dir + File.separator + "config.yml");

		getConfig().set(loc, obj);
		try {
			getConfig().save(this.conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		loadValues();
	}
}
