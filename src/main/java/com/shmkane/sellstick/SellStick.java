package com.shmkane.sellstick;

import com.earth2me.essentials.Essentials;
import com.shmkane.sellstick.configs.PriceConfig;
import com.shmkane.sellstick.configs.StickConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SellStick is a MC plugin that allows customizable selling of
 * chest contents.
 *
 * @author shmkane
 */
public final class SellStick extends JavaPlugin {

    /**
     * Instance of Vault Economy
     */
    private static Economy economy = null;

    private StickConfig stickConfig;
    private PriceConfig priceConfig;

    /**
     * Initial plugin setup. Creation and loading of YML files.
     * <p>
     * Creates / Loads Config.yml
     * Creates / Loads Prices.yml
     * Saves Current Config.
     * Create instance of Essentials
     * Hook SellStickCommand executor
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("prices.yml", false);

        stickConfig = new StickConfig(this);
        priceConfig = new PriceConfig(this);

        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Vault is required for this plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupEssentials();

        if (Bukkit.getPluginManager().isPluginEnabled("ShopGuiPlus")) {
            if (stickConfig.useShopGUI) {
                getLogger().warning("ShopGUI+ was found but not enabled in the config!");
            }
        }

        getCommand("sellstick").setExecutor(new SellStickCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    /**
     * Checks if Essentials is available to be hooked into.
     */
    public void setupEssentials() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                getLogger().info("Essentials was found");
                Essentials ess = Essentials.getPlugin(Essentials.class);

                if (stickConfig.useEssentialsWorth) {
                    if (ess == null) {
                        getLogger().warning("Trying to use essentials worth but essentials not found!");
                    } else {
                        getLogger().info("Using essentials worth!");
                    }
                }
            } else {
                getLogger().warning("Essentials not found");
            }
        } catch (Exception ex) {
            getLogger().warning("Something went wrong enabling Essentials. If you don't use it, you can ignore this message:");
            getLogger().warning(ex.getMessage());
        }
    }

    /**
     * Attempts to hook into Vault.
     *
     * @return If vault is available or not.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }

    public StickConfig getStickConfig() {
        return stickConfig;
    }

    public PriceConfig getPriceConfig() {
        return priceConfig;
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

        sender.sendMessage(stickConfig.prefix + msg);
    }
}
