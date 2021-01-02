package com.shmkane.sellstick.configs;

import com.shmkane.sellstick.SellStick;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Handles the operations of the prices.yml file
 *
 * @author shmkane
 * @author BomBardyGamer
 */
public final class PriceConfig {

    private final ConfigurationSection prices;

    public PriceConfig(final SellStick plugin) {

        prices = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "prices.yml")).getConfigurationSection("prices");
    }

    /**
     * Return a set of the prices;
     *
     * @return The prices in the config.
     */
    public ConfigurationSection getPrices() {
        return prices;
    }
}
