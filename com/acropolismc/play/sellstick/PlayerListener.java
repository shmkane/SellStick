package com.acropolismc.play.sellstick;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.acropolismc.play.sellstick.Configs.PriceConfig;
import com.acropolismc.play.sellstick.Configs.StickConfig;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import net.milkbowl.vault.economy.EconomyResponse;

public class PlayerListener implements Listener {
	private SellStick plugin;

	// Instance of BukkitPlugin(Main)
	public PlayerListener(SellStick plugin) {
		this.plugin = plugin;
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public boolean isInfinite(List<String> lores) {
		// If the 2nd line is the same as the config, its infinite
		if (lores.get(1).equalsIgnoreCase(StickConfig.instance.infiniteLore))
			return true;
		return false;
	}

	public int getUsesFromLore(List<String> lores) {
		String found = "";
		// We loop through the String(lore at index 1) and check all the indexes
		for (int i = 0; i < lores.get(1).length(); i++) {
			// If we find a digit, we
			if (Character.isDigit(lores.get(1).charAt(i))) {
				// Increment "found" string
				found += lores.get(1).charAt(i);
			} else {
				// Otherwise we insert a "-"
				found += "-";
			}
		}
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
		int min = hold.get(0);
		for (int i = 0; i < hold.size(); i++) {
			if (hold.get(i) < min) {
				min = hold.get(i);
			}
		}

		return min;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		// Gets item from Config.
		Material sellItem = Material.getMaterial(StickConfig.instance.item.toUpperCase());
		// When they left click with that item, and that item has the same name
		// as a sellstick
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (p.getItemInHand().getType() == sellItem) {
				if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(StickConfig.instance.name)) {
					if (e.getClickedBlock().getType() == Material.CHEST
							|| e.getClickedBlock().getType() == Material.TRAPPED_CHEST) {

						// Didn't have permission :(
						if (!p.hasPermission("sellstick.use")) {
							p.sendMessage(StickConfig.instance.prefix + StickConfig.instance.noPerm);
							return;
						}
						try { // Instead of adding a dependency on Factions,
							// putting a try catch to 'silently' hide
							// errors.
							Faction faction = null;
							FPlayer fplayer = FPlayers.getInstance().getByPlayer(p);
							Location location = e.getClickedBlock().getLocation();
							FLocation fLoc = new FLocation(location);
							faction = Board.getInstance().getFactionAt(fLoc);

							// Check to see if the location the block is at is
							// their/wilderness territory
							if (StickConfig.instance.onlyOwn) {
								// Not in own and not in wilderness
								if (!fplayer.isInOwnTerritory() && !faction.getTag().contains("Wilderness")) {
									p.sendMessage(StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
							}
						} catch (Exception ex) {
							//Spam the message a bit :P
							for(int i = 0; i < 5; i ++)
								System.out.println("[Sell Stick] Tried to setup Faction, but something failed. Are you sure you're using factions uuid?");
						}

						ItemStack is = p.getItemInHand();
						ItemMeta im = is.getItemMeta();
						// Get all the lores. lore[1] contains the uses/infinite
						// info.
						// We need to get the uses.
						// The reason for all of this is incase someone puts
						// %remaining% uses out of 50, in the config. We need to
						// be able to
						// get the first number(or the lower number in most
						// cases)
						// because an annoying person will put
						// out of 50 you have %remaining% uses left.

						// So here we go
						List<String> lores = im.getLore();
						int uses = -1;
						if (!isInfinite(lores)) {
							uses = getUsesFromLore(lores);
						}

						InventoryHolder c = (InventoryHolder) e.getClickedBlock().getState();
						ItemStack[] contents = (ItemStack[]) c.getInventory().getContents();
						// Keep track of sold items price
						double total = 0;
						double slotPrice = 0;
						// Sell the items
						for (int i = 0; i < c.getInventory().getSize(); i++) {
							try {// Calculate the price of each item.
								// TryCatch incase something goes wrong

								// Loop through the config
								for (String key : PriceConfig.instance.getConfig().getConfigurationSection("prices")
										.getKeys(false)) {

									// Pull put the item name and the data
									int data;
									String name;

									if (!key.contains(":")) {
										// If user didnt put a data value,
										// assumt its 0
										data = 0;
										name = key;
									} else {
										// Split by the colon.
										name = (key.split(":"))[0];
										data = Integer.parseInt(key.split(":")[1]);
									}

									// If the item matches(whether its numeric
									// or string)
									// in the config, and the data value
									// matches,
									if ((contents[i].getType().toString().equalsIgnoreCase(name) || (isNumeric(name)
											&& contents[i].getType().getId() == Integer.parseInt(name)))
											&& contents[i].getDurability() == data) {

										// Get the price listed for that item
										double price = Double.parseDouble(
												PriceConfig.instance.getConfig().getString("prices." + key));

										// Get the amount of that in the chest
										int amount = (int) contents[i].getAmount();

										// Get the price for that one slot in
										// the chest
										slotPrice = price * amount;
										// If it was more than 0,
										if (slotPrice > 0) {
											ItemStack sell;
											// Sell it
											if (isNumeric(name)) {
												sell = new ItemStack(Integer.parseInt(name), amount, (short) data);
											} else {
												sell = new ItemStack(Material.getMaterial(name.toUpperCase()), amount,
														(short) data);
											}
											// Then remove the item from the
											// chest
											if (c.getInventory().contains(sell)) {
												c.getInventory().remove(sell);
											}
										}
									}
								}
							} catch (NullPointerException ex) {
							}
							// Increment total, reset slot price for
							// next iteration
							total += slotPrice;
							slotPrice = 0;
						}
						// Give player money if the amount they sold was > 0
						if (total > 0) {
							if (!isInfinite(lores)) { // If the item was NOT an
								// infinite sell stick
								// Lower the # of uses
								lores.set(1, lores.get(1).replaceAll(uses + "", (uses - 1) + ""));
								im.setLore(lores);
								is.setItemMeta(im);
							}

							EconomyResponse r = plugin.getEcon().depositPlayer(p, total);
							// Send the payment
							if (r.transactionSuccess()) {
								p.sendMessage(StickConfig.instance.sellMessage
										.replace("%balance%", plugin.getEcon().format(r.balance))
										.replace("%price%", plugin.getEcon().format(r.amount)));
							} else {
								p.sendMessage(String.format("An error occured: %s", r.errorMessage));
							}

							// If it's out of uses, remove it from the player,
							// update inv.
							if (uses - 1 == 0) {
								p.getInventory().remove(p.getItemInHand());
								p.updateInventory();
							}

						} else { // If the sold amount < 0
							p.sendMessage(StickConfig.instance.nothingWorth);
						}
					}
				}
			}
		}
	}
}
