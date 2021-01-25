package me.t3sl4.meteor.Randomizers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import me.t3sl4.meteor.T3SL4Meteor;
import me.t3sl4.meteor.ExceptionHandling.ConfigException;
import me.t3sl4.meteor.Guardians.TreasureGuardian;
import me.t3sl4.meteor.Meteorites.Meteorite;
import me.t3sl4.meteor.Particles.MeteoriteParticle;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class RandomizerClass {
   private List<RandomizerClass.Chance> chances;
   private int sum;
   private Random random;

   public RandomizerClass(long seed) {
      this.random = new Random(seed);
      this.chances = new ArrayList();
      this.sum = 0;
   }

   public boolean addMaterials(ConfigurationSection configurationSection, T3SL4Meteor plugin) {
      try {
         Iterator var3 = ((ConfigurationSection)Objects.requireNonNull(configurationSection)).getKeys(false).iterator();

         while(var3.hasNext()) {
            String materialString = (String)var3.next();
            Material material = Material.getMaterial(materialString);
            if (material == null) {
               throw new ConfigException("Invalid material type: '" + materialString + "'");
            }

            this.addMaterialChance(material, configurationSection.getInt(materialString));
         }

         return true;
      } catch (ConfigException var6) {
         ConfigException.handleConfigException(plugin, var6);
         return false;
      }
   }

   public void addGuardians(T3SL4Meteor plugin, ConfigurationSection configurationSection) throws ConfigException {
      Iterator var3 = ((ConfigurationSection)Objects.requireNonNull(configurationSection)).getKeys(false).iterator();

      while(var3.hasNext()) {
         String guardianString = (String)var3.next();
         if (configurationSection.getBoolean(guardianString + ".enabled")) {
            TreasureGuardian guardian = new TreasureGuardian(plugin, guardianString);
            this.addGuardianChance(guardian, configurationSection.getInt(guardianString + ".chance"));
            guardian.setGuardianMaterials();
            guardian.setGuardianAttributeValues();
            guardian.setGuardianSpawnSound();
         }
      }

      if (this.chances.isEmpty()) {
         throw new ConfigException("All guardians are individually disabled but 'enable-treasure-guardian' was set to true, please set 'enable-treasure-guardian' to false to avoid errors!");
      }
   }

   public void addParticles(T3SL4Meteor plugin, ConfigurationSection configurationSection) throws ConfigException {
      Iterator var3 = configurationSection.getKeys(false).iterator();

      while(var3.hasNext()) {
         String particleString = (String)var3.next();
         if (configurationSection.getBoolean(particleString + ".enabled")) {
            MeteoriteParticle particle = new MeteoriteParticle(plugin, particleString);
            this.addParticleChance(particle, configurationSection.getInt(particleString + ".chance"));
         }
      }

      if (this.chances.isEmpty()) {
         throw new ConfigException("All particles are individually disabled but 'enable-meteorite-particles' was set to true, please set 'enable-meteorite-particles' to false to avoid errors!");
      }
   }

   public Material getRandomMaterial() {
      int index = this.random.nextInt(this.sum);
      Iterator var2 = this.chances.iterator();

      RandomizerClass.Chance chance;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         chance = (RandomizerClass.Chance)var2.next();
      } while(chance.getLowerLimit() > index || chance.getUpperLimit() <= index);

      return chance.getMaterial();
   }

   public TreasureGuardian getRandomGuardian() {
      int index = this.random.nextInt(this.sum);
      Iterator var2 = this.chances.iterator();

      RandomizerClass.Chance chance;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         chance = (RandomizerClass.Chance)var2.next();
      } while(chance.getLowerLimit() > index || chance.getUpperLimit() <= index);

      return chance.getGuardian();
   }

   public MeteoriteParticle getRandomParticle() {
      int index = this.random.nextInt(this.sum);
      Iterator var2 = this.chances.iterator();

      RandomizerClass.Chance chance;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         chance = (RandomizerClass.Chance)var2.next();
      } while(chance.getLowerLimit() > index || chance.getUpperLimit() <= index);

      return chance.getParticle();
   }

   public Meteorite getRandomMeteorite() {
      int index = this.random.nextInt(this.sum);
      Iterator var2 = this.chances.iterator();

      RandomizerClass.Chance chance;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         chance = (RandomizerClass.Chance)var2.next();
      } while(chance.getLowerLimit() > index || chance.getUpperLimit() <= index);

      return chance.getMeteorite();
   }

   public void addMaterialChance(Material material, int chance) {
      if (!this.chances.contains(material)) {
         this.chances.add(new RandomizerClass.Chance(material, this.sum, this.sum + chance));
         this.sum += chance;
      }

   }

   public void addGuardianChance(TreasureGuardian guardian, int chance) {
      if (!this.chances.contains(guardian)) {
         this.chances.add(new RandomizerClass.Chance(guardian, this.sum, this.sum + chance));
         this.sum += chance;
      }

   }

   public void addParticleChance(MeteoriteParticle particle, int chance) {
      if (!this.chances.contains(particle)) {
         this.chances.add(new RandomizerClass.Chance(particle, this.sum, this.sum + chance));
         this.sum += chance;
      }

   }

   public void addMeteoriteChance(Meteorite meteorite, int chance) {
      if (!this.chances.contains(meteorite)) {
         this.chances.add(new RandomizerClass.Chance(meteorite, this.sum, this.sum + chance));
         this.sum += chance;
      }

   }

   private class Chance {
      private int upperLimit;
      private int lowerLimit;
      private Material material;
      private TreasureGuardian guardian;
      private MeteoriteParticle particle;
      private Meteorite meteorite;

      public Chance(Material material, int lowerLimit, int upperLimit) {
         this.material = material;
         this.upperLimit = upperLimit;
         this.lowerLimit = lowerLimit;
      }

      public Chance(TreasureGuardian guardian, int lowerLimit, int upperLimit) {
         this.guardian = guardian;
         this.upperLimit = upperLimit;
         this.lowerLimit = lowerLimit;
      }

      public Chance(MeteoriteParticle particle, int lowerLimit, int upperLimit) {
         this.particle = particle;
         this.upperLimit = upperLimit;
         this.lowerLimit = lowerLimit;
      }

      public Chance(Meteorite meteorite, int lowerLimit, int upperLimit) {
         this.meteorite = meteorite;
         this.upperLimit = upperLimit;
         this.lowerLimit = lowerLimit;
      }

      public int getUpperLimit() {
         return this.upperLimit;
      }

      public int getLowerLimit() {
         return this.lowerLimit;
      }

      public Material getMaterial() {
         return this.material;
      }

      public TreasureGuardian getGuardian() {
         return this.guardian;
      }

      public MeteoriteParticle getParticle() {
         return this.particle;
      }

      public Meteorite getMeteorite() {
         return this.meteorite;
      }

      public String toString() {
         if (this.material != null) {
            return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.material.toString();
         } else if (this.guardian != null) {
            return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.guardian.toString();
         } else {
            return this.particle != null ? "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.particle.toString() : "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.meteorite.toString();
         }
      }
   }
}
