package me.t3sl4.meteor.EventListeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.Guardians.TreasureGuardianCreator;
import me.t3sl4.meteor.Meteorites.Meteorite;
import me.t3sl4.meteor.Meteorites.MeteoriteCore;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.util.MessageUtil;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListenerClass implements Listener {
   private T3SL4Meteor plugin;
   private MeteoriteCore core;
   private Meteorite mete;

   private Random random = new Random();

   private ItemStack treasureChecker;

   private static List<Block> meteoriteBlockList = new ArrayList<>();

   public EventListenerClass(T3SL4Meteor plugin) {
      this.plugin = plugin;
      initializeTreasureChecker();
   }

   @EventHandler
   public void onMeteoriteFall(EntityChangeBlockEvent e) {
      Entity meteoriteBlockEntity = e.getEntity();
      if (meteoriteBlockEntity instanceof org.bukkit.entity.FallingBlock) {
         ConfigurationSection coreConfig;
         ConfigurationSection innerConfig;
         ConfigurationSection outerConfig;
         switch (meteoriteBlockEntity.getName().toLowerCase()) {
            case "core":
               coreConfig = this.plugin.getConfig().getConfigurationSection("core-settings");
               assert coreConfig != null;
               handleMeteoriteBlockFall(meteoriteBlockEntity, coreConfig);
               handleMeteoriteCoreFall(meteoriteBlockEntity);
               meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
               break;
            case "inner":
               innerConfig = this.plugin.getConfig().getConfigurationSection("inner-layer-settings");
               assert innerConfig != null;
               handleMeteoriteBlockFall(meteoriteBlockEntity, innerConfig);
               meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
               break;
            case "outer":
               outerConfig = this.plugin.getConfig().getConfigurationSection("outer-layer-settings");
               assert outerConfig != null;
               handleMeteoriteBlockFall(meteoriteBlockEntity, outerConfig);
               meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
               break;
         }
      }
   }

   @EventHandler
   public void onTreasureInteract(PlayerInteractEvent e) {
      if (e.getClickedBlock() != null)
         if (e.getClickedBlock().getType() == Material.CHEST) {
            Chest treasure = (Chest)e.getClickedBlock().getState();
            checkForTreasure(e, treasure.getInventory(), treasure.getLocation());
         }
   }

   private void checkForTreasure(PlayerInteractEvent e, Inventory inventory, Location location) {
      ItemStack treasureCheck = inventory.getItem(26);
      if (treasureCheck != null &&
              treasureCheck.equals(this.treasureChecker)) {
         inventory.remove(treasureCheck);
         if (this.plugin.getConfig().getBoolean("enable-treasure-guardian")) {
            spawnMeteoriteGuardian(location, e.getPlayer());
            e.setCancelled(true);
         }
      }
   }

   private void spawnMeteoriteGuardian(Location treasureLocation, Player player) {
      Location playerLocation = player.getLocation();
      Location guardianLocation = getMiddleLocation(playerLocation, treasureLocation);
      TreasureGuardianCreator.getGuardianRandomizer().getRandomGuardian().spawnGuardian(guardianLocation, player);
   }

   private Location getMiddleLocation(Location location1, Location location2) {
      Location location = new Location(location1.getWorld(), (location1.getX() + location2.getX()) / 2.0D, (location1.getY() + location2.getY()) / 2.0D, (location1.getZ() + location2.getZ()) / 2.0D);
      while (location.getBlock().getType() != Material.AIR || (new Location(location
              .getWorld(), location.getX(), location.getY() + 1.0D, location.getZ())).getBlock().getType() != Material.AIR || (new Location(location
              .getWorld(), location.getX(), location.getY() + 2.0D, location.getZ())).getBlock().getType() != Material.AIR)
         location.add(0.0D, 1.0D, 0.0D);
      return location;
   }

   private void handleMeteoriteBlockFall(Entity meteoriteBlock, ConfigurationSection blockConfig) {
      Location blockLocation = meteoriteBlock.getLocation();
      if (blockConfig.getBoolean("enable-explosion"))
         meteoriteBlock.getWorld().createExplosion(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ(), blockConfig
                 .getInt("explosion-power"), blockConfig.getBoolean("explosion-sets-fire"), blockConfig.getBoolean("explosion-breaks-blocks"));
      if (blockConfig.getBoolean("enable-lighting-strike"))
         meteoriteBlock.getWorld().strikeLightning(blockLocation);
   }

   private void handleMeteoriteCoreFall(Entity coreBlock) {
      try {
         FileConfiguration fileConfiguration = this.plugin.getConfig();
         if (fileConfiguration.contains("enable-meteorite-treasure") && fileConfiguration.getBoolean("enable-meteorite-treasure")) {
            Location tLoc = coreBlock.getLocation().add((this.random.nextInt(2) - 1), -1.0D, (this.random.nextInt(2) - 1));
            Material treasureType = Material.getMaterial(Objects.<String>requireNonNull(((String)Objects.<String>requireNonNull(fileConfiguration.getString("treasure-barrel-or-chest"))).toUpperCase()));
            if (treasureType != Material.CHEST)
               throw new ConfigException("Invalid treasure type: " + fileConfiguration.getString("treasure-barrel-or-chest") + " -> Treasure must be in a barrel or chest!");
            tLoc.getBlock().setType(treasureType);
            if(treasureType == Material.CHEST) {
               Chest chest = (Chest)tLoc.getBlock().getState();
               CraftChest BukkitChest = (CraftChest) chest;
               TileEntityChest NMSChest = BukkitChest.getTileEntity();
               NMSChest.a(MessageUtil.TREASURECHEST);
               Inventory inventory = chest.getBlockInventory();
               determineTreasureContent(inventory);
            }
            meteoriteBlockList.add(tLoc.getBlock());
         }
         if (fileConfiguration.contains("core-settings.message")) {
            String chatMessage = fileConfiguration.getString("core-settings.message");
            assert chatMessage != null;
            if (!chatMessage.equals("")) {
               chatMessage = Meteorite.setLocationPlaceholders(chatMessage, coreBlock.getLocation());
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
            }
         }
         if (fileConfiguration.contains("core-settings.commands"))
            for (String command : fileConfiguration.getStringList("core-settings.commands")) {
               if (!command.equals("")) {
                  command = Meteorite.setLocationPlaceholders(command, coreBlock.getLocation());
                  Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), command);
               }
            }
      } catch (ConfigException e) {
         ConfigException.handleConfigException(this.plugin, e);
      }
   }

   private void determineTreasureContent(Inventory inventory) {
      try {
         ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("treasure-content");
         assert config != null;
         for (String itemName : config.getKeys(false)) {
            if (config.getBoolean(itemName + ".enabled")) {
               int amount = 1;
               boolean chanceVerified = true;
               int count = 0;
               if (config.contains(itemName + ".chance")) {
                  double chance = config.getDouble(itemName + ".chance");
                  if (chance < 0.0D || chance > 100.0D)
                     throw new ConfigException("Invalid chance for item " + itemName + ": " + chance + " - Chance must be between 0-100");
                  if ((this.random.nextInt(100) + 1) > chance)
                     chanceVerified = false;
               }
               if (chanceVerified) {
                  String itemType;
                  if (config.contains(itemName + ".item-type")) {
                     itemType = config.getString(itemName + ".item-type");
                     if (itemType == null || Material.getMaterial(itemType) == null)
                        throw new ConfigException("Invalid type for item " + itemName + ": " + itemType);
                  } else {
                     throw new ConfigException("You must specify a type for item: " + itemName);
                  }
                  if (config.contains(itemName + ".amount"))
                     amount = config.getInt(itemName + ".amount");
                  ItemStack item = new ItemStack(Objects.<Material>requireNonNull(Material.getMaterial(itemType)), amount);
                  ItemMeta meta = item.getItemMeta();
                  if (config.contains(itemName + ".display-name") && config.getString(itemName + ".display-name") != null) {
                     String displayName = config.getString(itemName + ".display-name");
                     ((ItemMeta)Objects.<ItemMeta>requireNonNull(meta)).setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.<String>requireNonNull(displayName)));
                     item.setItemMeta(meta);
                  }
                  if (config.contains(itemName + ".lore")) {
                     List<String> lore = new ArrayList<>();
                     for (String loreLine : config.getStringList(itemName + ".lore"))
                        lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                     ((ItemMeta)Objects.<ItemMeta>requireNonNull(meta)).setLore(lore);
                     item.setItemMeta(meta);
                  }
                  if (config.contains(itemName + ".unbreakable") && config.getBoolean(itemName + ".unbreakable")) {
                     ((ItemMeta)Objects.<ItemMeta>requireNonNull(meta)).addEnchant(Enchantment.DURABILITY, 3, false);
                     item.setItemMeta(meta);
                  }
                  if (config.contains(itemName + ".enchants")) {
                     for (String enchantLine : ((ConfigurationSection)Objects.<ConfigurationSection>requireNonNull(config.getConfigurationSection(itemName + ".enchants"))).getKeys(false)) {
                        if (Enchantment.getByName(enchantLine.toUpperCase()) == null)
                           throw new ConfigException("Invalid enchantment name for item " + itemName + ": " + enchantLine);
                        ((ItemMeta)Objects.<ItemMeta>requireNonNull(meta)).addEnchant(Objects.<Enchantment>requireNonNull(Enchantment.getByName(enchantLine.toUpperCase())), config.getInt(itemName + ".enchants." + enchantLine), true);
                     }
                     item.setItemMeta(meta);
                  }
                  if (config.contains(itemName + ".custom-model-data") && config.getInt(itemName + ".custom-model-data") != 0) {
                     int customModelData = config.getInt(itemName + ".custom-model-data");
                     //((ItemMeta)Objects.<ItemMeta>requireNonNull(meta)).setCustomModelData(Integer.valueOf(customModelData));
                     item.setItemMeta(meta);
                  }
                  if (config.contains(itemName + ".damage") && config.getInt(itemName + ".damage") != 0) {
                     int damage = config.getInt(itemName + ".damage");
                     (Objects.requireNonNull(meta)).addEnchant(Enchantment.DAMAGE_ALL, damage, false);
                     (Objects.requireNonNull(meta)).addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                     item.setItemMeta(meta);
                  }
                  inventory.addItem(new ItemStack[] { item });
                  count++;
                  if (count >= 25)
                     break;
               }
            }
         }
         inventory.setItem(26, this.treasureChecker);
      } catch (ConfigException e) {
         ConfigException.handleConfigException(this.plugin, e);
      }
   }

   private void initializeTreasureChecker() {
      this.treasureChecker = new ItemStack(Material.DIRT, 1);
      ItemMeta treasureCheckMeta = this.treasureChecker.getItemMeta();
      assert treasureCheckMeta != null;
      treasureCheckMeta.setDisplayName("*");
      treasureCheckMeta.addEnchant(Enchantment.DURABILITY, 3, false);
      //treasureCheckMeta.setCustomModelData(Integer.valueOf(2));
      this.treasureChecker.setItemMeta(treasureCheckMeta);
   }

   public static List<Block> getMeteoriteBlockList() {
      return meteoriteBlockList;
   }

   public static void clearMeteoriteBlockList() {
      meteoriteBlockList.clear();
   }
}
