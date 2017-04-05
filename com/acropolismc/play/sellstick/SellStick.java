package com.acropolismc.play.sellstick;

import java.util.logging.Logger;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.acropolismc.play.sellstick.Configs.PriceConfig;
import com.acropolismc.play.sellstick.Configs.StickConfig;

import net.milkbowl.vault.economy.Economy;

// @author shmkane
public class SellStick extends JavaPlugin{

	private static Economy econ = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    
	public void onEnable() {
		//Commands
		this.getCommand("sellstick").setExecutor(new SellStickCommand(this));	
		//Listeners
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		//HandleConfig
        this.saveDefaultConfig();
		StickConfig.instance.setup(getDataFolder());
		PriceConfig.instance.setup(getDataFolder());
		//Vault
		if (!setupEconomy() ) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}
	
	public Economy getEcon() {
		//Return instance of economy.
		return SellStick.econ;
	}
	
	//Setup Vault
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
