package com.shmkane.sellstick.Configs;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.shmkane.sellstick.SellStick;

import net.md_5.bungee.api.ChatColor;

/**
 * Handles the operations of the config.yml
 *
 * @author shmkane
 */
public class StickConfig {
    /**
     * Instance of the Config
     **/
    public static StickConfig instance = new StickConfig();
    /**
     * Instance of the file
     **/
    public File conf;


    /**
     * Display name of the stick
     **/
    public String name;
    /**
     * String version of item
     **/
    public String item;


    /**
     * Item Lore
     **/
    public List<String> lore;
    /**
     * Lore if finite
     **/
    public String finiteLore;
    /**
     * Lore if infinite
     **/
    public String infiniteLore;
    /**
     * Which line the durability will be shown on.
     **/
    public int durabilityLine;
    /**
     * Message is prefixed with this
     **/
    public String prefix;
    /**
     * Full message sent to user
     **/
    public String sellMessage;
    /**
     * Message sent if user doesn't have permission to use sellstick
     **/
    public String noPerm;
    /**
     * Message sent if user can't use sellstick there
     **/
    public String territoryMessage;
    /**
     * Message sent if items are worthless
     **/
    public String nothingWorth;
    /**
     * Message sent if sellstick breaks
     **/
    public String brokenStick;
    /**
     * Message sent when giving someone a sellstick
     **/
    public String giveMessage;
    /**
     * Message received if you get a sellstick
     **/
    public String receiveMessage;
    /**
     * Whether or not to make sellstick glow (enchant effect)
     **/
    public boolean glow;
    /**
     * Whether or not to use essentials worth
     **/
    public boolean useEssentialsWorth;
    /**
     * Whether or not to use ShopGUI+
     **/
    public boolean useShopGUI;
    /**
     * Whether or not to play a sound on use of sellstick
     **/
    public boolean sound;
    /**
     * Whether or not to print debug messages to console
     **/
    public boolean debug;
    private SellingInterface sellInterface = SellingInterface.PRICESYML;

    /**
     * Takes values from the config and loads them into variables.
     */
    public void loadValues() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);

        this.name = ChatColor.translateAlternateColorCodes('&', config.getString("DisplayName"));
        this.item = ChatColor.translateAlternateColorCodes('&', config.getString("ItemType").toUpperCase());
        this.glow = config.getBoolean("Glow");

        this.lore = config.getStringList("StickLore");
        this.finiteLore = ChatColor.translateAlternateColorCodes('&', config.getString("FiniteLore"));
        this.infiniteLore = ChatColor.translateAlternateColorCodes('&', config.getString("InfiniteLore"));

        this.durabilityLine = config.getInt("DurabilityLine");

        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("MessagePrefix"));
        this.sellMessage = ChatColor.translateAlternateColorCodes('&', config.getString("SellMessage"));
        this.noPerm = ChatColor.translateAlternateColorCodes('&', config.getString("NoPermissionMessage"));
        this.territoryMessage = ChatColor.translateAlternateColorCodes('&', config.getString("InvalidTerritoryMessage"));
        this.nothingWorth = ChatColor.translateAlternateColorCodes('&', config.getString("NotWorthMessage"));
        this.brokenStick = ChatColor.translateAlternateColorCodes('&', config.getString("BrokenStick"));
        this.giveMessage = ChatColor.translateAlternateColorCodes('&', config.getString("GiveMessage"));
        this.receiveMessage = ChatColor.translateAlternateColorCodes('&', config.getString("ReceiveMessage"));
        this.useEssentialsWorth = config.getBoolean("UseEssentialsWorth");
        this.useShopGUI = config.getBoolean("UseShopGUI");
        this.sound = config.getBoolean("UseSound");
        this.debug = config.getBoolean("debug");
    }

    /**
     * If the file DNE, create it.
     *
     * @param dir Location and name of the file to be created
     */
    public void setup(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }

        this.conf = new File(dir + File.separator + "config.yml");

        if (!this.conf.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);

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
            config.set("InvalidTerritoryMessage", "&cYou can't use sell stick here!");
            config.set("NotWorthMessage", "&cNothing worth selling inside");
            config.set("BrokenStick", "&cYour sellstick broke!(Ran out of uses)");
            config.set("GiveMessage", "&aYou gave &e%player%& &e&l%amount% &asell sticks!");
            config.set("ReceiveMessage", "&aYou've received &e&l%amount% &asell sticks!");
            config.set("UseEssentialsWorth", false);
            config.set("UseShopGUI", false);
            config.set("UseSound", true);
            config.set("debug", false);

            try {
                config.save(this.conf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadValues();
        handleInterface();
    }

    private void handleInterface() {
        if (useEssentialsWorth && useShopGUI) {
            SellStick.log.warning("Server has EssentialsWorth AND ShopGUI Selected.");
            SellStick.log.warning("Defaulting to ShopGUI");
            SellStick.log.warning("Edit the config to remove this error.");
            sellInterface = SellingInterface.SHOPGUI;

        } else if (!useEssentialsWorth && useShopGUI) {

            SellStick.log.info("Using ShopGUI worth for prices");
            sellInterface = SellingInterface.SHOPGUI;


        } else if (useEssentialsWorth && !useShopGUI) {

            SellStick.log.info("Using essentials for prices");
            sellInterface = SellingInterface.ESSWORTH;


        } else if (!useEssentialsWorth && !useShopGUI) {

            SellStick.log.info("Using prices.yml for prices");
            sellInterface = SellingInterface.PRICESYML;

        } else {
            SellStick.log.severe("Couldn't determine what to use.");
            SellStick.log.severe("Defaulting to PricesYML");
            SellStick.log.severe("Edit the config to remove this error.");
            sellInterface = SellingInterface.PRICESYML;
        }

    }

    /**
     * Returns an instance of the config
     *
     * @return FileConfiguration object of the config file
     */
    public FileConfiguration getConfig() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(this.conf);
        return config;
    }

    /**
     * Write values to the config
     *
     * @param dir Location of the file
     * @param loc Name of the var in the file
     * @param obj Object to write
     */
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

    /**
     * Returns what way to get prices
     *
     * @return The method of getting prices
     */
    public SellingInterface getSellInterface() {
        return sellInterface;
    }

    /**
     * What interface to use to sell
     **/
    public enum SellingInterface {
        PRICESYML,
        ESSWORTH,
        SHOPGUI
    }
}
