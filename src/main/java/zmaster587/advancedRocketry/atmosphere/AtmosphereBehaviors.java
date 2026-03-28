package zmaster587.advancedRocketry.atmosphere;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.logging.log4j.util.TriConsumer;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.network.PacketOxygenState;
import zmaster587.libVulpes.network.PacketHandler;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static zmaster587.advancedRocketry.atmosphere.AtmosphereType.SUPERHEATED;

public class AtmosphereBehaviors {
    /**
     * Magic number (20th bit), flag to indicate setBlockState() was called by a {@link BlockEffect}
     */
    private static final int MODIFIED_BY_ATMOSPHERE = 0b10000000000000000000;
    private static final int BLOCK_EFFECT_FLAG = MODIFIED_BY_ATMOSPHERE | Constants.BlockFlags.DEFAULT;

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

    public enum BlockEffect {
        COMBUST(EnumSet.of(SUPERHEATED), (world, pos, state) -> {
            Block block = state.getBlock();
            Material material = state.getMaterial();
            if (block instanceof IPlantable
                    || material == Material.WEB || material == Material.CLOTH || material == Material.GOURD
                    || isFoliage(world, state, pos, block, material)) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState(), BLOCK_EFFECT_FLAG);
            }
        }),
        VAPORIZE_GAS(EnumSet.of(AtmosphereType.VACUUM), (world, pos, state) -> {
            Block block = state.getBlock();
            Material material = state.getMaterial();
            if (material == Material.WATER && block instanceof IFluidBlock) {
                IFluidBlock fluidBlock = (IFluidBlock) block;
                if (fluidBlock.getFluid().isGaseous()) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), BLOCK_EFFECT_FLAG);
                }
            }
        }),
        // TODO: implement SUFFOCATE
        // sure.. causes stackoverflow left right center
        /*
        else if (!handler.getAtmosphereType(bpos).allowsCombustion()) {
            if (world.getBlockState(bpos).getBlock().isLeaves(world.getBlockState(bpos), world, bpos)) {
                if (!(Boolean)world.getBlockState(bpos).getValue(BlockLeaves.CHECK_DECAY)) {
                    world.setBlockToAir(bpos);
                }
            } else if (world.getBlockState(bpos).getMaterial() == Material.FIRE) {
                world.setBlockToAir(bpos);
            } else if (world.getBlockState(bpos).getMaterial() == Material.CACTUS) {
                world.setBlockToAir(bpos);
            } else if (world.getBlockState(bpos).getMaterial() == Material.PLANTS && world.getBlockState(bpos).getBlock() != Blocks.DEADBUSH) {
                world.setBlockState(bpos, Blocks.DEADBUSH.getDefaultState());
            } else if (world.getBlockState(bpos).getMaterial() == Material.VINE) {
                world.setBlockToAir(bpos);
            } else if (world.getBlockState(bpos).getMaterial() == Material.GRASS) {
                world.setBlockState(bpos, Blocks.DIRT.getDefaultState());
            }
        }
         */
        // TODO: implement VAPORIZE_WATER
        // Water blocks should also vaporize and disappear
        /*
        yes but not like this because it crashes the game
        every updated water causes the water next to it to update -> stackoverflow -> server goes boom


        if (handler.getAtmosphereType(bpos) == AtmosphereType.SUPERHEATED || handler.getAtmosphereType(bpos) == AtmosphereType.SUPERHEATEDNOO2 || handler.getAtmosphereType(bpos) == AtmosphereType.VERYHOT || handler.getAtmosphereType(bpos) == AtmosphereType.VERYHOTNOO2) {
            if (world.getBlockState(bpos).getMaterial() == Material.WATER && world.getBlockState(bpos).getValue(BlockLiquid.LEVEL) == 0) {
                world.setBlockToAir(bpos);
            }
        }
         */
        ;

        private final EnumSet<AtmosphereType> validTypes;
        private final TriConsumer<World, BlockPos, IBlockState> behavior;

        BlockEffect(EnumSet<AtmosphereType> validTypes, TriConsumer<World, BlockPos, IBlockState> behavior) {
            this.validTypes = validTypes;
            this.behavior = behavior;
        }

        public boolean canHandle(AtmosphereType type) {
            return validTypes.contains(type);
        }

        public boolean handle(World world, BlockPos pos, IBlockState newState, int flags) {
            // Prevent recursive calls
            if (flags == BLOCK_EFFECT_FLAG) {
                return false;
            }
            behavior.accept(world, pos, newState);
            return true;
        }

        private static boolean isFoliage(World world, IBlockState state, BlockPos pos, Block block, Material material) {
            return material == Material.LEAVES || material == Material.PLANTS || material == Material.VINE
                    || block.isFoliage(world, pos) || block.isWood(world, pos) || block.isLeaves(state, world, pos);
        }
    }
}
