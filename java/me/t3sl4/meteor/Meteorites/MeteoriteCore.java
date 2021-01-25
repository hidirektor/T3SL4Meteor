package me.t3sl4.meteor.Meteorites;

import me.t3sl4.meteor.T3SL4Meteor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class MeteoriteCore {
   private Material material;
   private Location location;
   private T3SL4Meteor plugin;

   public MeteoriteCore(Material material, T3SL4Meteor plugin) {
      this.material = material;
      this.plugin = plugin;
   }

   public void setLocation(Location location) {
      this.location = location;
   }

   public Location getLocation() {
      return this.location;
   }

   public boolean spawnMeteoriteCore(World world, Vector vector, ConfigurationSection coreConfig) {
      //BlockData coreBlockData = Bukkit.createBlockData(this.material);
      FallingBlock fallingCore = world.spawnFallingBlock(this.location, this.material, (byte) 0);
      fallingCore.setCustomName("core");
      fallingCore.setVelocity(vector);
      fallingCore.setHurtEntities(coreConfig.getBoolean("can-hurt-entities"));
      fallingCore.setDropItem(coreConfig.getBoolean("drop-item-when-destroyed"));
      return true;
   }
}
