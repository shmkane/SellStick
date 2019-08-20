package com.shmkane.sellstick;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.shmkane.sellstick.PlayerListener;
import com.shmkane.sellstick.SellStick;
import com.shmkane.sellstick.SellStickCommand;
import com.shmkane.sellstick.Configs.PriceConfig;
import com.shmkane.sellstick.Configs.StickConfig;
import com.earth2me.essentials.Essentials;

import net.milkbowl.vault.economy.Economy;

// @author shmkane
public class SellStick extends JavaPlugin {

	/** Holds instance of Essentials **/
	public Essentials ess;
	/** Instance of Vault Economy **/
	private static Economy econ;
	/** Server logger **/
	private static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Initial plugin setup. Creation and loading of YML files.
	 * 
	 * Creates / Loads Config.yml
	 * Creates / Loads Prices.yml
	 * Saves Current Config.
	 * Create instance of Essentials
	 * Hook SellStickCommand executor
	 */
	@Override
	public void onEnable() {
		
		if (!setupEconomy()) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		this.saveDefaultConfig();

		StickConfig.instance.setup(getDataFolder());
		PriceConfig.instance.setup(getDataFolder());
	
		setupEssentials();

		this.getCommand("sellstick").setExecutor(new SellStickCommand(this));
		
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
	}

	@Override
	public void onDisable() {
		log.warning(String.format("[%s] - Attempting to disabling...", getDescription().getName()));
		try {
			ess = null;
			econ = null;
		} catch (Exception ex) {
			log.severe(String.format("[%s] - Was not disabled correctly!", getDescription().getName()));
		} finally {
			log.warning(String.format("[%s] - Attempt complete!", getDescription().getName()));
		}
	}

	public void setupEssentials() {
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			log.info(String.format("[%s] Hooked into essentials!", getDescription().getName()));
			ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}

		if (StickConfig.instance.useEssentialsWorth) {
			if (ess == null || !ess.isEnabled()) {
				log.warning(String.format("[%s] Trying to use essentials worth but essentials not found!",
						getDescription().getName()));
			} else {
				log.info(String.format("[%s] Using essentials worth!", getDescription().getName()));
			}
		}

	}

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

	public Economy getEcon() {
		return SellStick.econ;
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
