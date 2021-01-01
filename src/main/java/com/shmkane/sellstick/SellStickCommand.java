package com.shmkane.sellstick;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;

import com.shmkane.sellstick.Configs.StickConfig;

/**
 * Handles the /sellstick Command
 *
 * @author shmkane
 */
public class SellStickCommand implements CommandExecutor, TabExecutor {

    /**
     * Instance of the plugin
     **/
    private SellStick plugin;

    /**
     * Constructor of SellStickCommand Only one of these should be constructed in
     * the onEnable of SellStick.java
     *
     * @param plugin Takes a SellStick object
     */
    public SellStickCommand(SellStick plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            commands.add("give");
            if (sender.hasPermission("sellstick.reload")) {
                commands.add("reload");
            }
        } else if (args.length == 2) {
            commands.add(sender.getName());
        } else if (args.length == 3) {
            commands.add("1");
        } else if (args.length == 4) {
            commands.add("i");
            commands.add("1");
            commands.add("2");
            commands.add("3");
            commands.add("5");
            commands.add("10");
        }
        return commands;
    }

    /**
     * Handle the sellstick command here
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        PluginDescriptionFile pdf = plugin.getDescription();

        if (args.length == 0) {
            sendCommandNotProperMessage(sender, pdf);
            return true;

        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("sellstick.reload")) {
                try {
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                    plugin.msg(sender, ChatColor.RED + "Reloading Plugin");
                    plugin.getServer().getPluginManager().enablePlugin(plugin);
                    plugin.msg(sender, ChatColor.GREEN + "Plugin Reloaded");
                } catch (Exception ex) {
                    plugin.msg(sender, "Something went wrong! Check console for error");
                    System.out.println(ex.getMessage());
                }
            } else {
                plugin.msg(sender, ChatColor.GRAY + "" + ChatColor.ITALIC + pdf.getFullName() + " (MC "
                        + this.plugin.getServer().getVersion() + ") by " + pdf.getAuthors().get(0));
            }
            return true;
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender.hasPermission("sellstick.give")) {
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target != null && target.isOnline()) {

                        int numSticks;

                        try {
                            numSticks = Integer.parseInt(args[2]);
                        } catch (Exception ex) {
                            sendCommandNotProperMessage(sender, pdf);
                            return false;
                        }

                        for (int i = 0; i < numSticks; i++) {
                            /**
                             * This assigns a random string to the item meta so that the item cannot be
                             * stacked
                             */
                            RandomString random = new RandomString(5);
                            String UUID = random.nextString();
                            ItemStack is;
                            try {
                                is = new ItemStack(Objects.requireNonNull(Material.getMaterial(StickConfig.instance.item)));
                            }catch(NullPointerException ex) {
                                SellStick.log.severe(String.format("[%s] - Invalid item set in config. Please read the links I put in the config to fix this.", plugin.getDescription().getName()));
                                return false;
                            }
                            ItemMeta im = is.getItemMeta();

                            List<String> lores = new ArrayList<String>();

                            im.setDisplayName(StickConfig.instance.name + UUID);

                            // Load values from config onto the stick lores array
                            for (int z = 0; z < StickConfig.instance.lore.size(); z++) {
                                lores.add(StickConfig.instance.lore.get(z).replace("&", ChatColor.COLOR_CHAR + ""));
                            }

                            try {
                                lores.add(StickConfig.instance.durabilityLine - 1, "%usesLore%");
                            } catch (IndexOutOfBoundsException e) {
                                plugin.msg(sender, ChatColor.RED + "CONFIG ERROR:");
                                plugin.msg(sender,
                                        ChatColor.RED + "You tried to set a DurabilityLine of "
                                                + (StickConfig.instance.durabilityLine - 1) + " but the lore is "
                                                + lores.size() + " long");
                                plugin.msg(sender,
                                        ChatColor.RED + "Try changing the DurabilityLine value in the config");
                                plugin.msg(sender, ChatColor.RED + "Then, run /sellstick reload");

                                return false;

                            } catch (Exception ex) {
                                plugin.msg(sender, ChatColor.RED
                                        + "Something went wrong. Please check the console for an error message.");
                                SellStick.log.severe(ex.getMessage());
                                return false;
                            }

                            if (args[3].equalsIgnoreCase("infinite") || args[3].equalsIgnoreCase("i")) {
                                lores.set(StickConfig.instance.durabilityLine - 1,
                                        lores.get(StickConfig.instance.durabilityLine - 1).replace("%usesLore%",
                                                StickConfig.instance.infiniteLore));
                            } else {
                                try {
                                    int uses = Integer.parseInt(args[3]);
                                    // otherwise replace it with the remaining uses
                                    lores.set(StickConfig.instance.durabilityLine - 1,
                                            lores.get(StickConfig.instance.durabilityLine - 1).replace("%usesLore%",
                                                    StickConfig.instance.finiteLore.replace("%remaining%", uses + "")));
                                } catch (Exception ex) {
                                    // They typed something stupid here...
                                    sendCommandNotProperMessage(sender, pdf);
                                    return false;
                                }
                            }

                            im.setLore(lores);
                            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            is.setItemMeta(im);

                            if (StickConfig.instance.glow) {
                                is = glow(is);
                            }

                            target.getInventory().addItem(is);
                        }
                        plugin.msg(target, StickConfig.instance.receiveMessage.replace("%amount%",
                                Integer.parseInt(args[2]) + ""));

                        plugin.msg(sender, StickConfig.instance.giveMessage.replace("%player%", target.getName())
                                .replace("%amount%", Integer.parseInt(args[2]) + ""));

                        return true;

                    } else {
                        plugin.msg(sender, ChatColor.RED + "Player not found");
                    }
                } else {
                    plugin.msg(sender, StickConfig.instance.noPerm);
                }
            }
        } else {
            plugin.msg(sender, "" + ChatColor.RED + "Invalid command. Type /Sellstick for help");
        }
        return false;
    }

    /**
     * Sent to 'sender' if their command was invalid.
     * @param sender Sender of the command
     * @param pdf PluginDescriptionFile object
     */
    void sendCommandNotProperMessage(CommandSender sender, PluginDescriptionFile pdf) {
        // They typed something stupid here...
        plugin.msg(sender, ChatColor.GRAY + "" + ChatColor.ITALIC + pdf.getFullName()
                + " (MC " + this.plugin.getServer().getVersion() + ") by " + pdf.getAuthors().get(0));
        if (sender.hasPermission("sellstick.give")) {
            plugin.msg(sender, ChatColor.GREEN
                    + "/SellStick give <player> <amount> (<uses>/infinite)");
        }

    }

    /**
     * @param itemStack Accepts an itemstack
     * @return returns an enchanted item with durability 1(unbreaking)
     */
    public ItemStack glow(ItemStack itemStack) {
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return itemStack;
    }

}