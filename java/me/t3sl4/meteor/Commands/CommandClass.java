package me.t3sl4.meteor.Commands;

import java.util.Iterator;
import java.util.Objects;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.Meteorites.MeteoriteCreator;
import me.t3sl4.meteor.Meteorites.RandomMeteoriteHandler;
import me.t3sl4.meteor.util.MessageUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CommandClass implements CommandExecutor {
   private T3SL4Meteor plugin;
   private double meteoriteSpeed = 2.0D;

   public CommandClass(T3SL4Meteor plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("tmeteor")) {
         TextComponent msg = new TextComponent("§e§lAuthor §7|| §e§lYapımcı");
         msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§7Eklenti Yapımcısı:\n   §8§l» §eSYN_T3SL4 \n   §8§l» §7Discord: §eHalil#4439")).create()));
         Player player = null;
         if (sender instanceof Player) {
            player = ((Player)sender).getPlayer();
         }

         String var6;
         byte var7;
         switch(args.length) {
         case 0:
            if (!sender.hasPermission("t3sl4meteor.default") && !sender.hasPermission("t3sl4meteor.admin")) {
               sender.sendMessage(MessageUtil.NOPERM);
            } else {
               for (String s : MessageUtil.INFO) {
                  sender.sendMessage(String.valueOf(s));
               }
            }
            break;
         case 1:
            var6 = args[0].toLowerCase();
            var7 = -1;
            switch(var6.hashCode()) {
            case -934641255:
               if (var6.equals("reload")) {
                  var7 = 2;
               }
               break;
            case 3198785:
               if (var6.equals("help")) {
                  var7 = 0;
               }
               break;
            case 3540994:
               if (var6.equals("stop")) {
                  var7 = 6;
               }
               break;
            case 109413407:
               if (var6.equals("shoot")) {
                  var7 = 3;
               }
               break;
            case 109757538:
               if (var6.equals("start")) {
                  var7 = 5;
               }
               break;
            case 893818274:
               if (var6.equals("shootrandom")) {
                  var7 = 4;
               }
               break;
            case 1671380268:
               if (var6.equalsIgnoreCase("discord")) {
                  var7 = 1;
               }
            }

            switch(var7) {
            case 0:
               if (!sender.hasPermission("t3sl4meteor.default") && !sender.hasPermission("t3sl4meteor.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
               } else {
                  for (String s : MessageUtil.INFO) {
                     sender.sendMessage(String.valueOf(s));
                  }
               }

               return true;
            case 1:
               if (!sender.hasPermission("t3sl4meteor.default") && !sender.hasPermission("t3sl4meteor.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
               } else {
                  if(sender instanceof Player) {
                     Player hover = (Player) sender;
                     hover.spigot().sendMessage(msg);
                  } else {
                     sender.sendMessage(MessageUtil.colorize("&e&lAuthor &7|| &e&lYapimci"));
                     sender.sendMessage(MessageUtil.colorize("&7Eklenti Yapimcisi:\n   &8&l» &eSYN_T3SL4"));
                     sender.sendMessage(MessageUtil.colorize("&8&l» &7Discord: &eHalil#4439"));
                  }
               }
               return true;
            case 2:
               if (!sender.hasPermission("t3sl4meteor.reload") && !sender.hasPermission("t3sl4meteor.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
               } else {
                  try {
                     this.plugin.reloadConfig();
                     if (RandomMeteoriteHandler.getScheduler() != null) {
                        RandomMeteoriteHandler.getScheduler().cancelTask(RandomMeteoriteHandler.getSchedulerId());
                     }

                     if (this.plugin.initializePluginRandomizers() && RandomMeteoriteHandler.randomMeteoriteHandler(this.plugin)) {
                        sender.sendMessage(MessageUtil.RELOAD);
                        return true;
                     }

                     throw new ConfigException(MessageUtil.CHECK);
                  } catch (ConfigException var9) {
                     sender.sendMessage(MessageUtil.PREFIX + var9.getMessage());
                  }
               }

               return true;
            case 3:
               if (!sender.hasPermission("meteoritespro.shoot") && !sender.hasPermission("meteoritespro.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
                  return true;
               } else {
                  if (player != null) {
                     sender.sendMessage(MessageUtil.VERIFYMETEOR);
                     sender.sendMessage((MessageUtil.AVAILABLEMETEORS).replaceAll("%meteors%", this.getAvailableMeteoriteNames()));
                  } else {
                     sender.sendMessage(MessageUtil.MUSTINGAME);
                  }

                  return true;
               }
            case 4:
               if (sender.hasPermission("t3sl4meteor.shootrandom") || sender.hasPermission("t3sl4meteor.admin")) {
                  RandomMeteoriteHandler.shootRandomMeteorite(this.plugin);
                  sender.sendMessage(MessageUtil.SHOOTING);
               }

               return true;
            case 5:
               if (!sender.hasPermission("t3sl4meteor.start") && !sender.hasPermission("t3sl4meteor.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
                  return true;
               } else if (this.plugin.getConfig().getBoolean("enable-random-meteorites")) {
                  if (!RandomMeteoriteHandler.getScheduler().isCurrentlyRunning(RandomMeteoriteHandler.getSchedulerId()) && !RandomMeteoriteHandler.getScheduler().isQueued(RandomMeteoriteHandler.getSchedulerId())) {
                     RandomMeteoriteHandler.randomMeteoriteHandler(this.plugin);
                     sender.sendMessage(MessageUtil.RANDOMMETEORS);
                  } else {
                     sender.sendMessage(MessageUtil.ALREADYRANDOM);
                  }

                  return true;
               } else {
                  sender.sendMessage(MessageUtil.DISABLEDRANDOM);
                  return true;
               }
            case 6:
               if (!sender.hasPermission("t3sl4meteor.stop") && !sender.hasPermission("t3sl4meteor.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
                  return true;
               } else {
                  if (!RandomMeteoriteHandler.getScheduler().isCurrentlyRunning(RandomMeteoriteHandler.getSchedulerId()) && !RandomMeteoriteHandler.getScheduler().isQueued(RandomMeteoriteHandler.getSchedulerId())) {
                     sender.sendMessage(MessageUtil.RANDOMNOTFALLING);
                  } else {
                     RandomMeteoriteHandler.getScheduler().cancelTask(RandomMeteoriteHandler.getSchedulerId());
                     sender.sendMessage(MessageUtil.STOPRANDOM);
                  }

                  return true;
               }
            default:
               this.sendPlayerUnknownCommand(sender);
               return true;
            }
         case 2:
            var6 = args[0].toLowerCase();
            var7 = -1;
            switch(var6.hashCode()) {
            case 109413407:
               if (var6.equals("shoot")) {
                  var7 = 0;
               }
            }

            switch(var7) {
            case 0:
               if (!sender.hasPermission("t3sl4meteor.shoot") && !sender.hasPermission("t3sl4meteor.admin")) {
                  sender.sendMessage(MessageUtil.NOPERM);
                  return true;
               } else if (player != null) {
                  if (this.plugin.getConfig().contains("meteorites") && ((ConfigurationSection)Objects.requireNonNull(this.plugin.getConfig().getConfigurationSection("meteorites"))).getKeys(false).contains(args[1])) {
                     ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("meteorites." + args[1]);
                     if (config != null) {
                        if (config.contains("meteorite-speed")) {
                           this.meteoriteSpeed = config.getDouble("meteorite-speed");
                           if (this.meteoriteSpeed > 5.0D) {
                              this.meteoriteSpeed = 5.0D;
                           } else if (this.meteoriteSpeed < 0.0D) {
                              this.meteoriteSpeed = 1.0D;
                           }
                        }

                        if (MeteoriteCreator.createMeteorite(this.getPlayerLocationForMeteorite(player, 10), this.calculateMeteoriteVectorFromPlayersView(player, this.meteoriteSpeed), this.plugin, config)) {
                           sender.sendMessage(MessageUtil.SHOOTING);
                        } else {
                           sender.sendMessage(MessageUtil.FAILEDMETEOR);
                        }

                        return true;
                     }
                  } else {
                     sender.sendMessage((MessageUtil.INVALID).replaceAll("%invalidmeteor%", args[1]));
                     sender.sendMessage((MessageUtil.AVAILABLEMETEORS).replaceAll("%meteors%", this.getAvailableMeteoriteNames()));
                  }

                  return true;
               } else {
                  sender.sendMessage(MessageUtil.MUSTINGAME);
                  return true;
               }
            default:
               this.sendPlayerUnknownCommand(sender);
               return true;
            }
         default:
            if (!sender.hasPermission("t3sl4meteor.default") && !sender.hasPermission("t3sl4meteor.admin")) {
               sender.sendMessage(MessageUtil.NOPERM);
            } else {
               sender.sendMessage(MessageUtil.TOOMANY);
               for (String s : MessageUtil.INFO) {
                  sender.sendMessage(String.valueOf(s));
               }
            }
         }
      }

      return true;
   }

   private void sendPlayerUnknownCommand(CommandSender sender) {
      if (!sender.hasPermission("t3sl4meteor.default") && !sender.hasPermission("t3sl4meteor.admin")) {
         sender.sendMessage(MessageUtil.NOPERM);
      } else {
         sender.sendMessage(MessageUtil.UNKNOWN);
         for (String s : MessageUtil.INFO) {
            sender.sendMessage(String.valueOf(s));
         }
      }

   }

   private Location getPlayerLocationForMeteorite(Player player, int amountOfBlocksAbovePlayer) {
      Location location = player.getLocation();
      location.add(0.0D, (double)amountOfBlocksAbovePlayer, 0.0D);
      double x = (double)((int)location.getX());
      double z = (double)((int)location.getZ());
      if (x >= 0.0D) {
         x += 0.5D;
      } else {
         x -= 0.5D;
      }

      if (z >= 0.0D) {
         z += 0.5D;
      } else {
         z -= 0.5D;
      }

      location.setX(x);
      location.setZ(z);
      return location;
   }

   private Vector calculateMeteoriteVectorFromPlayersView(Player player, double speed) {
      return new Vector(player.getLocation().getDirection().getX() * speed, player.getLocation().getDirection().getY() * speed, player.getLocation().getDirection().getZ() * speed);
   }

   private String getAvailableMeteoriteNames() {
      StringBuilder meteoriteNames = new StringBuilder();
      Iterator var2 = ((ConfigurationSection)Objects.requireNonNull(this.plugin.getConfig().getConfigurationSection("meteorites"))).getKeys(false).iterator();

      while(var2.hasNext()) {
         String meteoriteStringName = (String)var2.next();
         meteoriteNames.append("'").append(meteoriteStringName).append("', ");
      }

      meteoriteNames.delete(meteoriteNames.length() - 2, meteoriteNames.length());
      return meteoriteNames.toString();
   }
}
