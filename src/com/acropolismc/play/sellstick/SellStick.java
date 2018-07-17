package com.acropolismc.play.sellstick;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.acropolismc.play.sellstick.Configs.PriceConfig;
import com.acropolismc.play.sellstick.Configs.StickConfig;
import com.earth2me.essentials.Essentials;

import net.milkbowl.vault.economy.Economy;

// @author shmkane
public class SellStick extends JavaPlugin {

	public Plugin essentialsPlugin;
	public Essentials ess;
	private static Economy econ = null;
	private static final Logger log = Logger.getLogger("Minecraft");

	public void onEnable() {
		// Hook into essentials.
		essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
		if (essentialsPlugin.isEnabled() && (essentialsPlugin instanceof Essentials)) {
			log.info("[Sellstick] Essentials found");
			this.essentialsPlugin = (Essentials) essentialsPlugin;
		}

		// Essentials
		ess = (Essentials) essentialsPlugin;

		// Commands
		this.getCommand("sellstick").setExecutor(new SellStickCommand(this));
		// Listeners
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		// Safe the default config
		this.saveDefaultConfig();
		// Setup the other configs.
		StickConfig.instance.setup(getDataFolder());
		PriceConfig.instance.setup(getDataFolder());
		// Vault
		if (!setupEconomy()) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// There's probably a better way for this, but this will do for now.

		Plugin factions;
		Plugin skyblock;

		//Checks for FactionsUUID
		if (StickConfig.instance.usingFactionsUUID || StickConfig.instance.usingSavageFactions) {
			factions = getServer().getPluginManager().getPlugin("Factions");
			if (factions != null && factions.isEnabled())
				log.info("[Sellstick] Hooking into FactionsUUID/Savage");
			else
				log.warning("[Sellstick] Tried to hook into FactionsUUID/Savage but failed!");
			
			
		//Check for Legacy Factions
		} else if (StickConfig.instance.usingLegacyFactions) {
			factions = getServer().getPluginManager().getPlugin("Factions");
			if (factions != null && factions.isEnabled())
				log.info("[Sellstick] Hooking into LegacyFactions");
			else
				log.warning("[Sellstick] Tried to hook into LegacyFactions but failed!");
			
			
		//Check for MCoreFactions
		} else if (StickConfig.instance.usingMCoreFactions) {
			factions = getServer().getPluginManager().getPlugin("MassiveCore");
			if (factions != null && factions.isEnabled())
				log.info("[Sellstick] Hooking into MCoreFactions");
			else
				log.warning("[Sellstick] Tried to hook into McoreFactions but failed!");
			
			
		//Check for SavageFactions
		} else {//If no Factions plugin was set.
			log.warning("[Sellstick] No factions plugin enabled in config! Factions features won't work!");
			if(!StickConfig.instance.usingSkyblock) {
				log.warning("[Sellstick] No SkyBlock plugin enabled in config! Plugin won't function normally!");
			}
		}
		//Check for SkyBlock
		if (StickConfig.instance.usingSkyblock) {
			skyblock = getServer().getPluginManager().getPlugin("ASkyBlock");
			if (skyblock != null && skyblock.isEnabled())
				log.info("[Sellstick] Hooking into ASkyBlock");
			else
				log.warning("[Sellstick] Tried to hook into ASkyBlock but failed!");
		}

		if (StickConfig.instance.useEssentialsWorth) {
			if (essentialsPlugin == null || !essentialsPlugin.isEnabled()) {
				log.warning("[Sellstick] Trying to use essentials worth but essentials not found!");
			} else {
				log.info("[Sellstick] Hooked into Essentials Worth");
			}
		}
	}

	public Economy getEcon() {
		// Return instance of economy.
		return SellStick.econ;
	}

	// Setup Vault
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
}
