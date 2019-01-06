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
import org.bukkit.permissions.PermissionAttachmentInfo;

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

	public PlayerListener(SellStick plugin) {
		this.plugin = plugin;
	}

	public static boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
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

						/**
						 * This part checks what factions the user is running and will handle sellstick
						 * accordingly
						 */
						Faction faction = null;
						FPlayer fplayer = FPlayers.getInstance().getByPlayer(p);
						FLocation fLoc = new FLocation(location);
						faction = Board.getInstance().getFactionAt(fLoc);

						// Own
						if ((fplayer.hasFaction() && fplayer.isInOwnTerritory() && !StickConfig.instance.allowOwn)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}

						// Wilderness
						if ((faction.getTag().contains("Wilderness") && !StickConfig.instance.allowWilderness)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}
						// Warzone
						if ((faction.getTag().contains("Warzone") && !StickConfig.instance.allowWarzone)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}
						// Safezone
						if ((faction.getTag().contains("Safezone") && !StickConfig.instance.allowSafezone)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}

						// Enemy
						if ((fplayer.hasFaction() && fplayer.isInEnemyTerritory()
								&& !StickConfig.instance.allowEnemy)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}

						// Neutral
						if ((fplayer.hasFaction() && fplayer.isInNeutralTerritory()
								&& !StickConfig.instance.allowNeutral)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}

						// Ally
						if ((fplayer.hasFaction() && fplayer.isInAllyTerritory() && !StickConfig.instance.allowAlly)) {
							plugin.msg(p, StickConfig.instance.territoryMessage.replace("%claims%",
									fplayer.getRelationTo(faction).getColor() + faction.getTag()));
							e.setCancelled(true);
							return;
						}

						ItemStack is = p.getItemInHand();
						ItemMeta im = is.getItemMeta();

						// So here we go
						List<String> lores = im.getLore();
						int uses = -1;
						if (!isInfinite(lores)) {
							uses = getUsesFromLore(lores);
						}
						if (uses == -2) {
							// This should honestly never happen unless someone changes sellstick lores
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
						double total = 0;
						double slotPrice = 0;
						double price = 0;
						double multiplier = Double.NEGATIVE_INFINITY;

						for (int i = 0; i < c.getInventory().getSize(); i++) {
							try {
								if (!StickConfig.instance.useEssentialsWorth || plugin.ess == null
										|| !plugin.ess.isEnabled()) {
									for (String key : PriceConfig.instance.getConfig().getConfigurationSection("prices")
											.getKeys(false)) {

										int data;
										String name;

										if (!key.contains(":")) {
											data = 0;
											name = key;
										} else {
											name = (key.split(":"))[0];
											data = Integer.parseInt(key.split(":")[1]);
										}

										if ((contents[i].getType().toString().equalsIgnoreCase(name) || (isNumeric(name)
												&& contents[i].getType().getId() == Integer.parseInt(name)))
												&& contents[i].getDurability() == data) {
											price = Double.parseDouble(
													PriceConfig.instance.getConfig().getString("prices." + key));

										}
									}
								} else {
									// Essentials Worth
									price = plugin.ess.getWorth().getPrice(contents[i]).doubleValue();
								}

								int amount = (int) contents[i].getAmount();

								slotPrice = price * amount;
								if (slotPrice > 0) {
									ItemStack sell = contents[i];
									c.getInventory().remove(sell);
									e.getClickedBlock().getState().update();
								}

							} catch (NullPointerException ex) {
							}

							total += slotPrice;
							slotPrice = 0;
							price = 0;
						}

						if (total > 0) {
							if (!isInfinite(lores)) {

								lores.set(StickConfig.instance.durabilityLine - 1,
										lores.get(StickConfig.instance.durabilityLine - 1).replaceAll(uses + "",
												(uses - 1) + ""));
								im.setLore(lores);
								is.setItemMeta(im);
							}

							/*
							 * Permissions based multiplier check. If user doesn't have
							 * sellstick.multiplier.x permission Multiplier defaults to 1 as seen below.
							 */
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

								System.out.println(p.getName() + " sold items via sellstick for " + r.amount
										+ " and now has " + r.balance);
							} else {
								plugin.msg(p, String.format("An error occured: %s", r.errorMessage));
							}

							if (uses - 1 == 0) {
								p.getInventory().remove(p.getItemInHand());
								p.updateInventory();
								plugin.msg(p, StickConfig.instance.brokenStick);
							}

						} else {
							plugin.msg(p, StickConfig.instance.nothingWorth);
						}
						e.setCancelled(true);
					}
				}
			}
		}
	}
}
