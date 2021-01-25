package me.t3sl4.meteor.ExceptionHandling;

import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.util.MessageUtil;
import org.bukkit.ChatColor;

public class ConfigException extends Exception {
   public ConfigException(String message) {
      super(message);
   }

   public static void handleConfigException(T3SL4Meteor plugin, ConfigException e) {
      plugin.getServer().getConsoleSender().sendMessage(MessageUtil.PREFIX + e.getMessage());
   }
}
