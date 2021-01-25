package me.t3sl4.meteor.Guardians;

import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.Randomizers.RandomizerClass;
import org.bukkit.configuration.Configuration;

public class TreasureGuardianCreator {
   private static RandomizerClass guardianRandomizer;

   public static RandomizerClass getGuardianRandomizer() {
      return guardianRandomizer;
   }

   public static boolean initializeGuardianRandomizer(T3SL4Meteor plugin) {
      try {
         Configuration config = plugin.getConfig();
         if (config.getBoolean("enable-treasure-guardian")) {
            guardianRandomizer = new RandomizerClass(123456789L);
            guardianRandomizer.addGuardians(plugin, config.getConfigurationSection("possible-guardians"));
         }

         return true;
      } catch (ConfigException var2) {
         ConfigException.handleConfigException(plugin, var2);
         return false;
      }
   }
}
