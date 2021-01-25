package me.t3sl4.meteor.Meteorites;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.t3sl4.meteor.util.BlockVector3;
import me.t3sl4.meteor.util.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.util.BlockVector2;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class RandomMeteoriteHandler {
   private static BukkitScheduler scheduler;
   private static int schedulerId;
   private static Runnable runnable;

   public static boolean randomMeteoriteHandler(final T3SL4Meteor plugin) {
      try {
         final Configuration config = plugin.getConfig();
         scheduler = plugin.getServer().getScheduler();
         long delay = plugin.getConfig().getLong("random-meteorite-interval") * 20L;
         final Random random = new Random();
         final double height = config.getDouble("random-meteorite-spawn-height");
         final double maxX = config.getDouble("random-meteorite-max-spawn-x-coord");
         final double maxZ = config.getDouble("random-meteorite-max-spawn-z-coord");
         final double minX = config.getDouble("random-meteorite-min-spawn-x-coord");
         final double minZ = config.getDouble("random-meteorite-min-spawn-z-coord");
         if (maxX <= minX) {
            throw new ConfigException("Max X coordinate for random meteorite may not be smaller than min X coordinate");
         } else if (maxZ <= minZ) {
            throw new ConfigException("Max Z coordinate for random meteorite may not be smaller than min Z coordinate");
         } else {
            final Vector randomVector = new Vector();
            String worldName = config.getString("random-meteorite-world");

            assert worldName != null;

            final World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
               throw new ConfigException("Invalid world name for random meteorites.");
            } else {
               runnable = new Runnable() {
                  Location randomLocation;
                  double randomX;
                  double randomZ;
                  double differenceX;
                  double differenceZ;
                  boolean WGisEnabled = false;
                  WorldGuardPlugin worldGuardPlugin;
                  RegionContainer regionContainer;
                  RegionManager regionManager;
                  ProtectedRegion protectedRegion;
                  BlockVector2 blockVector2;
                  int safeZoneBufferWG;
                  int tryCount = 0;
                  boolean GPisEnabled = false;
                  GriefPrevention griefPrevention;
                  int safeZoneBufferGP;
                  boolean foundSafeLocation = false;
                  boolean locationIsSafeForGP;
                  boolean locationIsSafeForWG;

                  public void run() {
                     if (config.contains("enable-griefprevention-safe-zones") && config.getBoolean("enable-griefprevention-safe-zones")) {
                        this.griefPrevention = RandomMeteoriteHandler.getGriefPrevention(plugin);
                     }

                     if (this.griefPrevention != null && this.griefPrevention.claimsEnabledForWorld(world)) {
                        this.GPisEnabled = true;
                     }

                     if (config.contains("enable-worldguard-safe-zones") && config.getBoolean("enable-worldguard-safe-zones")) {
                        this.worldGuardPlugin = RandomMeteoriteHandler.getWorldGuard(plugin);
                     }

                     if (this.worldGuardPlugin != null) {
                        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
                        this.regionContainer = wg.getRegionContainer();
                        this.regionManager = this.regionContainer.get(world);
                        if (this.regionManager != null) {
                           this.WGisEnabled = true;
                        }
                     }

                     if (this.checkAllSafeZones()) {
                        RandomMeteoriteHandler.setRandomVector(randomVector, random, plugin);
                        if (!MeteoriteCreator.getMeteoriteRandomizer().getRandomMeteorite().spawnMeteorite(this.randomLocation, randomVector, plugin)) {
                           plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Failed to shoot random meteorite.");
                        }
                     } else {
                        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Couldn't find safe random meteorite location after " + this.tryCount + " attempts.");
                        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Make sure there's enough space that isn't protected for the meteorite to spawn in! Are your buffers to high?");
                     }

                     this.tryCount = 0;
                     this.foundSafeLocation = false;
                  }

                  private boolean checkAllSafeZones() {
                     this.setRandomLocation();

                     for(; this.tryCount < 20 && !this.foundSafeLocation; this.foundSafeLocation = true) {
                        if (this.GPisEnabled) {
                           this.locationIsSafeForGP = this.checkIfInGPClaim();
                           if (!this.locationIsSafeForGP) {
                              ++this.tryCount;
                              this.checkAllSafeZones();
                              break;
                           }
                        }

                        if (this.WGisEnabled) {
                           this.locationIsSafeForWG = this.checkIfInWGSafeZone();
                           if (!this.locationIsSafeForWG) {
                              ++this.tryCount;
                              this.checkAllSafeZones();
                              break;
                           }
                        }
                     }

                     return this.tryCount < 20;
                  }

                  private boolean checkIfInGPClaim() {
                     Claim claim = this.griefPrevention.dataStore.getClaimAt(this.randomLocation, true, (Claim)null);
                     if (claim != null) {
                        return false;
                     } else {
                        this.safeZoneBufferGP = config.getInt("griefprevention-safe-zone-buffer");
                        CreateClaimResult bufferClaimResult = this.griefPrevention.dataStore.createClaim(world, (int)this.randomLocation.getX() - this.safeZoneBufferGP, (int)this.randomLocation.getX() + this.safeZoneBufferGP, 0, 256, (int)this.randomLocation.getZ() - this.safeZoneBufferGP, (int)this.randomLocation.getZ() + this.safeZoneBufferGP, (UUID)null, (Claim)null, 88888888L, (Player)null);
                        if (bufferClaimResult.succeeded) {
                           this.griefPrevention.dataStore.deleteClaim(this.griefPrevention.dataStore.getClaim(88888888L));
                           return true;
                        } else {
                           return false;
                        }
                     }
                  }

                  private boolean checkIfInWGSafeZone() {
                     this.safeZoneBufferWG = config.getInt("worldguard-safe-zone-buffer");
                     if (plugin.getConfig().contains("protect-all-worldguard-zones") && plugin.getConfig().getBoolean("protect-all-worldguard-zones")) {
                        List<ProtectedRegion> protectedRegionList = new ArrayList(this.regionManager.getRegions().values());
                        Iterator var5 = protectedRegionList.iterator();

                        ProtectedRegion safeZone;
                        do {
                           if (!var5.hasNext()) {
                              return true;
                           }

                           safeZone = (ProtectedRegion)var5.next();
                        } while(!this.checkIfRandomLocationIsInSafeZonePlusBuffer(safeZone));

                        return false;
                     } else {
                        Iterator var1 = config.getStringList("worldguard-safe-zone-names").iterator();

                        while(var1.hasNext()) {
                           String safeZoneConfigString = (String)var1.next();
                           this.protectedRegion = this.regionManager.getRegion(safeZoneConfigString);
                           if (this.protectedRegion == null) {
                              plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Caution, there is no matching world guard region for safe zone: '" + safeZoneConfigString + "'.");
                           } else if (this.checkIfRandomLocationIsInSafeZonePlusBuffer(this.protectedRegion)) {
                              return false;
                           }
                        }

                        return true;
                     }
                  }

                  private boolean checkIfRandomLocationIsInSafeZonePlusBuffer(ProtectedRegion safeZone) {
                     //ProtectedCuboidRegion fullSafeZone = new ProtectedCuboidRegion("meteoriteSafeZone123", safeZone.getMinimumPoint().add(-this.safeZoneBufferWG, 0, -this.safeZoneBufferWG), safeZone.getMaximumPoint().add(this.safeZoneBufferWG, 0, this.safeZoneBufferWG));
                     ProtectedCuboidRegion fullSafeZone = new ProtectedCuboidRegion("meteoriteSafeZone123", BlockVector3.at(-this.safeZoneBufferWG, 0, -this.safeZoneBufferWG), BlockVector3.at(this.safeZoneBufferWG, 0, this.safeZoneBufferWG));
                     this.blockVector2 = BlockVector2.at(this.randomLocation.getX(), this.randomLocation.getZ());
                     if (fullSafeZone.contains(this.blockVector2)) {
                        plugin.getServer().getConsoleSender().sendMessage("Try " + this.tryCount + ", Meteorite Location " + this.randomLocation.toString() + " was in WG safe zone: " + safeZone.toString());
                        this.regionManager.removeRegion("meteoriteSafeZone123");
                        return true;
                     } else {
                        return false;
                     }
                  }

                  private void setRandomLocation() {
                     this.differenceX = maxX - minX;
                     this.randomX = (double)random.nextInt((int)this.differenceX) + minX;
                     this.differenceZ = maxZ - minZ;
                     this.randomZ = (double)random.nextInt((int)this.differenceZ) + minZ;
                     this.randomLocation = new Location(world, this.randomX, height, this.randomZ);
                  }
               };
               if (config.getBoolean("enable-random-meteorites")) {
                  schedulerId = scheduler.scheduleSyncRepeatingTask(plugin, runnable, delay, delay);
               }

               return true;
            }
         }
      } catch (ConfigException var18) {
         ConfigException.handleConfigException(plugin, var18);
         return false;
      }
   }

   private static WorldGuardPlugin getWorldGuard(T3SL4Meteor t3SL4Meteor) {
      Plugin plugin = t3SL4Meteor.getServer().getPluginManager().getPlugin("WorldGuard");
      return !(plugin instanceof WorldGuardPlugin) ? null : (WorldGuardPlugin)plugin;
   }

   private static GriefPrevention getGriefPrevention(T3SL4Meteor t3SL4Meteor) {
      Plugin plugin = t3SL4Meteor.getServer().getPluginManager().getPlugin("GriefPrevention");
      return !(plugin instanceof GriefPrevention) ? null : (GriefPrevention)plugin;
   }

   private static void setRandomVector(Vector randomVector, Random random, T3SL4Meteor plugin) {
      double speed = 2.0D;
      if (plugin.getConfig().contains("meteorite-speed")) {
         speed = plugin.getConfig().getDouble("meteorite-speed");
      }

      randomVector.setX((double)(random.nextInt(2000) - 1000) / 1000.0D * speed);
      randomVector.setZ((double)(random.nextInt(2000) - 1000) / 1000.0D * speed);
      randomVector.setY(random.nextInt(3) - 2);
   }

   public static BukkitScheduler getScheduler() {
      return scheduler;
   }

   public static int getSchedulerId() {
      return schedulerId;
   }

   public static void shootRandomMeteorite(T3SL4Meteor plugin) {
      scheduler.runTask(plugin, runnable);
   }
}
