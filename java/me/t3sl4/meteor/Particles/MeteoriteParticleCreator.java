package me.t3sl4.meteor.Particles;

import java.util.ArrayList;
import java.util.List;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.Randomizers.RandomizerClass;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;

public class MeteoriteParticleCreator {
   private static RandomizerClass particleRandomizer;

   public static RandomizerClass getParticleRandomizer() {
      return particleRandomizer;
   }

   public static boolean initializeParticleRandomizer(T3SL4Meteor plugin) {
      try {
         Configuration config = plugin.getConfig();
         if (config.getBoolean("enable-meteorite-particles")) {
            particleRandomizer = new RandomizerClass(123456789L);
            particleRandomizer.addParticles(plugin, config.getConfigurationSection("possible-meteorite-particle-effects"));
         }

         return true;
      } catch (ConfigException var2) {
         ConfigException.handleConfigException(plugin, var2);
         return false;
      }
   }

   public static void spawnParticle(T3SL4Meteor plugin, final FallingBlock fallingBlock) {
      int interval = plugin.getConfig().getInt("meteorite-particle-interval");
      final List<Location> locationList = new ArrayList();
      if (interval > 0) {
         (new BukkitRunnable() {
            public void run() {
               if (fallingBlock.isDead()) {
                  this.cancel();
               }

               MeteoriteParticle particle = MeteoriteParticleCreator.particleRandomizer.getRandomParticle();
               Location fallingBlockLocation = fallingBlock.getLocation();
               double spread = particle.getSpread();
               //fallingBlock.getWorld().spawnParticle(particle.getParticleType(), fallingBlockLocation, particle.getAmount(), spread, spread, spread, particle.getSpeed(), (Object)null, particle.isForceView());
               fallingBlock.getWorld().playEffect(fallingBlockLocation, Effect.LARGE_SMOKE, spread);
               if (locationList.size() > 30 && ((Location)locationList.get(locationList.size() - 1)).equals(fallingBlockLocation)) {
                  this.cancel();
               }

               locationList.add(fallingBlockLocation);
            }
         }).runTaskTimer(plugin, 1L, (long)interval);
      }

      (new BukkitRunnable() {
         public void run() {
         }
      }).runTaskLater(plugin, 200L);
   }
}
