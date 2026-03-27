package zmaster587.advancedRocketry.atmosphere;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import zmaster587.advancedRocketry.api.EntityRocketBase;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.armor.IProtectiveArmor;
import zmaster587.advancedRocketry.api.capability.CapabilitySpaceArmor;
import zmaster587.advancedRocketry.entity.EntityElevatorCapsule;
import zmaster587.advancedRocketry.integration.MatterOvedriveIntegration;
import zmaster587.advancedRocketry.util.ItemAirUtils;

import javax.annotation.Nonnull;

public interface AtmosphereImmunity {
    IAtmosphere getAtmosphere();

    EquipmentRequirement getEquipmentRequirement();

    default boolean isImmune(EntityLivingBase entity) {
        // Fallback to atmosphere property if no equipment required
        if (getEquipmentRequirement() == EquipmentRequirement.NONE) {
            return getAtmosphere().isBreathable();
        }
        // Temporary initial immunity
        if (entity.getEntityData().getLong("arRocketTransferGrace") > entity.world.getTotalWorldTime()) {
            return true;
        }
        // Mod compat
        if (Loader.isModLoaded("matteroverdrive") && MatterOvedriveIntegration.isAndroidNeedNoOxygen(entity)) {
            return true;
        }
        // Immune entities
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            return player.capabilities.isCreativeMode || player.isSpectator();
        }
        if (entity.getRidingEntity() instanceof EntityRocketBase) {
            return true;
        }
        if (entity.getRidingEntity() instanceof EntityElevatorCapsule) {
            return true;
        }

        //Checks if player is wearing spacesuit or anything that extends ItemSpaceArmor
        ItemStack helm = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        boolean helmProtects = protectsFrom(helm);
        if (getEquipmentRequirement() == EquipmentRequirement.MASK_ONLY && helmProtects) {
            // Check chest to decrement air from it
            ItemStack chest = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            return protectsFrom(chest);
        } else if (getEquipmentRequirement() == EquipmentRequirement.FULL) {
            ItemStack leg = entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            ItemStack feet = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            ItemStack chest = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            // Note: protectsFrom(chest) is intentionally the last thing to check here.  This is because java will bail on the check early if others fail
            // this will prevent the O2 level in the chest from being needlessly decremented
            return helmProtects && protectsFrom(leg) && protectsFrom(feet) && protectsFrom(chest);
        }

        return false;
    }

    default boolean protectsFrom(@Nonnull ItemStack stack) {
        if (ItemAirUtils.INSTANCE.isStackValidAirContainer(stack)) {
            ItemAirUtils.ItemAirWrapper itemAirWrapper = new ItemAirUtils.ItemAirWrapper(stack);
            if (itemAirWrapper.protectsFromSubstance(getAtmosphere(), stack, true)) {
                return true;
            }
        }
        if (!stack.isEmpty()) {
            IProtectiveArmor armorCap = stack.getCapability(CapabilitySpaceArmor.PROTECTIVEARMOR, null);
            return armorCap != null && armorCap.protectsFromSubstance(getAtmosphere(), stack, true);
        }
        return false;
    }

    enum EquipmentRequirement {
        NONE,
        MASK_ONLY,
        FULL
    }
}
