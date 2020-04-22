package com.shmkane.sellstick;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.shmkane.sellstick.Configs.PriceConfig;
import com.shmkane.sellstick.Configs.StickConfig;
import com.shmkane.sellstick.Configs.StickConfig.SellingInterface;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.player.PlayerDataNotLoadedException;
import net.milkbowl.vault.economy.EconomyResponse;

/**
 * The PlayerListener class will handle all of the events from the player.
 * Furthermore, it contains code that will take a stick's lore/display name, and
 * chest interaction events.
 *
 * @author shmkane
 */
public class PlayerListener implements Listener {
    /**
     * Instance of the plugin
     **/
    private SellStick plugin;

    /**
     * Construct object, pass instance of SellStick
     *
     * @param plugin The passed in instance
     */
    public PlayerListener(SellStick plugin) {
        this.plugin = plugin;
    }

    /**
     * Given a string, check if it's numeric.
     *
     * @param str Given a string
     * @return Check if it is a number.
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the stick is infinite. An infinte stick means that it can be
     * used regardless of it's durabaility. For that matter, it's infinite if the
     * item lore matches the infinite lore stated in the config.
     * <p>
     * IMPORTANT: If you change the infiniteLore in the config, you may break the
     * sticks with the old lore.
     *
     * @param lores Given these lores from the stick
     * @return True if infinite stick
     */
    public boolean isInfinite(List<String> lores) {
        return lores.get(StickConfig.instance.durabilityLine - 1).equalsIgnoreCase(StickConfig.instance.infiniteLore);
    }

    /**
     * This method was created incase someone wants to put "%remaining uses out of
     * 50% Where the last int is NOT the remaining uses.
     * <p>
     * There's probably a more efficient way to do this but I haven't gotten around
     * to recoding it and it hasn't given an issue yet.
     *
     * @param lores Takes a string list
     * @return finds the uses in the lores and returns as int.
     */
    public int getUsesFromLore(List<String> lores) {

        /*
         * Get all the lores. lore[1] contains the uses/infinite info. We need to get
         * the uses. The reason for all of this is incase someone puts %remaining% uses
         * out of 50, in the config. We need to be able to get the first number(or the
         * lower number in most cases) because an annoying person will put out of 50 you
         * have %remaining% uses left. Again, the only purpose of this is to Get the #
         * of uses if theres multiple numbers in the lore We loop through the
         * String(lore at index 1) and check all the indexes
         */

        String found = parseDurabilityLine(lores);

        // We now take that found string, and split it at every "-"
        String[] split = found.split("-");

        List<Integer> hold = new ArrayList<Integer>();

        // We take the split array, and loop thru it
        for (int i = 0; i < split.length; i++) {
            // If we find a number in it,
            if (isNumeric(split[i])) {
                // We hold onto that number
                hold.add(Integer.parseInt(split[i]));
            }
        }
        // Now we just do a quick loop through the hold array and find the
        // lowest number.

        int min = -2;
        try {
            min = hold.get(0);
        } catch (Exception ex) {
            SellStick.log.severe(StickConfig.instance.durabilityLine + "");
            SellStick.log.severe("The problem seems to be that your sellstick useline number has changed.");
            SellStick.log.severe(ex.toString());
        }

        for (int i = 0; i < hold.size(); i++) {
            if (hold.get(i) < min) {
                min = hold.get(i);
            }
        }
        return min;
    }

    /**
     * Loops through the lores and determines if lines are valid and if they're
     * color codes.
     * <p>
     * This method will go through and find a digit (i). If a digit is found, get
     * the char before it (i-1). if (i-1) is the color_char(�).
     * <p>
     * It will make a string that looks something like "----a--b-4--" where dashes
     * represent something, and everything remaining represents color codes.
     * <p>
     * It's not worth understanding how this method works if I'm being honest. Just
     * don't break it.
     * <p>
     * Also parses the durability from all it.
     * <p>
     * Tried to make this as idiot-proof as possible, but made code sloppy
     *
     * @param lores Lores of the sellstick
     * @return a specially "encrypted" string that can be read to make sense by
     * further methods.
     */
    public String parseDurabilityLine(List<String> lores) {
        String found = "";
        int duraLine = StickConfig.instance.durabilityLine;

        for (int i = 0; i < lores.get(duraLine - 1).length(); i++) {
            if (Character.isDigit(lores.get(duraLine - 1).charAt(i))) {

                if (i != 0) {
                    // Check to see if the index before is the & sign (If its a color code)
                    if (lores.get(duraLine - 1).charAt(i - 1) != ChatColor.COLOR_CHAR) {
                        // And if it isnt, keep track of it
                        found += lores.get(duraLine - 1).charAt(i);
                    } else {
                        // If it IS a color code, simply ignore it
                        found += "-";
                    }
                    // But if it's index == 0
                } else {
                    // There can't be a & before it, so keep track of it
                    found += lores.get(duraLine - 1).charAt(i);
                }
            } else {
                // Otherwise we insert a "-"
                found += "-";
            }
        }

        return found;
    }

    /**
     * Method for checking if a player just clicked a chest with a sellstick
     *
     * @param p Player that clicked the chest
     * @param e On a player interact event
     * @return True if the item in hand was a sellstick && player clicked a chest
     */
    @SuppressWarnings("deprecation")
    public boolean didClickChestWithSellStick(Player p, PlayerInteractEvent e) {
        Material sellItem = Material.getMaterial(StickConfig.instance.item.toUpperCase());

        if (p.getItemInHand().getType() == sellItem) {
            if (p.getItemInHand().getItemMeta().getDisplayName() != null
                    && p.getItemInHand().getItemMeta().getDisplayName().startsWith(StickConfig.instance.name)) {
                return e.getClickedBlock().getType() == Material.CHEST
                        || e.getClickedBlock().getType() == Material.TRAPPED_CHEST;
            }
        }

        return false;
    }

    /**
     * Gets uses from the lores
     *
     * @param p     Send message to this player
     * @param lores SellStick lores List
     * @return returns the number of uses the stick has
     */
    public int handleUses(Player p, List<String> lores) {
        int uses = -1;
        if (!isInfinite(lores)) {
            uses = getUsesFromLore(lores);
        }
        if (uses == -2) {
            // This should honestly never happen unless someone changes sellstick lores
            plugin.msg(p, ChatColor.RED + "There was an error!");
            plugin.msg(p, ChatColor.RED + "Please let an admin know to check console, or, send them these messages:");

            plugin.msg(p,
                    ChatColor.RED + "Player has a sellstick that has had its 'DurabilityLine' changed in the config");
            plugin.msg(p, ChatColor.RED
                    + "For this reason, the plugin could not find the line number on which the finite/infinite lore exists");
            plugin.msg(p, ChatColor.RED + "This can be resolved by either:");
            plugin.msg(p, ChatColor.RED + "1: Giving the player a new sellstick");
            plugin.msg(p, ChatColor.RED + "(Includes anyone on the server that has this issue)");
            plugin.msg(p, ChatColor.RED + "or");
            plugin.msg(p, ChatColor.RED + "2: Changing the DurabilityLine to match the one that is on this sellstick");

            plugin.msg(p, ChatColor.RED + "For help, contact shmkane on spigot or github");
            plugin.msg(p, ChatColor.RED + "But shmkane will just tell you to do one of the above options.");

        }
        return uses;
    }

    /**
     * Now check the worth of what we're about to sell.
     *
     * @param c Inventory Holder is a chest
     * @param e Triggers on a playerinteract event
     * @return the worth
     */
    @SuppressWarnings("deprecation")
    public double calculateWorth(InventoryHolder c, PlayerInteractEvent e) {

        ItemStack[] contents = c.getInventory().getContents();

        double total = 0;
        double slotPrice = 0;
        double price = 0;

        SellingInterface si = StickConfig.instance.getSellInterface();

        if (StickConfig.instance.debug) {
            SellStick.log.warning("1-Getting prices from " + si);
            SellStick.log.warning("2-Clicked Chest(size=" + c.getInventory().getSize() + "):");
        }

        for (int i = 0; i < c.getInventory().getSize(); i++) {

            try {
                if (si == SellingInterface.PRICESYML) { // Not essW, not
                    // shopgui

                    for (String key : PriceConfig.instance.getPrices()) {

                        int data;
                        String name;

                        if (!key.contains(":")) {
                            data = 0;
                            name = key;
                        } else {
                            name = (key.split(":"))[0];
                            data = Integer.parseInt(key.split(":")[1]);
                        }

                        if ((contents[i].getType().toString().equalsIgnoreCase(name)
                                || (isNumeric(name) && contents[i].getType().getId() == Integer.parseInt(name)))
                                && contents[i].getDurability() == data) {
                            price = Double.parseDouble(PriceConfig.instance.getConfig().getString("prices." + key));

                        }

                        if (StickConfig.instance.debug) {
                            if (price > 0) {
                                SellStick.log.warning(contents[i].getType() + " x " + contents[i].getAmount());
                                SellStick.log.warning("-Price: " + price);
                            }
                        }

                    }
                } else if (si == SellingInterface.ESSWORTH) {
                    price = plugin.ess.getWorth().getPrice(plugin.ess, contents[i]).doubleValue();
                    if (StickConfig.instance.debug) {
                        if (price > 0)
                            SellStick.log.warning("-Price: " + price);
                        SellStick.log.warning(contents[i].getType() + " x " + contents[i].getAmount());
                    }

                } else if (si == SellingInterface.SHOPGUI) {

                    price = ShopGuiPlusApi.getItemStackPriceSell(e.getPlayer(), contents[i]);

                    if (price < 0) {
                        price = 0;
                    }

                    if (StickConfig.instance.debug) {
                        if (price > 0)
                            SellStick.log.warning("-Price: " + price);
                        SellStick.log.warning(contents[i].getType() + " x " + contents[i].getAmount());
                    }

                }

                if (StickConfig.instance.debug) {
                    SellStick.log.warning("--Price of (" + contents[i].getType() + "): " + price);
                }

                int amount;
                if (si != SellingInterface.SHOPGUI) {
                    amount = contents[i].getAmount();
                } else {
                    amount = 1;
                }
                slotPrice = price * amount;

                if (slotPrice > 0) {
                    ItemStack sell = contents[i];
                    c.getInventory().remove(sell);
                    e.getClickedBlock().getState().update();
                }

            } catch (Exception ex) {

                if (StickConfig.instance.debug) {
                    if (!(ex instanceof NullPointerException))
                        SellStick.log.warning(ex.toString());
                }

                if (ex instanceof PlayerDataNotLoadedException) {
                    SellStick.log.severe("Player should relog to fix this.");
                    e.getPlayer().sendMessage(ChatColor.DARK_RED + "Please re-log to use SellStick.");
                    return 0;
                }
            }
            if (StickConfig.instance.debug && slotPrice > 0) {
                SellStick.log.warning("---slotPrice=" + slotPrice);
                SellStick.log.warning("---total=" + total);
            }
            total += slotPrice;
            slotPrice = 0;
            price = 0;
        }
        if (StickConfig.instance.debug)
            System.out.println();

        return total;
    }

    /**
     * Handles the sellstick after the 'sale' has been made.
     *
     * @param lores Lores of the sellstick
     * @param uses  number of uses
     * @param p     The player who used it
     * @param total how much was sold
     * @param im    Item meta of the stick
     * @param is    Item stack object of the stick
     * @author MrGhetto
     */
    @SuppressWarnings("deprecation")
    public boolean postSale(List<String> lores, int uses, Player p, double total, ItemMeta im, ItemStack is) {

        if (!isInfinite(lores)) {

            lores.set(StickConfig.instance.durabilityLine - 1,
                    lores.get(StickConfig.instance.durabilityLine - 1).replaceAll(uses + "", (uses - 1) + ""));
            im.setLore(lores);
            is.setItemMeta(im);
        }

        /*
         * Permissions based multiplier check. If user doesn't have
         * sellstick.multiplier.x permission Multiplier defaults to 1 as seen below.
         */
        double multiplier = Double.NEGATIVE_INFINITY;

        for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
            if (perm.getPermission().startsWith("sellstick.multiplier")) {
                String stringPerm = perm.getPermission();
                String permSection = stringPerm.replaceAll("sellstick.multiplier.", "");
                if (Double.parseDouble(permSection) > multiplier) {
                    multiplier = Double.parseDouble(permSection);
                }
            }
        }

        /*
         * Multiplier set to Double.NEGATIVE_INFINITY by default to signal "unchanged"
         * Problem with defaulting to 0 is total*0 = 0, Problem with defaulting to 1 is
         * multipliers < 1.
         */
        EconomyResponse r;

        if (multiplier == Double.NEGATIVE_INFINITY) {
            r = plugin.getEcon().depositPlayer(p, total);
        } else {
            r = plugin.getEcon().depositPlayer(p, total * multiplier);
        }
        boolean success = false;
        if (r.transactionSuccess()) {
            success = true;
            if (StickConfig.instance.sellMessage.contains("\\n")) {
                String[] send = StickConfig.instance.sellMessage.split("\\\\n");
                for (String msg : send) {
                    plugin.msg(p, msg.replace("%balance%", plugin.getEcon().format(r.balance)).replace("%price%",
                            plugin.getEcon().format(r.amount)));
                }
            } else {
                plugin.msg(p, StickConfig.instance.sellMessage.replace("%balance%", plugin.getEcon().format(r.balance))
                        .replace("%price%", plugin.getEcon().format(r.amount)));
            }

            SellStick.log.info(p.getName() + " sold items via sellstick for " + r.amount + " and now has " + r.balance);
        } else {
            plugin.msg(p, String.format("An error occured: %s", r.errorMessage));
        }

        if (uses - 1 == 0) {
            p.getInventory().remove(p.getItemInHand());
            p.updateInventory();
            plugin.msg(p, StickConfig.instance.brokenStick);
        }

        return success;

    }

    /**
     * Handles the actual clicking event of the player. Deprecated since getItemHand
     * in 1.9+ should specify which hand, but will keep since it allows for
     * backwards compatibility
     * <p>
     * {@link EventPriority} Should let all other plugins handle whether or not
     * sellstick can be used.
     *
     * @param e The event
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        // When they left click with that item, and that item has the same name
        // as a sellstick
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (didClickChestWithSellStick(p, e)) {

                // Other plugin overriden.
                if (e.isCancelled()) {
                    plugin.msg(p, StickConfig.instance.territoryMessage);
                    e.setCancelled(true);
                    return;
                }

                // Didn't have permission :(
                if (!p.hasPermission("sellstick.use")) {
                    plugin.msg(p, StickConfig.instance.noPerm);
                    e.setCancelled(true);
                    return;
                }

                ItemStack is = p.getItemInHand();
                ItemMeta im = is.getItemMeta();

                List<String> lores = im.getLore();

                int uses = handleUses(p, lores);

                InventoryHolder c = (InventoryHolder) e.getClickedBlock().getState();

                double total = calculateWorth(c, e);

                if (total > 0) {
                    if (postSale(lores, uses, p, total, im, is) && StickConfig.instance.sound) {
                        p.playSound(e.getClickedBlock().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
                    }
                } else {
                    plugin.msg(p, StickConfig.instance.nothingWorth);
                }
                e.setCancelled(true);
            }
        }
    }
}
