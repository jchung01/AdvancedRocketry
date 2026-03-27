package zmaster587.advancedRocketry.atmosphere;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.network.PacketOxygenState;
import zmaster587.libVulpes.network.PacketHandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AtmosphereBehaviors {
    public enum EntityEffect {
        NONE(0, (entity) -> {}),
        LOW_OXYGEN(20, (entity) -> {
            entity.attackEntityFrom(AtmosphereHandler.lowOxygenDamage, 1);
            debilitate(entity, 2, 2);
            syncOxygen(entity);
        }),
        // Double effectiveness and frequency of LOW_OXYGEN
        NO_OXYGEN(10, (entity) -> {
            entity.attackEntityFrom(AtmosphereHandler.lowOxygenDamage, 1);
            debilitate(entity, 4, 4);
            applyNausea(entity);
            syncOxygen(entity);
        }),
        VACUUM(10, (entity) -> {
            entity.attackEntityFrom(AtmosphereHandler.vacuumDamage, ARConfiguration.getCurrentConfig().vacuumDamage);
            debilitate(entity, 4, 4);
            applyNausea(entity);
            syncOxygen(entity);
        }),
        HIGH_PRESSURE(20, (entity) -> {
            debilitate(entity, 2, 2);
        }),
        HIGH_PRESSURE_NO_OXYGEN(10, NO_OXYGEN.behavior),
        SUPER_HIGH_PRESSURE(20, (entity) -> {
            debilitate(entity, 3, 3);
            entity.attackEntityFrom(AtmosphereHandler.oxygenToxicityDamage, 1);
            syncOxygen(entity);
        }),
        SUPER_HIGH_PRESSURE_NO_OXYGEN(10, (entity) -> {
           NO_OXYGEN.behavior.accept(entity);
            // Removes the ability to jump. Nitrogen/other gas narcosis
            entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 40, 150));
        }),
        VERY_HOT(20, (entity) -> {
            entity.setFire(1);
            entity.attackEntityFrom(AtmosphereHandler.heatDamage, 1);
            debilitate(entity, 3, 0);
        }),
        VERY_HOT_NO_OXYGEN(10, (entity) -> {
            NO_OXYGEN.behavior.accept(entity);
            if (entity.world.getTotalWorldTime() % 20 == 0) {
                // Heat, but no combustion
                entity.attackEntityFrom(AtmosphereHandler.heatDamage, 1);
            }
        }),
        SUPERHEATED(20, (entity) -> {
            entity.setFire(1);
            entity.attackEntityFrom(AtmosphereHandler.heatDamage, 4);
            debilitate(entity, 3, 0);
        }),
        SUPERHEATED_NO_OXYGEN(10, (entity) -> {
            NO_OXYGEN.behavior.accept(entity);
            if (entity.world.getTotalWorldTime() % 20 == 0) {
                // Heat, but no combustion
                entity.attackEntityFrom(AtmosphereHandler.heatDamage, 4);
            }
        }),
        ;

        private final int tickRate;
        private final Consumer<EntityLivingBase> behavior;

        EntityEffect(int tickRate, Consumer<EntityLivingBase> behavior) {
            this.tickRate = tickRate;
            this.behavior = behavior;
        }

        public void handle(EntityLivingBase entity, Predicate<EntityLivingBase> isImmune) {
            if (entity.world.getTotalWorldTime() % tickRate != 0) return;
            if (isImmune.test(entity)) return;

            behavior.accept(entity);
        }

        private static void debilitate(EntityLivingBase entity, int slowLevel, int fatigueLevel) {
            if (slowLevel > 0) {
                entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, slowLevel));
            }
            if (fatigueLevel > 0) {
                entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 40, fatigueLevel));
            }
        }

        private static void applyNausea(EntityLivingBase entity) {
            if (!ARConfiguration.getCurrentConfig().enableNausea) return;

            entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 400, 1));
        }

        private static void syncOxygen(EntityLivingBase entity) {
            if (!(entity instanceof EntityPlayer)) return;

            PacketHandler.sendToPlayer(new PacketOxygenState(), (EntityPlayer) entity);
        }
    }
}
