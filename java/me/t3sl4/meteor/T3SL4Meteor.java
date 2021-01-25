package me.t3sl4.meteor;

import java.util.Objects;
import me.t3sl4.meteor.Commands.CommandClass;
import me.t3sl4.meteor.EventListeners.EventListenerClass;
import me.t3sl4.meteor.Guardians.TreasureGuardianCreator;
import me.t3sl4.meteor.Meteorites.MeteoriteCreator;
import me.t3sl4.meteor.Meteorites.RandomMeteoriteHandler;
import me.t3sl4.meteor.Particles.MeteoriteParticleCreator;
import me.t3sl4.meteor.util.MessageUtil;
import me.t3sl4.meteor.util.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class T3SL4Meteor extends JavaPlugin {
   private static SettingsManager manager = SettingsManager.getInstance();

   public void onEnable() {
      this.sendConsoleStartMessage();
      this.saveDefaultConfig();
      CommandClass commandClass = new CommandClass(this);
      EventListenerClass eventListenerClass = new EventListenerClass(this);
      ((PluginCommand)Objects.requireNonNull(this.getCommand("tmeteor"))).setExecutor(commandClass);
      this.getServer().getPluginManager().registerEvents(eventListenerClass, this);
      this.initializePluginRandomizers();
      RandomMeteoriteHandler.randomMeteoriteHandler(this);
      manager.setup(this);
      MessageUtil.loadMessages();
   }

   public void onDisable() {
   }

   private void sendConsoleStartMessage() {
      Bukkit.getConsoleSender().sendMessage("   ");
      Bukkit.getConsoleSender().sendMessage("  ____   __   __  _   _   _____   _____   ____    _       _  _   ");
      Bukkit.getConsoleSender().sendMessage(" / ___|  \\ \\ / / | \\ | | |_   _| |___ /  / ___|  | |     | || |  ");
      Bukkit.getConsoleSender().sendMessage(" \\___ \\   \\ V /  |  \\| |   | |     |_ \\  \\___ \\  | |     | || |_ ");
      Bukkit.getConsoleSender().sendMessage("  ___) |   | |   | |\\  |   | |    ___) |  ___) | | |___  |__   _|");
      Bukkit.getConsoleSender().sendMessage(" |____/    |_|   |_| \\_|   |_|   |____/  |____/  |_____|    |_|  ");
      Bukkit.getConsoleSender().sendMessage("    ");
   }

   public Boolean initializePluginRandomizers() {
      boolean successfulMeteorite = MeteoriteCreator.initializeMeteorites(this);
      boolean successfulTreasureGuardian = true;
      boolean successfulParticles = true;
      if (this.getConfig().contains("enable-treasure-guardian") && this.getConfig().getBoolean("enable-treasure-guardian")) {
         successfulTreasureGuardian = TreasureGuardianCreator.initializeGuardianRandomizer(this);
      }

      if (this.getConfig().contains("enable-meteorite-particles") && this.getConfig().getBoolean("enable-meteorite-particles")) {
         successfulParticles = MeteoriteParticleCreator.initializeParticleRandomizer(this);
      }

      return successfulMeteorite && successfulTreasureGuardian && successfulParticles;
   }
}
