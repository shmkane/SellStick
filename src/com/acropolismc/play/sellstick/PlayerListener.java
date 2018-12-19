package com.acropolismc.play.sellstick;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.acropolismc.play.sellstick.Configs.PriceConfig;
import com.acropolismc.play.sellstick.Configs.StickConfig;
import com.intellectualcrafters.plot.object.Plot;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import net.milkbowl.vault.economy.EconomyResponse;
import net.redstoneore.legacyfactions.entity.FPlayerColl;

public class PlayerListener implements Listener {
	private SellStick plugin;

	// Instance of BukkitPlugin(Main)
	public PlayerListener(SellStick plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("unused")
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
		if (lores.get(StickConfig.instance.durabilityLine - 1).equalsIgnoreCase(StickConfig.instance.infiniteLore))
			return true;
		return false;
	}

	/**
	 * This method was created incase someone wants to put "%remaining uses out of
	 * 50% Where the last int is NOT the remaining uses.
	 * 
	 * There's probably a more efficient way to do this but I haven't gotten around
	 * to recoding it and it hasn't given an issue yet.
	 * 
	 * @param lores Takes a string list
	 * @return finds the uses in the lores and returns as int.
	 */
	public int getUsesFromLore(List<String> lores) {
		String found = "";
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
		// Again, the only purpose of this is to
		// Get the # of uses if theres multiple numbers
		// in the lore
		// We loop through the String(lore at index 1) and check all the indexes
		// TODO: Make this readable and more efficient.
		for (int i = 0; i < lores.get(StickConfig.instance.durabilityLine - 1).length(); i++) {
			if (Character.isDigit(lores.get(StickConfig.instance.durabilityLine - 1).charAt(i))) {
				// Increment "found" string
				// Make sure it wasnt a number from a color code

				// If it ISNT the first index
				if (i != 0) {
					// Check to see if the index before is the & sign (If its a color code)
					if (lores.get(StickConfig.instance.durabilityLine - 1).charAt(i - 1) != ChatColor.COLOR_CHAR) {
						// And if it isnt, keep track of it
						found += lores.get(StickConfig.instance.durabilityLine - 1).charAt(i);
					} else {
						// If it IS a color code, simply ignore it
						found += "-";
					}
					// But if it's index == 0
				} else {
					// There can't be a & before it, so keep track of it
					found += lores.get(StickConfig.instance.durabilityLine - 1).charAt(i);
				}
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

		int min = -2;
		try {
			min = hold.get(0);
		} catch (Exception ex) {
			System.out.println(StickConfig.instance.durabilityLine);
			System.out.println("The problem seems to be that your sellstick useline number has changed.");
			System.out.println(ex);
		}

		for (int i = 0; i < hold.size(); i++) {
			if (hold.get(i) < min) {
				min = hold.get(i);
			}
		}
		// System.out.println(min);
		return min;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		// Gets item from Config.
		Material sellItem = Material.getMaterial(StickConfig.instance.item.toUpperCase());
		// When they left click with that item, and that item has the same name
		// as a sellstick
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

			// Leaving this depricated for some backwards compatibility. Anything after
			// 1.9 should have p.getInventory().getItemInMainHand()
			if (p.getItemInHand().getType() == sellItem) {
				if (p.getItemInHand().getItemMeta().getDisplayName() != null
						&& p.getItemInHand().getItemMeta().getDisplayName().startsWith(StickConfig.instance.name)) {
					if (e.getClickedBlock().getType() == Material.CHEST
							|| e.getClickedBlock().getType() == Material.TRAPPED_CHEST) {

						// Didn't have permission :(
						if (!p.hasPermission("sellstick.use")) {
							plugin.msg(p, StickConfig.instance.noPerm);
							return;
						}

						Location location = e.getClickedBlock().getLocation();
						if (StickConfig.instance.usingPlotSquared) {
							try {
								Plot plot = Plot.getPlot(BukkitUtil.getLocation(location));

								if (!plot.getMembers().contains(p.getUniqueId())
										&& !plot.getOwners().contains(p.getUniqueId())
										&& !plot.getTrusted().contains(p.getUniqueId())) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
							} catch (Exception ex) {
								// p.sendMessage("No plot here"); This is cheating but w/e
							}
						}

						if (StickConfig.instance.usingMCoreFactions) { // If the server runs MCore Factions
							com.massivecraft.factions.entity.Faction faction = null;
							MPlayer mplayer = MPlayer.get(e.getPlayer().getUniqueId());
							faction = BoardColl.get().getFactionAt(PS.valueOf(location));

							if ((mplayer.hasFaction() && mplayer.isInOwnTerritory())
									&& !StickConfig.instance.allowOwn) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (faction.getName().contains("Wilderness")
									&& !StickConfig.instance.allowWilderness) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (faction.getName().contains("Warzone") && !StickConfig.instance.allowWarzone) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (faction.getName().contains("Safezone") && !StickConfig.instance.allowSafezone) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (mplayer.hasFaction() && !mplayer.isInOwnTerritory()) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (!mplayer.hasFaction()) {
								if (!faction.getName().contains("Wilderness") || !StickConfig.instance.allowWilderness) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
								else if (!faction.getName().contains("Warzone") || !StickConfig.instance.allowWarzone) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
								else if (!faction.getName().contains("Safezone") || !StickConfig.instance.allowSafezone) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}


								return;
							}

						}

						// SavageFactions or factionsuuid
						/**
						 * This part checks what factions the user is running and will handle sellstick
						 * accordingly
						 */
						if (StickConfig.instance.usingSavageFactions || StickConfig.instance.usingFactionsUUID) {
							Faction faction = null;
							FPlayer fplayer = FPlayers.getInstance().getByPlayer(p);
							FLocation fLoc = new FLocation(location);
							faction = Board.getInstance().getFactionAt(fLoc);

							if ((fplayer.hasFaction() && fplayer.isInOwnTerritory())
									&& !StickConfig.instance.allowOwn) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								System.out.println(1);
								e.setCancelled(true);
								return;
							} else if (faction.getTag().contains("Wilderness")
									&& !StickConfig.instance.allowWilderness) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								System.out.println(2);

								e.setCancelled(true);
								return;
							} else if (faction.getTag().contains("Warzone") && !StickConfig.instance.allowWarzone) {

								plugin.msg(p, StickConfig.instance.territoryMessage);
								System.out.println(faction.getTag().contains("Warzone"));
								System.out.println(StickConfig.instance.allowWarzone);

								e.setCancelled(true);
								return;
							} else if (faction.getTag().contains("Safezone") && !StickConfig.instance.allowSafezone) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								System.out.println(4);
								e.setCancelled(true);

								return;
							} else if (fplayer.hasFaction() && !fplayer.isInOwnTerritory()) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);

								return;
							} else if (!fplayer.hasFaction()) {
								if (!faction.getTag().contains("Wilderness") || !StickConfig.instance.allowWilderness) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
								else if (!faction.getTag().contains("Warzone") || !StickConfig.instance.allowWarzone) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
								else if (!faction.getTag().contains("Safezone") || !StickConfig.instance.allowSafezone) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}


								return;
							}

						}

						if (StickConfig.instance.usingLegacyFactions) { // Support for legacy factions
							net.redstoneore.legacyfactions.entity.Faction faction = null;
							net.redstoneore.legacyfactions.entity.FPlayer fplayer = FPlayerColl.get(p);
							net.redstoneore.legacyfactions.FLocation fLoc = new net.redstoneore.legacyfactions.FLocation(
									location);
							faction = net.redstoneore.legacyfactions.entity.Board.get().getFactionAt(fLoc);

							if ((fplayer.hasFaction() && fplayer.isInOwnTerritory())
									&& !StickConfig.instance.allowOwn) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (faction.getTag().contains("Wilderness")
									&& !StickConfig.instance.allowWilderness) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (faction.getTag().contains("Warzone") && !StickConfig.instance.allowWarzone) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (faction.getTag().contains("Safezone") && !StickConfig.instance.allowSafezone) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (fplayer.hasFaction() && !fplayer.isInOwnTerritory()) {
								plugin.msg(p, StickConfig.instance.territoryMessage);
								e.setCancelled(true);
								return;
							} else if (!fplayer.hasFaction()) {
								if (!faction.getTag().contains("Wilderness") || !StickConfig.instance.allowWilderness) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
								else if (!faction.getTag().contains("Warzone") || !StickConfig.instance.allowWarzone) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}
								else if (!faction.getTag().contains("Safezone") || !StickConfig.instance.allowSafezone) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}


								return;
							}

						}
						if (StickConfig.instance.usingSkyblock) {
							Island island = null;
							try {
								island = ASkyBlockAPI.getInstance().getIslandAt(location);

								if (!island.getMembers().contains(p.getUniqueId())) {
									plugin.msg(p, StickConfig.instance.territoryMessage);
									e.setCancelled(true);
									return;
								}

							} catch (Exception ex) {
								e.setCancelled(true);
								return;
								// System.out.println(ex.getMessage());
							}

						}

						/**
						 * This is where the selling magic happens
						 */
						ItemStack is = p.getItemInHand();
						ItemMeta im = is.getItemMeta();

						// So here we go
						List<String> lores = im.getLore();
						int uses = -1;
						if (!isInfinite(lores)) {
							uses = getUsesFromLore(lores);
						}
						if (uses == -2) {
							plugin.msg(p, ChatColor.RED + "There was an error!");
							plugin.msg(p, ChatColor.RED
									+ "Please let an admin know to check console, or, send them these messages:");

							plugin.msg(p, ChatColor.RED
									+ "Player has a sellstick that has had its 'DurabilityLine' changed in the config");
							plugin.msg(p, ChatColor.RED
									+ "For this reason, the plugin could not find the line number on which the finite/infinite lore exists");
							plugin.msg(p, ChatColor.RED + "This can be resolved by either:");
							plugin.msg(p, ChatColor.RED + "1: Giving the player a new sellstick");
							plugin.msg(p, ChatColor.RED + "(Includes anyone on the server that has this issue)");
							plugin.msg(p, ChatColor.RED + "or");
							plugin.msg(p, ChatColor.RED
									+ "2: Changing the DurabilityLine to match the one that is on this sellstick");

							plugin.msg(p, ChatColor.RED + "For help, contact shmkane on spigot or github");

							return;
						}

						InventoryHolder c = (InventoryHolder) e.getClickedBlock().getState();
						ItemStack[] contents = (ItemStack[]) c.getInventory().getContents();
						// Keep track of sold items price
						double total = 0;
						double slotPrice = 0;
						double price = 0;
						// Sell the items
						for (int i = 0; i < c.getInventory().getSize(); i++) {
							try {// Calculate the price of each item.
									// TryCatch incase something goes wrong

								// Loop through the config
								if (!StickConfig.instance.useEssentialsWorth) {
									for (String key : PriceConfig.instance.getConfig().getConfigurationSection("prices")
											.getKeys(false)) {

										// Pull put the item name and the data
										int data;
										String name;

										if (!key.contains(":")) {
											// If user didn't put a data value,
											// assume its 0
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
											price = Double.parseDouble(
													PriceConfig.instance.getConfig().getString("prices." + key));

											// Get the amount of that in the chest

										}
									}
								} else {
									// Essentials Worth
									price = plugin.ess.getWorth().getPrice(contents[i]).doubleValue();
								}

								int amount = (int) contents[i].getAmount();

								slotPrice = price * amount; // Price for slot i
								// If it was more than 0,
								if (slotPrice > 0) {
									ItemStack sell = contents[i];

									// remove from chest
									c.getInventory().remove(sell);
									e.getClickedBlock().getState().update();
								}

							} catch (NullPointerException ex) {

							}
							// Increment total, reset slot price for
							// next iteration
							total += slotPrice;
							slotPrice = 0;
							price = 0;
						}
						// Give player money if the amount they sold was > 0
						if (total > 0) {
							if (!isInfinite(lores)) { // If the item was NOT an
								// infinite sell stick
								// Lower the # of uses
								lores.set(StickConfig.instance.durabilityLine - 1,
										lores.get(StickConfig.instance.durabilityLine - 1).replaceAll(uses + "",
												(uses - 1) + ""));
								im.setLore(lores);
								is.setItemMeta(im);
							}

							EconomyResponse r = plugin.getEcon().depositPlayer(p, total);
							// Send the payment
							if (r.transactionSuccess()) {
								if (StickConfig.instance.sellMessage.contains("\\n")) {
									String[] send = StickConfig.instance.sellMessage.split("\\\\n");
									for (String msg : send) {
										plugin.msg(p, msg.replace("%balance%", plugin.getEcon().format(r.balance))
												.replace("%price%", plugin.getEcon().format(r.amount)));
									}
								} else {
									plugin.msg(p,
											StickConfig.instance.sellMessage
													.replace("%balance%", plugin.getEcon().format(r.balance))
													.replace("%price%", plugin.getEcon().format(r.amount)));
								}

								// Add this to keep a log just incase.
								System.out.println(p.getName() + " sold items via sellstick for " + r.amount
										+ " and now has " + r.balance);
							} else {
								plugin.msg(p, String.format("An error occured: %s", r.errorMessage));
							}

							// If it's out of uses, remove it from the player,
							// update inv.
							if (uses - 1 == 0) { // I guess this couldve also been if uses == 1 lol
								p.getInventory().remove(p.getItemInHand());
								p.updateInventory();
								plugin.msg(p, StickConfig.instance.brokenStick);
							}

						} else { // If the sold amount < 0
							plugin.msg(p, StickConfig.instance.nothingWorth);
						}
					}
				}
			}
		}
	}
}
