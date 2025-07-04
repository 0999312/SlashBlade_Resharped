package mods.flammpfeil.slashblade.registry.specialeffects;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

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

    public static boolean isEffective(SpecialEffect se, int level) {
        return se.requestLevel <= level;
    }

    public static boolean isEffective(ResourceLocation id, int level) {
        return Objects.requireNonNull(SpecialEffectsRegistry.REGISTRY.get().getValue(id)).getRequestLevel() <= level;
    }

    public static Component getDescription(ResourceLocation id) {
        return Objects.requireNonNull(SpecialEffectsRegistry.REGISTRY.get().getValue(id)).getDescription();
    }

    public static int getRequestLevel(ResourceLocation id) {
        return Objects.requireNonNull(SpecialEffectsRegistry.REGISTRY.get().getValue(id)).getRequestLevel();
    }

    public Component getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(SpecialEffectsRegistry.REGISTRY.get().getKey(this)).toString();
    }

    private String descriptionId;

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("se", SpecialEffectsRegistry.REGISTRY.get().getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }
}
