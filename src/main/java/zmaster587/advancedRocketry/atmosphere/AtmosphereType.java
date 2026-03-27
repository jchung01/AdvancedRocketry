package zmaster587.advancedRocketry.atmosphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import zmaster587.advancedRocketry.atmosphere.AtmosphereBehaviors.EntityEffect;
import zmaster587.libVulpes.LibVulpes;

public enum AtmosphereType implements IAtmosphere, AtmosphereImmunity {
    //We're probably not getting a polluted atmosphere type
    AIR(false, true, true, "air", "", EntityEffect.NONE, EquipmentRequirement.NONE),
    PRESSURIZEDAIR(false, true, true, "PressurizedAir", "", EntityEffect.NONE, EquipmentRequirement.NONE),
    LOWOXYGEN(true, false, true, "lowO2", "msg.noOxygen", EntityEffect.LOW_OXYGEN, EquipmentRequirement.MASK_ONLY),
    NOO2(true, false, false, "NoO2", "msg.noOxygen", EntityEffect.NO_OXYGEN, EquipmentRequirement.MASK_ONLY),
    VACUUM(true, false, false, "vacuum", "msg.noOxygen", EntityEffect.VACUUM, EquipmentRequirement.FULL),
    HIGHPRESSURE(true, false, true, "HighPressure", "msg.tooDense", EntityEffect.HIGH_PRESSURE, EquipmentRequirement.FULL),
    HIGHPRESSURENOO2(true, false, false, "HighPressureNoO2", "msg.noOxygen", EntityEffect.HIGH_PRESSURE_NO_OXYGEN, EquipmentRequirement.FULL),
    SUPERHIGHPRESSURE(true, false, true, "SuperHighPressure", "msg.muchTooDense", EntityEffect.SUPER_HIGH_PRESSURE, EquipmentRequirement.FULL),
    SUPERHIGHPRESSURENOO2(true, false, false, "SuperHighPressureNoO2", "msg.noOxygen", EntityEffect.SUPER_HIGH_PRESSURE, EquipmentRequirement.FULL),
    VERYHOT(true, false, true, "VeryHot", "msg.tooHot", EntityEffect.VERY_HOT, EquipmentRequirement.FULL),
    VERYHOTNOO2(true, false, false, "VeryHotNoO2", "msg.noOxygen", EntityEffect.VERY_HOT_NO_OXYGEN, EquipmentRequirement.FULL),
    SUPERHEATED(true, false, true, "Superheated", "msg.tooHot", EntityEffect.SUPERHEATED, EquipmentRequirement.FULL),
    SUPERHEATEDNOO2(true, false, false, "SuperheatedNoOxygen", "msg.noOxygen", EntityEffect.SUPERHEATED_NO_OXYGEN, EquipmentRequirement.FULL),
    ;

    private boolean allowsCombustion;
    private boolean isBreathable;
    private final boolean canTick;
    private final String name;
    private final String translationKey;
    private final EntityEffect entityEffect;
    private final EquipmentRequirement equipmentRequirement;

    AtmosphereType(boolean canTick, boolean isBreathable, boolean allowsCombustion, String name, String translationKey, EntityEffect entityEffect, EquipmentRequirement equipmentRequirement) {
        this.allowsCombustion = allowsCombustion;
        this.isBreathable = isBreathable;
        this.canTick = canTick;
        this.name = name;
        this.translationKey = translationKey;
        this.entityEffect = entityEffect;
        this.equipmentRequirement = equipmentRequirement;
        AtmosphereRegister.getInstance().registerAtmosphere(this);
    }

    /**
     * Should the gas run a tick on every player in it?  Calls onTick(EntityLiving base)
     *
     * @return true if the atmosphere performs an action every tick
     */
    public boolean canTick() {
        return canTick;
    }

    //TODO: check for all entities (?)

    public boolean isImmune(EntityLivingBase entity) {
        return AtmosphereImmunity.super.isImmune(entity);
    }

    public boolean isImmune(Class<? extends Entity> clazz) {
        return isBreathable() || ARConfiguration.getCurrentConfig().bypassEntity.contains(clazz);
    }

    @Override
    public boolean isBreathable() {
        return isBreathable;
    }

    public boolean allowsCombustion() {
        return allowsCombustion;
    }

    /**
     * Sets the atmosphere to be breathable or not breathable
     *
     * @param isBreathable true if breathable, false otherwise
     */
    public void setIsBreathable(boolean isBreathable) {
        this.isBreathable = isBreathable;
    }

    /**
     * Sets the atmosphere to allow combustion or not to allow combustion
     *
     * @param allowsCombustion true if combustion is allowed, false otherwise
     */
    public void setAllowsCombustion(boolean allowsCombustion) {
        this.allowsCombustion = allowsCombustion;
    }

    /**
     * @return unlocalized message to display when player is in the gas with no protection
     */
    public String getDisplayMessage() {
        return translationKey.isEmpty() ? "" : LibVulpes.proxy.getLocalizedString(translationKey);
    }

    //TODO: tick for all entities (?)

    public void onTick(EntityLivingBase entity) {
        entityEffect.handle(entity, this::isImmune);
    }

    @Override
    public String getUnlocalizedName() {
        return name;
    }

    @Override
    public IAtmosphere getAtmosphere() {
        return this;
    }

    @Override
    public EquipmentRequirement getEquipmentRequirement() {
        return equipmentRequirement;
    }
}
