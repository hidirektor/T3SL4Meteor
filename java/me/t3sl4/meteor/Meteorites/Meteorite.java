package me.t3sl4.meteor.Meteorites;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.EventListeners.EventListenerClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Meteorite {
   private MeteoriteCore core;
   private MeteoriteLayer outerLayer;
   private MeteoriteLayer innerLayer;
   private ConfigurationSection config;
   BukkitScheduler scheduler = Bukkit.getScheduler();
   private int schedulerId;
   List<Block> blockList = new ArrayList();

   public Meteorite(MeteoriteCore core, MeteoriteLayer outerLayer, ConfigurationSection config) {
      this.core = core;
      this.outerLayer = outerLayer;
      this.innerLayer = null;
      this.config = config;
   }

   public Meteorite(MeteoriteCore core, MeteoriteLayer outerLayer, MeteoriteLayer innerLayer, ConfigurationSection config) {
      this.core = core;
      this.outerLayer = outerLayer;
      this.innerLayer = innerLayer;
      this.config = config;
   }

   public boolean spawnMeteorite(Location location, Vector vector, T3SL4Meteor plugin) {
      ConfigurationSection coreConfig = plugin.getConfig().getConfigurationSection("core-settings");
      ConfigurationSection innerConfig = plugin.getConfig().getConfigurationSection("inner-layer-settings");
      ConfigurationSection outerConfig = plugin.getConfig().getConfigurationSection("outer-layer-settings");
      this.core.setLocation(location);
      if (this.config.contains("chat-message")) {
         String chatMessage = this.config.getString("chat-message");

         assert chatMessage != null;

         if (!chatMessage.equals("")) {
            chatMessage = setLocationPlaceholders(chatMessage, this.core.getLocation());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
         }
      }

      if (this.config.contains("meteorite-spawn-commands")) {
         Iterator var9 = this.config.getStringList("meteorite-spawn-commands").iterator();

         while(var9.hasNext()) {
            String command = (String)var9.next();
            if (!command.equals("")) {
               command = setLocationPlaceholders(command, this.core.getLocation());
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
         }
      }

      World world = location.getWorld();
      if (this.config.contains("clean-up-meteorite-blocks-interval") && this.config.getInt("clean-up-meteorite-blocks-interval") > 0) {
         this.schedulerId = this.scheduler.scheduleSyncDelayedTask(plugin, () -> {
            this.blockList.addAll(new ArrayList(EventListenerClass.getMeteoriteBlockList()));
            EventListenerClass.clearMeteoriteBlockList();
         }, 380L);
         this.schedulerId = this.scheduler.scheduleSyncDelayedTask(plugin, this::cleanUpMeteoriteBlocks, (long)(this.config.getInt("clean-up-meteorite-blocks-interval") * 20));
      } else {
         this.schedulerId = this.scheduler.scheduleSyncDelayedTask(plugin, EventListenerClass::clearMeteoriteBlockList, 380L);
      }

      if (!this.core.spawnMeteoriteCore(world, vector, coreConfig)) {
         return false;
      } else {
         return this.innerLayer != null && !this.innerLayer.spawnMeteoriteLayer(world, vector, this.core, innerConfig) ? false : this.outerLayer.spawnMeteoriteLayer(world, vector, this.core, outerConfig);
      }
   }

   public static String setLocationPlaceholders(String string, Location location) {
      string = string.replaceAll("%locationX%", String.valueOf(Math.round(location.getX())));
      string = string.replaceAll("%locationZ%", String.valueOf(Math.round(location.getZ())));
      string = string.replaceAll("%locationY%", String.valueOf(Math.round(location.getY())));
      return string;
   }

   public void cleanUpMeteoriteBlocks() {
      Iterator var1 = this.blockList.iterator();

      while(var1.hasNext()) {
         Block meteoriteBlock = (Block)var1.next();
         meteoriteBlock.setType(Material.AIR);
      }

      this.blockList.clear();
   }
}
