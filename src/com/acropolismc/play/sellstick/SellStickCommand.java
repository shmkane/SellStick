package com.acropolismc.play.sellstick;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.acropolismc.play.sellstick.Configs.StickConfig;

public class SellStickCommand implements CommandExecutor {

	private SellStick plugin;
	// Saving which line the uses is on here
	public static int index = 0;

	// Pass instance of main class to 'plugin'
	public SellStickCommand(SellStick plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Args length is 0
		if (args.length == 0) {
			// For players, show them nothing.
			plugin.msg(sender,
					StickConfig.instance.prefix + ChatColor.GRAY + "" + ChatColor.ITALIC + "Sell Stick by shmkane");
			// If the sender has perms to give a stick, show the help message
			if (sender.hasPermission("sellstick.give")) {
				plugin.msg(sender, StickConfig.instance.prefix + ChatColor.GREEN
						+ "/SellStick give <player> <amount> (<uses>/infinite)");
			}
			return true;

			// Args length is 1
		} else if (args.length == 1) {
			// If the sender has permission to reload
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
				return true;
			} else {
				plugin.msg(sender, 
						StickConfig.instance.prefix + ChatColor.GRAY + "" + ChatColor.ITALIC + "Sell Stick by shmkane");
				return true;
			}
			// Args length is 3
		} else if (args.length == 4) {
			// If they're trying to give a stick...
			if (args[0].equalsIgnoreCase("give")) {
				// And has permission...
				if (sender.hasPermission("sellstick.give")) {
					// Get the intended target
					Player target = plugin.getServer().getPlayer(args[1]);
					// and make sure the target is online/exists
					if (target != null && target.isOnline()) {
						// Give them X number of sticks.
						for (int i = 0; i < Integer.parseInt(args[2]); i++) {
							/**
							 * This assigns a random string to the item meta so that the item cannot be
							 * stacked
							 */
							RandomString random = new RandomString(10);
							String UUID = random.nextString();

							ItemStack is = new ItemStack(Material.getMaterial(StickConfig.instance.item));
							ItemMeta im = is.getItemMeta();

							List<String> lores = new ArrayList<String>();

							im.setDisplayName(StickConfig.instance.name);

							// Load values from config onto the stick lores array
							for (int z = 0; z < StickConfig.instance.lore.size(); z++) {
								lores.add(StickConfig.instance.lore.get(z).replace("&", "§"));
							}

							lores.add(1, "%usesLore%");

							if (args[3].equalsIgnoreCase("infinite") || args[3].equalsIgnoreCase("i")) {
								lores.set(1, lores.get(1).replace("%usesLore%", StickConfig.instance.infiniteLore));
							} else {
								int uses = Integer.parseInt(args[3]);
								// otherwise replace it with the remaining uses
								lores.set(1, lores.get(1).replace("%usesLore%",
										StickConfig.instance.usesLore.replace("%remaining%", uses + "")));
							}
							// assign the meta to the stick
							lores.add(UUID);
							im.setLore(lores);
							im.addItemFlags(ItemFlag.HIDE_ENCHANTS);

							is.setItemMeta(im);

							// Give the item
							if (StickConfig.instance.glow) {
								is = glow(is);
							}

							target.getInventory().addItem(is);
						}
						// Send target and sender a message.
						plugin.msg(target, StickConfig.instance.receiveMessage.replace("%amount%",
								Integer.parseInt(args[2]) + ""));

						plugin.msg(sender, StickConfig.instance.giveMessage.replace("%player%", target.getName())
								.replace("%amount%", Integer.parseInt(args[2]) + ""));

						return true;

						// If the target wasn't found
					} else {
						plugin.msg(sender, ChatColor.RED + "Player not found");
					}
					// If the sender didn't have permission
				} else {
					plugin.msg(sender, StickConfig.instance.prefix + StickConfig.instance.noPerm);
				}
			}
			// Invalid number of args.
		} else {
			plugin.msg(sender, "" + ChatColor.RED + "Invalid command. Type /Sellstick for help");
		}
		return false;
	}



	/**
	 * 
	 * @param itemStack Accepts an itemstack
	 * @return returns an enchanted item with durability 1(unbreaking)
	 */
	public ItemStack glow(ItemStack itemStack) {
		itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		return itemStack;
	}
}