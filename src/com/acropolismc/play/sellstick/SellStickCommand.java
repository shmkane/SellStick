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

	public SellStickCommand(SellStick plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			plugin.msg(sender, ChatColor.GRAY + "" + ChatColor.ITALIC + "Sell Stick by shmkane");
			if (sender.hasPermission("sellstick.give")) {
				plugin.msg(sender, ChatColor.GREEN + "/SellStick give <player> <amount> (<uses>/infinite)");
			}
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
				return true;
			} else {
				plugin.msg(sender, ChatColor.GRAY + "" + ChatColor.ITALIC + "Sell Stick by shmkane");
				return true;
			}
		} else if (args.length == 4) {
			if (args[0].equalsIgnoreCase("give")) {
				if (sender.hasPermission("sellstick.give")) {
					Player target = plugin.getServer().getPlayer(args[1]);
					if (target != null && target.isOnline()) {
						for (int i = 0; i < Integer.parseInt(args[2]); i++) {
							/**
							 * This assigns a random string to the item meta so that the item cannot be
							 * stacked
							 */
							RandomString random = new RandomString(5);
							String UUID = random.nextString();

							ItemStack is = new ItemStack(Material.getMaterial(StickConfig.instance.item));
							ItemMeta im = is.getItemMeta();

							List<String> lores = new ArrayList<String>();

							im.setDisplayName(StickConfig.instance.name + UUID);

							// Load values from config onto the stick lores array
							for (int z = 0; z < StickConfig.instance.lore.size(); z++) {
								lores.add(StickConfig.instance.lore.get(z).replace("&", "§"));
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
								System.out.println(ex);
								return false;
							}

							if (args[3].equalsIgnoreCase("infinite") || args[3].equalsIgnoreCase("i")) {
								lores.set(StickConfig.instance.durabilityLine - 1,
										lores.get(StickConfig.instance.durabilityLine - 1).replace("%usesLore%",
												StickConfig.instance.infiniteLore));
							} else {
								int uses = Integer.parseInt(args[3]);
								// otherwise replace it with the remaining uses
								lores.set(StickConfig.instance.durabilityLine - 1,
										lores.get(StickConfig.instance.durabilityLine - 1).replace("%usesLore%",
												StickConfig.instance.finiteLore.replace("%remaining%", uses + "")));
							}
							// assign the meta to the stick

							im.setLore(lores);
							im.addItemFlags(ItemFlag.HIDE_ENCHANTS);

							is.setItemMeta(im);

							// Give the item
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
	 * 
	 * @param itemStack Accepts an itemstack
	 * @return returns an enchanted item with durability 1(unbreaking)
	 */
	public ItemStack glow(ItemStack itemStack) {
		itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		return itemStack;
	}
}