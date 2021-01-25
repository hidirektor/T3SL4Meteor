package me.t3sl4.meteor.Meteorites;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.Particles.MeteoriteParticleCreator;
import me.t3sl4.meteor.Randomizers.RandomizerClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class MeteoriteLayer {
   private int diameter;
   private String innerOrOuterMeteorLayer;
   private RandomizerClass randomizer;
   private T3SL4Meteor plugin;

   public MeteoriteLayer(int diameter, String innerOrOuterMeteorLayer, RandomizerClass randomizer, T3SL4Meteor plugin) {
      this.diameter = diameter;
      this.innerOrOuterMeteorLayer = innerOrOuterMeteorLayer;
      this.randomizer = randomizer;
      this.plugin = plugin;
   }

   public boolean spawnMeteoriteLayer(World world, Vector vector, MeteoriteCore core, ConfigurationSection layerConfig) {
      Iterator var5 = this.generateSphere(core.getLocation(), this.diameter, true).iterator();

      while(var5.hasNext()) {
         Location meteorLayerBlockLocation = (Location)var5.next();
         //BlockData meteoriteLayerBlockData = Bukkit.createBlockData(this.randomizer.getRandomMaterial());
         FallingBlock fallingMeteorLayerBlock = world.spawnFallingBlock(meteorLayerBlockLocation, this.randomizer.getRandomMaterial(), (byte) 0);
         fallingMeteorLayerBlock.setCustomName(this.innerOrOuterMeteorLayer);
         fallingMeteorLayerBlock.setVelocity(vector);
         fallingMeteorLayerBlock.setHurtEntities(layerConfig.getBoolean("can-hurt-entities"));
         fallingMeteorLayerBlock.setDropItem(layerConfig.getBoolean("drop-item-when-destroyed"));
         if (this.plugin.getConfig().contains("enable-meteorite-particles") && this.plugin.getConfig().getBoolean("enable-meteorite-particles")) {
            MeteoriteParticleCreator.spawnParticle(this.plugin, fallingMeteorLayerBlock);
         }
      }

      return true;
   }

   public List<Location> generateSphere(Location centerBlock, int radius, boolean hollow) {
      List<Location> circleBlocks = new ArrayList();
      int bx = centerBlock.getBlockX();
      int by = centerBlock.getBlockY();
      int bz = centerBlock.getBlockZ();

      for(int x = bx - radius; x <= bx + radius; ++x) {
         for(int y = by - radius; y <= by + radius; ++y) {
            for(int z = bz - radius; z <= bz + radius; ++z) {
               double distance = (double)((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y));
               if (distance < (double)(radius * radius) && (!hollow || !(distance < (double)((radius - 1) * (radius - 1))))) {
                  Location l = new Location(centerBlock.getWorld(), (double)x, (double)y, (double)z);
                  circleBlocks.add(l);
               }
            }
         }
      }

      return circleBlocks;
   }
}
