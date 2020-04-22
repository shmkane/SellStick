package com.shmkane.sellstick;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.shmkane.sellstick.Configs.PriceConfig;
import com.shmkane.sellstick.Configs.StickConfig;

import net.milkbowl.vault.economy.Economy;

/**
 * SellStick is a MC plugin that allows customizable selling of
 * chest contents.
 *
 * @author shmkane
 */
public class SellStick extends JavaPlugin {

    /**
     * Server logger
     **/
    public static final Logger log = Logger.getLogger("Minecraft");
    /**
     * Instance of Vault Economy
     **/
    private static Economy econ;
    /**
     * Holds instance of Essentials
     **/
    public Essentials ess;

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

        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupEssentials();

        if (Bukkit.getPluginManager().isPluginEnabled("ShopGuiPlus")) {
            if (!StickConfig.instance.useShopGUI) {
                log.warning(String.format("[%s] ShopGUI+ was found but not enabled in the config!", getDescription().getName()));
            }
        }

        this.saveDefaultConfig();

        StickConfig.instance.setup(getDataFolder());
        PriceConfig.instance.setup(getDataFolder());

        this.getCommand("sellstick").setExecutor(new SellStickCommand(this));
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    /**
     * Attempt to disable plugin. Reset the values of some instance varibles.
     */
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

    /**
     * Checks if Essentials is available to be hooked into.
     */
    public void setupEssentials() {
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            log.info(String.format("[%s] Essentals was found", getDescription().getName()));
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
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Returns an instance of vault.
     *
     * @return
     */
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
