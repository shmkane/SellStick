package com.acropolismc.play.sellstick;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.acropolismc.play.sellstick.Configs.PriceConfig;
import com.acropolismc.play.sellstick.Configs.StickConfig;
import com.earth2me.essentials.Essentials;

import net.milkbowl.vault.economy.Economy;

// @author shmkane
public class SellStick extends JavaPlugin {

	public Essentials ess;
	private static Economy econ = null;
	private static final Logger log = Logger.getLogger("Minecraft");

	public void onEnable() {
		// Hook into essentials.
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			log.info("[Sellstick] Essentials found");
			ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}

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

		//plot

		//Checks for FactionsUUID
		factions = getServer().getPluginManager().getPlugin("Factions");
		if (factions != null && factions.isEnabled())
			log.info("[Sellstick] Hooking into FactionsUUID/Savage");
		else
			log.warning("[Sellstick] Tried to hook into FactionsUUID/Savage but failed!");

		if (StickConfig.instance.useEssentialsWorth) {
			if (ess == null || !ess.isEnabled()) {
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

	/**
	 * This will send a player a message. If message is empty, it wont send
	 * anything.
	 * 
	 * @param sender The target player
	 * @param msg    the message
	 */
	public void msg(CommandSender sender, String msg) {
		if (msg.length() == 0) {
			return;
		}

		sender.sendMessage(StickConfig.instance.prefix + msg);
	}
}
