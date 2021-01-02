package com.shmkane.sellstick.configs;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.shmkane.sellstick.SellStick;

/**
 * Handles the operations of the config.yml
 *
 * @author shmkane
 * @author BomBardyGamer
 */
public final class StickConfig {

    private final SellStick plugin;

    public StickConfig(final SellStick plugin) {
        this.plugin = plugin;

        loadValues();
        loadSellInterface();
    }

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
     */
    public List<String> lore;

    /**
     * Lore if finite
     */
    public String finiteLore;

    /**
     * Lore if infinite
     */
    public String infiniteLore;

    /**
     * Which line the durability will be shown on.
     */
    public int durabilityLine;

    /**
     * Message is prefixed with this
     */
    public String prefix;

    /**
     * Full message sent to user
     */
    public String sellMessage;

    /**
     * Message sent if user doesn't have permission to use sellstick
     */
    public String noPerm;

    /**
     * Message sent if user can't use sellstick there
     */
    public String territoryMessage;

    /**
     * Message sent if items are worthless
     */
    public String nothingWorth;

    /**
     * Message sent if sellstick breaks
     */
    public String brokenStick;

    /**
     * If they try to do something other than selling with the sellstick...
     */
    public String nonSellingRelated;

    /**
     * Message sent when giving someone a sellstick
     */
    public String giveMessage;

    /**
     * Message received if you get a sellstick
     */
    public String receiveMessage;

    /**
     * Whether or not to make sellstick glow (enchant effect)
     */
    public boolean glow;

    /**
     * Whether or not to use essentials worth
     */
    public boolean useEssentialsWorth;

    /**
     * Whether or not to use ShopGUI+
     */
    public boolean useShopGUI;

    /**
     * Whether or not to play a sound on use of sellstick
     */
    public boolean sound;

    /**
     * Whether or not to print debug messages to console
     */
    public boolean debug;

    private SellingInterface sellInterface = SellingInterface.BUILTIN;

    /**
     * Takes values from the config and loads them into variables.
     */
    @SuppressWarnings("ConstantConditions") // Fine to do this as if the values are null something is wrong
    private void loadValues() {
        FileConfiguration config = plugin.getConfig();

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
        this.nonSellingRelated = ChatColor.translateAlternateColorCodes('&', config.getString("NonSellingRelated"));
        this.giveMessage = ChatColor.translateAlternateColorCodes('&', config.getString("GiveMessage"));
        this.receiveMessage = ChatColor.translateAlternateColorCodes('&', config.getString("ReceiveMessage"));
        this.useEssentialsWorth = config.getBoolean("UseEssentialsWorth");
        this.useShopGUI = config.getBoolean("UseShopGUI");
        this.sound = config.getBoolean("UseSound");
        this.debug = config.getBoolean("debug");
    }

    private void loadSellInterface() {
        if (useEssentialsWorth && useShopGUI) {
            plugin.getLogger().warning("Essentials worth and ShopGUI+ enabled, defaulting to ShopGUI+");
            plugin.getLogger().warning("Edit the config to remove this error.");
            sellInterface = SellingInterface.SHOPGUI;
        } else if (!useEssentialsWorth && useShopGUI) {
            plugin.getLogger().info("Using ShopGUI worth for prices");
            sellInterface = SellingInterface.SHOPGUI;
        } else if (useEssentialsWorth) {
            plugin.getLogger().info("Using essentials for prices");
            sellInterface = SellingInterface.ESSENTIALS;
        } else {
            plugin.getLogger().info("Essentials worth and ShopGUI+ disabled, defaulting to prices.yml");
            sellInterface = SellingInterface.BUILTIN;
        }

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
     */
    public enum SellingInterface {
        BUILTIN, // The built-in prices.yml file
        ESSENTIALS, // Essentials worth
        SHOPGUI // ShopGUI+
    }
}
