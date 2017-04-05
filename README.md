[ATTACH]223965[/ATTACH] 


Example Gif(click)
Sell stick allows players to quickly sell items inside a chest with just one click! You can customize the item used and the amount of uses the item gives, as well as the name of the item and the lore! All of these options are located inside the config.yml!

To set the price for the items sold, simply edit the prices.yml

(Tested on paperspigot1.8.8)


    Download SellStick
    Download Vault!
    Place both in your (/Plugins) folder
    Restart the server
    Edit the config to your liking
    Edit the prices to your liking
    Do /sellstick reload to save changes
    Enjoy!


This plugin is a must have for factions servers! Players will have many methods of getting money, and surely iron golem, cow, and creeper are a few of them. With sell stick, they can quickly sell all the loot!

These can also be used as voting or donating incentives!



sellstick.give - Allows the player with this permission to give another player sell sticks!
sellstick.use - Allows the player with this permission to use a sell stick!



Use this for the item and for the prices
Here's the config.yml and prices.yml we use on Acropolis

[CODE]

#Sell Stick by shmkane
#-----------------------------------------
#For any Materials, use https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html as a reference
#-----------------------------------------
#YES IT MATTERS HOW YOU TYPE IT IN
#PLEASE GO TO THAT LINK
#What's your server name?
servername: AcropolisMC
#What prefix should be sent with each message?
prefix: '&8[&a&lAcropolis&8] &a'
#What to tell players when they don't have permission
noPermission: '&cSorry, you don''t have permission for this!'
#SellStick Options
sellstick:
#What should the item be?
#USE THE LINK ABOVE TO CHANGE
  item: STICK
  #What should be the display name?
  displayname: '&cSellStick'
  #Do you want to give users a small tutorial?
  lore: '&cLeft click on a chest to sell items inside!'
  #Should the sell stick be infinite uses?
  infinite: false
  #How many times can they use this stick? HAVE infinite set to false to use this
  uses: 10
  #Only in own/wilderness territory?
  onlyOwn: true
  #What message to send them if they try to sell something outside their territory
  notYourTerritory: '&cYou can''t use sell stick outside your territory!'

[/CODE]

PRICES:

[CODE]
prices:
IRON_INGOT: 10
SULPHUR: 6
RED_ROSE: 1
LEATHER: 2
COOKED_BEEF: 5
SULPHUR: 6
BONE: 1
ARROW: 1
ROTTEN_FLESH: 1
BLAZE_ROD: 8
ENDER_PEARL: 18
SPIDER_EYE: 3
STRING: 1
SLIME_BALL: 5
FEATHER: 2
EGG: 2
STICK: 1
INK_SACK: 5
GOLD_NUGGET: 2
TNT: 25
[/CODE]


Bugs

If you find a bug in the plugin, rather than leaving a bad rating, PM me and I will fix it :)
