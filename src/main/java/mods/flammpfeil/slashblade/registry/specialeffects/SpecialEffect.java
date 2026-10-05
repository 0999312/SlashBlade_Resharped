package mods.flammpfeil.slashblade.registry.specialeffects;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class SpecialEffect {
    public static final ResourceKey<Registry<SpecialEffect>> REGISTRY_KEY = ResourceKey
        .createRegistryKey(SlashBlade.prefix("special_effect"));
    
    private final int requestLevel;
    private final boolean isCopiable;
    private final boolean isRemovable;
    
    public SpecialEffect(int requestLevel) {
        this(requestLevel, false, false);
    }
    
    public SpecialEffect(int requestLevel, boolean isCopiable, boolean isRemovable) {
        this.requestLevel = requestLevel;
        this.isCopiable = isCopiable;
        this.isRemovable = isRemovable;
    }
    
    public int getRequestLevel() {
        return requestLevel;
    }
    
    public boolean isCopiable() {
        return isCopiable;
    }
    
    public boolean isRemovable() {
        return isRemovable;
    }
    
    public boolean isEffective(Entity user) {
        if (user instanceof Player player) {
            return this.requestLevel <= player.experienceLevel;
        }
        return false;
    }
    
    public static boolean isEffective(SpecialEffect se, Entity user) {
        return se.isEffective(user);
    }
    
    public static boolean isEffective(ResourceLocation id, Entity user) {
        SpecialEffect specialEffect = SpecialEffectsRegistry.REGISTRY.get(id);
        if (specialEffect == null) {
            throw new NullPointerException("SpecialEffect with id " + id + " not found!");
        }
        return isEffective(specialEffect, user);
    }
    
    /**
     * Use {@link SpecialEffect#isEffective(SpecialEffect, Entity)} instead
     */
    @Deprecated
    public static boolean isEffective(SpecialEffect se, int level) {
        return se.requestLevel <= level;
    }
    
    /**
     * Use {@link SpecialEffect#isEffective(ResourceLocation, Entity)} instead
     */
    @Deprecated
    public static boolean isEffective(ResourceLocation id, int level) {
        SpecialEffect specialEffect = SpecialEffectsRegistry.REGISTRY.get(id);
        if (specialEffect == null) {
            throw new NullPointerException("SpecialEffect with id " + id + " not found!");
        }
        return specialEffect.getRequestLevel() <= level;
    }
    
    public static Component getDescription(ResourceLocation id) {
        SpecialEffect specialEffect = SpecialEffectsRegistry.REGISTRY.get(id);
        if (specialEffect == null) {
            throw new NullPointerException("SpecialEffect with id " + id + " hasn't registered!");
        }
        return specialEffect.getDescription();
    }
    
    public static int getRequestLevel(ResourceLocation id) {
        SpecialEffect specialEffect = SpecialEffectsRegistry.REGISTRY.get(id);
        if (specialEffect == null) {
            throw new NullPointerException("SpecialEffect with id " + id + " hasn't registered!");
        }
        return specialEffect.getRequestLevel();
    }
    
    public Component getDescription() {
        return Component.translatable(this.getDescriptionId());
    }
    
    @Override
    public String toString() {
        ResourceLocation specialEffect = SpecialEffectsRegistry.REGISTRY.getKey(this);
        if (specialEffect == null) {
            throw new NullPointerException("SpecialEffect " + super.toString() + " hasn't registered!");
        }
        return specialEffect.toString();
    }
    
    @Nullable
    private String descriptionId;
    
    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("se", SpecialEffectsRegistry.REGISTRY.getKey(this));
        }
        return this.descriptionId;
    }
    
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }
}
