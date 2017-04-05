package com.acropolismc.play.sellstick;

import java.util.List;

import org.bukkit.ChatColor;
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

	@EventHandler
	public void onUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		// Gets item from Config.
		Material sellItem = Material.getMaterial(StickConfig.instance.item);
		// When they left click with that item, and that item has the same name
		// as a sellstick
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (p.getItemInHand().getType() == sellItem) {
				if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(StickConfig.instance.itemName)) {
					if (e.getClickedBlock().getType() == Material.CHEST
							|| e.getClickedBlock().getType() == Material.TRAPPED_CHEST) {

						// Didn't have permission :(
						if (!p.hasPermission("sellstick.use")) {
							p.sendMessage(StickConfig.instance.prefix + StickConfig.instance.noPermission);
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
									p.sendMessage(StickConfig.instance.notYourTerritory);
									e.setCancelled(true);
									return;
								}
							}
						} catch (Exception ex) {
							System.out.println(
									"[Sell Stick] Tried to setup Faction, but something failed. Are you using factions uuid?");
						}

						ItemStack is = p.getItemInHand();
						ItemMeta im = is.getItemMeta();
						List<String> lores = im.getLore();
						String[] split = lores.get(1).split(" ");

						String useslore = split[0];
						int uses = 0;
						// Ignore this if infinite uses is on
						if (!StickConfig.instance.infiniteUses) {
							uses = Integer.valueOf(useslore);
						}

						// If they have unlimited uses
						if (uses > 0 || StickConfig.instance.infiniteUses) {

							InventoryHolder c = (InventoryHolder) e.getClickedBlock().getState();
							ItemStack[] contents = (ItemStack[]) c.getInventory().getContents();
							// Keep track of sold items price
							int total = 0;
							int slotPrice = 0;
							// Sell the items
							for (int i = 0; i < c.getInventory().getSize(); i++) {
								try {// Calculate the price of each item.
										// TryCatch incase something goes wrong
									if (PriceConfig.instance.getConfig()
											.contains("prices." + contents[i].getType().toString())) {
										int price = (int) PriceConfig.instance.getConfig()
												.get("prices." + contents[i].getType().toString());
										int amount = (int) contents[i].getAmount();

										slotPrice = price * amount;
										if (slotPrice > 0)
											c.getInventory().remove(new ItemStack(
													Material.getMaterial(contents[i].getType().toString()), amount));
									}
								} catch (NullPointerException ex) {
								} // A very helpful catch!
									// Increment total, reset slot price for
									// next iteration
								total += slotPrice;
								slotPrice = 0;
							}
							// Give player money if the amount they sold was > 0
							if (total > 0) {
								EconomyResponse r = plugin.getEcon().depositPlayer(p, total);
								if (r.transactionSuccess()) {
									p.sendMessage(String.format(ChatColor.GREEN + "You were given %s and now have %s",
											plugin.getEcon().format(r.amount), plugin.getEcon().format(r.balance)));
								} else {
									p.sendMessage(String.format("An error occured: %s", r.errorMessage));
								}
								// Remove one use from the stick if not
								// infinite.
								if (!StickConfig.instance.infiniteUses) {
									uses--;
									lores.set(1, uses + " uses remaining");
									im.setLore(lores);
									is.setItemMeta(im);
									p.updateInventory();
								} else { // If it is infinite, just make sure
											// the lore is still "infinite"
									// This is just incase the server had it to
									// "finite" uses before
									// And then changed to infinite.
									lores.set(1, "Infinite Uses");
									im.setLore(lores);
									is.setItemMeta(im);
								}

							} else { // If the sold amount < 0
								p.sendMessage(ChatColor.RED + "Nothing worth selling inside");
							}
						} else if (uses <= 0) {
							p.sendMessage(ChatColor.RED + "This sellstick has run out of uses!");
						}
					}
				}
			}
		}
	}

}
