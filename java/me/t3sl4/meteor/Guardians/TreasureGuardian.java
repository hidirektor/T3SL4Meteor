package me.t3sl4.meteor.Guardians;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.Meteorites.Meteorite;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class TreasureGuardian {
   private T3SL4Meteor plugin;
   private ConfigurationSection config;
   private Entity guardian;
   private String guardianConfigName;
   private EntityType entityType;
   private Material mainHand;
   private Material offHand;
   private Material helmet;
   private Material chestplate;
   private Material leggings;
   private Material boots;
   private double maxHealth;
   private double attackDamage;
   private double movementSpeed;
   private Sound spawnSound;
   private float spawnSoundVolume;
   private float spawnSoundPitch;

   public TreasureGuardian(T3SL4Meteor plugin, String guardianConfigName) throws ConfigException {
      this.plugin = plugin;
      this.guardianConfigName = guardianConfigName;
      this.config = plugin.getConfig().getConfigurationSection("possible-guardians." + guardianConfigName);
      if (this.config == null) {
         throw new ConfigException("Missing config section possible-guardians." + guardianConfigName);
      } else {
         this.setEntityType();
      }
   }

   public void spawnGuardian(Location spawnLocation, Player player) {
      this.sendPlayerMessage(player);
      if (this.config.contains("commands")) {
         Iterator var3 = this.config.getStringList("commands").iterator();

         while(var3.hasNext()) {
            String command = (String)var3.next();
            if (!command.equals("")) {
               command = Meteorite.setLocationPlaceholders(command, spawnLocation);
               command = command.replaceAll("%player%", player.getName());
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
         }
      }

      if (!this.config.contains("use-command-only") || !this.config.getBoolean("use-command-only")) {
         this.guardian = ((World)Objects.requireNonNull(spawnLocation.getWorld())).spawnEntity(spawnLocation, this.entityType);
         this.setDisplayName();
         this.setGuardianEquipment();
         this.setGuardianAttributes();
         this.playSpawnSound(player, spawnLocation);
      }

   }

   private void playSpawnSound(Player player, Location spawnLocation) {
      player.playSound(spawnLocation, this.spawnSound, this.spawnSoundVolume, this.spawnSoundPitch);
   }

   private void sendPlayerMessage(Player player) {
      if (this.config.contains("player-message") && !Objects.equals(this.config.getString("player-message"), "")) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', (String)Objects.requireNonNull(this.config.getString("player-message"))));
      }

   }

   private void setEntityType() throws ConfigException {
      if (!Arrays.asList(EntityType.values()).toString().contains(((String)Objects.requireNonNull(this.config.getString(".guardian-mob-type"))).toUpperCase())) {
         throw new ConfigException("Invalid mob type: '" + this.config.getString(".guardian-mob-type") + "' for guardian '" + this.guardianConfigName + "'");
      } else {
         try {
            this.entityType = EntityType.valueOf(((String)Objects.requireNonNull(this.config.getString(".guardian-mob-type"))).toUpperCase());
         } catch (IllegalArgumentException var2) {
            this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] Invalid mob type: '" + this.config.getString(".guardian-mob-type") + "' for guardian '" + this.guardianConfigName + "'");
         }

      }
   }

   private void setDisplayName() {
      if (this.config.getString(".guardian-display-name") != null && !Objects.equals(this.config.getString(".guardian-display-name"), "")) {
         this.guardian.setCustomName(ChatColor.translateAlternateColorCodes('&', (String)Objects.requireNonNull(this.config.getString(".guardian-display-name"))));
         this.guardian.setCustomNameVisible(true);
      }

   }

   private void setGuardianEquipment() {
      if (this.config.getBoolean("enable-guardian-equipment")) {
         EntityEquipment equipment = ((LivingEntity)this.guardian).getEquipment();
         if (equipment != null) {
            if (this.mainHand != null) {
               equipment.setItemInHand(new ItemStack(this.mainHand));
            }

            //if (this.offHand != null) {
               //equipment.setItemInOffHand(new ItemStack(this.offHand));
            //}

            if (this.helmet != null) {
               equipment.setHelmet(new ItemStack(this.helmet));
            }

            if (this.chestplate != null) {
               equipment.setChestplate(new ItemStack(this.chestplate));
            }

            if (this.leggings != null) {
               equipment.setLeggings(new ItemStack(this.leggings));
            }

            if (this.boots != null) {
               equipment.setBoots(new ItemStack(this.boots));
            }
         }
      }

   }

   private void setGuardianAttributes() {
      EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) this.guardian).getHandle();
      if (this.config.contains("guardian-attack-damage") && !((String)Objects.requireNonNull(this.config.getString("guardian-attack-damage"))).equalsIgnoreCase("default")) {
         nmsEntity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.attackDamage);
      }

      if (this.config.contains("guardian-health") && !((String)Objects.requireNonNull(this.config.getString("guardian-health"))).equalsIgnoreCase("default")) {
         nmsEntity.getAttributeInstance(GenericAttributes.maxHealth).setValue(this.maxHealth);
      }

      if (this.config.contains("guardian-movement-speed") && !((String)Objects.requireNonNull(this.config.getString("guardian-movement-speed"))).equalsIgnoreCase("default")) {
         nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.movementSpeed);
      }

   }

   public void setGuardianMaterials() throws ConfigException {
      if (this.config.getBoolean("enable-guardian-equipment")) {
         ConfigurationSection materialConfig = this.config.getConfigurationSection(".guardian-equipment");
         if (materialConfig == null) {
            throw new ConfigException("Missing guardian-equipment in config file for guardian: " + this.guardianConfigName);
         }

         if (materialConfig.contains(".main-hand") && !Objects.equals(materialConfig.getString(".main-hand"), "")) {
            this.mainHand = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".main-hand")));
            if (this.mainHand == null) {
               throw new ConfigException("Invalid material type for main hand '" + materialConfig.getString(".main-hand") + "' of guardian: " + this.guardianConfigName);
            }
         }

         if (materialConfig.contains(".off-hand") && !Objects.equals(materialConfig.getString(".off-hand"), "")) {
            this.offHand = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".off-hand")));
            if (this.offHand == null) {
               throw new ConfigException("Invalid material type for off hand '" + materialConfig.getString(".off-hand") + "' of guardian: " + this.guardianConfigName);
            }
         }

         if (materialConfig.contains(".helmet") && !Objects.equals(materialConfig.getString(".helmet"), "")) {
            this.helmet = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".helmet")));
            if (this.helmet == null) {
               throw new ConfigException("Invalid material type for helmet '" + materialConfig.getString(".helmet") + "' of guardian: " + this.guardianConfigName);
            }
         }

         if (materialConfig.contains(".chestplate") && !Objects.equals(materialConfig.getString(".chestplate"), "")) {
            this.chestplate = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".chestplate")));
            if (this.chestplate == null) {
               throw new ConfigException("Invalid material type for chestplate '" + materialConfig.getString(".chestplate") + "' of guardian: " + this.guardianConfigName);
            }
         }

         if (materialConfig.contains(".leggings") && !Objects.equals(materialConfig.getString(".leggings"), "")) {
            this.leggings = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".leggings")));
            if (this.leggings == null) {
               throw new ConfigException("Invalid material type for leggings '" + materialConfig.getString(".leggings") + "' of guardian: " + this.guardianConfigName);
            }
         }

         if (materialConfig.contains(".boots") && !Objects.equals(materialConfig.getString(".boots"), "")) {
            this.boots = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".boots")));
            if (this.boots == null) {
               throw new ConfigException("Invalid material type for boots '" + materialConfig.getString(".boots") + "' of guardian: " + this.guardianConfigName);
            }
         }
      }

   }

   public void setGuardianAttributeValues() throws ConfigException {
      if (this.config.contains("guardian-attack-damage") && !((String)Objects.requireNonNull(this.config.getString("guardian-attack-damage"))).equalsIgnoreCase("default")) {
         if (!(this.config.getDouble("guardian-attack-damage") >= 0.0D) || !(this.config.getDouble("guardian-attack-damage") <= 500.0D)) {
            throw new ConfigException("guardian-attack-damage must be a value from 0 - 500 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-attack-damage"));
         }

         this.attackDamage = this.config.getDouble("guardian-attack-damage");
      }

      if (this.config.contains("guardian-health") && !((String)Objects.requireNonNull(this.config.getString("guardian-health"))).equalsIgnoreCase("default")) {
         if (!(this.config.getDouble("guardian-health") >= 1.0D) || !(this.config.getDouble("guardian-health") <= 2000.0D)) {
            throw new ConfigException("guardian-health must be a value from 1 - 2000 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-health"));
         }

         this.maxHealth = this.config.getDouble("guardian-health");
      }

      if (this.config.contains("guardian-movement-speed") && !((String)Objects.requireNonNull(this.config.getString("guardian-movement-speed"))).equalsIgnoreCase("default")) {
         if (!(this.config.getDouble("guardian-movement-speed") >= 0.0D) || !(this.config.getDouble("guardian-movement-speed") <= 0.7D)) {
            throw new ConfigException("guardian-health must be a value from 0 - 1 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-movement-speed"));
         }

         this.movementSpeed = this.config.getDouble("guardian-movement-speed");
      }

   }

   public void setGuardianSpawnSound() throws ConfigException {
      if (!Arrays.asList(Sound.values()).toString().contains(((String)Objects.requireNonNull(this.config.getString(".guardian-spawn-sound"))).toUpperCase())) {
         throw new ConfigException("Invalid sound type: '" + this.config.getString(".guardian-spawn-sound") + "' for guardian '" + this.guardianConfigName + "'");
      } else {
         try {
            if (this.config.contains("guardian-spawn-sound") && !Objects.equals(this.config.getString("guardian-spawn-sound"), "")) {
               this.spawnSound = Sound.valueOf(((String)Objects.requireNonNull(this.config.getString("guardian-spawn-sound"))).toUpperCase());
            }
         } catch (IllegalArgumentException var2) {
            this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] Invalid sound type: '" + this.config.getString(".guardian-spawn-sound") + "' for guardian '" + this.guardianConfigName + "'");
         }

         if (this.config.contains("guardian-spawn-sound-volume")) {
            if (!(this.config.getDouble("guardian-spawn-sound-volume") >= 0.0D)) {
               throw new ConfigException("guardian-spawn-sound-volume must be a value from larger than 0 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-spawn-sound-volume"));
            }

            this.spawnSoundVolume = (float)this.config.getDouble("guardian-spawn-sound-volume");
         } else {
            this.spawnSoundVolume = 5.0F;
         }

         if (this.config.contains("guardian-spawn-sound-pitch")) {
            if (!(this.config.getDouble("guardian-spawn-sound-pitch") >= 0.5D) || !(this.config.getDouble("guardian-spawn-sound-pitch") <= 2.0D)) {
               throw new ConfigException("guardian-spawn-sound-pitch must be a value from 0.5 - 2.0 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-spawn-sound-pitch"));
            }

            this.spawnSoundPitch = (float)this.config.getDouble("guardian-spawn-sound-pitch");
         } else {
            this.spawnSoundPitch = 1.0F;
         }

      }
   }
}
