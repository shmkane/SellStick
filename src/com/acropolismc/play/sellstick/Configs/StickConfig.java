package com.acropolismc.play.sellstick.Configs;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.massivecraft.factions.integration.Essentials;

public class StickConfig {
	public static StickConfig instance = new StickConfig();
	public File conf;

	public String name;
	public String item;
	public boolean onlyOwn;

	public List<String> lore;

	public String usesLore;

	public String infiniteLore;


	public String prefix;
	public String sellMessage;
	public String noPerm;
	public String territoryMessage;
	public String nothingWorth;

	public String brokenStick;

	public boolean useEssentialsWorth;





	public void loadValues() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);


		
		this.name = config.getString("DisplayName").replace("&", "§");
		this.item = config.getString("ItemType").toUpperCase().replace("&", "§");
		this.onlyOwn = config.getBoolean("Only_In_Own_Territory");

		this.lore = config.getStringList("StickLore");

		this.usesLore = config.getString("FiniteLore").replace("&", "§");

		this.infiniteLore = config.getString("InfiniteLore").replace("&", "§");



		this.prefix = config.getString("MessagePrefix").replace("&", "§");
		this.sellMessage = config.getString("SellMessage").replace("&", "§");
		this.noPerm = config.getString("NoPermissionMessage").replace("&", "§");
		this.territoryMessage = config.getString("InvalidTerritoryMessage").replace("&", "§");
		this.nothingWorth = config.getString("NotWorthMessage").replace("&", "§");	
		this.brokenStick =  config.getString("BrokenStick").replace("&", "§");

		this.useEssentialsWorth = config.getBoolean("UseEssentialsWorth");
	}

	public void setup(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}

		this.conf = new File(dir + File.separator + "config.yml");

		if (!this.conf.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);

			config.set("DisplayName", "&cSellStick");
			config.set("ItemType", "STICK");
			config.set("Only_In_Own_Territory", true);

			List<String> lore = Arrays.asList("&c&lLeft&c click on a chest to sell items inside!", "&cSellStick by &oshmkane");
			config.set("StickLore", lore);
			config.set("FiniteLore", "&c%remaining% &fremaining uses");
			config.set("InfiniteLore", "&4Infinite &cuses!");

			config.set("MessagePrefix", "&6[&eSellStick&6] &e");	
			config.set("SellMessage", "&cYou sold items for &f%price% &cand now have &f%balance%");
			config.set("NoPermissionMessage", "&cSorry, you don't have permission for this!");
			config.set("InvalidTerritoryMessage", "&cYou can't use sell stick outside your territory!");
			config.set("NotWorthMessage", "&cNothing worth selling inside");

			config.set("BrokenStick", "&cYour sellstick broke!(Ran out of uses)");

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
