package com.acropolismc.play.sellstick;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.acropolismc.play.sellstick.Configs.StickConfig;

public class SellStickCommand implements CommandExecutor {
	private SellStick plugin;

	public SellStickCommand(SellStick plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(
					StickConfig.instance.prefix + ChatColor.GRAY + "" + ChatColor.ITALIC + "Sell Stick by shmkane");
			if (sender.hasPermission("sellstick.give")) {
				sender.sendMessage(StickConfig.instance.prefix + ChatColor.GREEN + "/SellStick give <player> <amount>");
			}
			return true;
		} else if (args.length == 1) {
			if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("sellstick.reload")){
				plugin.getServer().getPluginManager().disablePlugin(plugin);
				sender.sendMessage(ChatColor.RED + "Disabled Plugin");
				plugin.getServer().getPluginManager().enablePlugin(plugin);
				sender.sendMessage(ChatColor.GREEN + "Enabled Plugin");
				return true;
			}
			sender.sendMessage(
					StickConfig.instance.prefix + ChatColor.GRAY + "" + ChatColor.ITALIC + "Sell Stick by shmkane");
			return false;
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("give")) {
				if (sender.hasPermission("sellstick.give")) {

					Player target = plugin.getServer().getPlayer(args[1]);

					if (target != null && target.isOnline()) {
						for(int i = 0; i < Integer.parseInt(args[2]); i ++){
							RandomString random = new RandomString(10);
							String UUID = random.nextString();
							ItemStack is = new ItemStack(Material.getMaterial(StickConfig.instance.item));
							ItemMeta im = is.getItemMeta();
							List<String> lores = new ArrayList<String>();
							im.setDisplayName(StickConfig.instance.itemName);
							lores.add(StickConfig.instance.itemLore);
							if(StickConfig.instance.infiniteUses){
								lores.add("Infinite Uses");
							}else{
								lores.add(StickConfig.instance.uses + " uses remaining");
							}
							lores.add(ChatColor.MAGIC + UUID);
							im.setLore(lores);
							is.setItemMeta(im);

							target.getInventory().addItem(is);
						}
						target.sendMessage(
								StickConfig.instance.prefix + "You've been given " + Integer.parseInt(args[2])
								+ " sell " + ((Integer.parseInt(args[2]) > 1) ? "sticks!" : "stick!"));
						sender.sendMessage(StickConfig.instance.prefix + "Given " + target.getName() + " "
								+ Integer.parseInt(args[2]) + " sell "
								+ ((Integer.parseInt(args[2]) > 1) ? "sticks!" : "stick!"));

						return true;

					} else {
						sender.sendMessage(ChatColor.RED + "Player not found");
					}
				} else {
					sender.sendMessage(StickConfig.instance.prefix + StickConfig.instance.noPermission);
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid command. Type /Sellstick for help");
		}
		return false;
	}
}