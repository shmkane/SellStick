package com.acropolismc.play.sellstick.Configs;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StickConfig {
	public static StickConfig instance = new StickConfig();
	public File conf;

	public String name;
	public String item;

	public List<String> lore;
	public String finiteLore;
	public String infiniteLore;
	public int durabilityLine;
	public String prefix;
	public String sellMessage;
	public String noPerm;
	public String territoryMessage;
	public String nothingWorth;
	public String brokenStick;
	public String giveMessage;
	public String receiveMessage;
	public boolean glow;

	public boolean useEssentialsWorth;

	public void loadValues() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);

		this.name = config.getString("DisplayName").replace("&", "§");
		this.item = config.getString("ItemType").toUpperCase().replace("&", "§");
		this.glow = config.getBoolean("Glow");

		this.lore = config.getStringList("StickLore");
		this.finiteLore = config.getString("FiniteLore").replace("&", "§");
		this.infiniteLore = config.getString("InfiniteLore").replace("&", "§");

		this.durabilityLine = config.getInt("DurabilityLine");

		this.prefix = config.getString("MessagePrefix").replace("&", "§");
		this.sellMessage = config.getString("SellMessage").replace("&", "§");
		this.noPerm = config.getString("NoPermissionMessage").replace("&", "§");
		this.territoryMessage = config.getString("InvalidTerritoryMessage").replace("&", "§");
		this.nothingWorth = config.getString("NotWorthMessage").replace("&", "§");
		this.brokenStick = config.getString("BrokenStick").replace("&", "§");
		this.giveMessage = config.getString("GiveMessage").replace("&", "§");
		this.receiveMessage = config.getString("ReceiveMessage").replace("&", "§");
		this.useEssentialsWorth = config.getBoolean("UseEssentialsWorth");
	}

	public void setup(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}

		this.conf = new File(dir + File.separator + "config.yml");

		if (!this.conf.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);

			/*
			 * Default config options
			 */

			config.set("DisplayName", "&cSellStick");
			config.set("ItemType", "STICK");
			config.get("Glow", true);

			List<String> lore = Arrays.asList("&c&lLeft&c click on a chest to sell items inside!",
					"&cSellStick by &oshmkane");
			config.set("StickLore", lore);
			config.set("FiniteLore", "&c%remaining% &fremaining uses");
			config.set("InfiniteLore", "&4Infinite &cuses!");
			config.set("DurabilityLine", 2);
			config.set("MessagePrefix", "&6[&eSellStick&6] &e");
			config.set("SellMessage", "&cYou sold items for &f%price% &cand now have &f%balance%");
			config.set("NoPermissionMessage", "&cSorry, you don't have permission for this!");
			config.set("InvalidTerritoryMessage", "&cYou can't use sell stick outside your plot!");
			config.set("NotWorthMessage", "&cNothing worth selling inside");
			config.set("BrokenStick", "&cYour sellstick broke!(Ran out of uses)");
			config.set("GiveMessage", "&aYou gave &e%player%& &e&l%amount% &asell sticks!");
			config.set("ReceiveMessage", "&aYou've received &e&l%amount% &asell sticks!");
			config.set("UseEssentialsWorth", false);

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
