package mods.flammpfeil.slashblade.registry.slashblade;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PropertiesDefinition {
    public static final Codec<PropertiesDefinition> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(ResourceLocation.CODEC.optionalFieldOf("root_combo", ComboStateRegistry.STANDBY.getId())
                                    .forGetter(PropertiesDefinition::getComboRoot),
                            ResourceLocation.CODEC.optionalFieldOf("slash_art", SlashArtsRegistry.JUDGEMENT_CUT.getId())
                                    .forGetter(PropertiesDefinition::getSpecialAttackType),
                            Codec.FLOAT.optionalFieldOf("attack_base", 4.0F)
                                    .forGetter(PropertiesDefinition::getBaseAttackModifier),
                            Codec.INT.optionalFieldOf("max_damage", 40).forGetter(PropertiesDefinition::getMaxDamage),
                            SwordType.CODEC.listOf().optionalFieldOf("sword_type", Lists.newArrayList())
                                    .forGetter(PropertiesDefinition::getDefaultType),
                            ResourceLocation.CODEC.listOf().optionalFieldOf("special_effects", Lists.newArrayList())
                                    .forGetter(PropertiesDefinition::getSpecialEffects)
                    )
                    .apply(instance, PropertiesDefinition::new));

    private final ResourceLocation comboRoot;
    private final ResourceLocation specialAttackType;
    private final float baseAttackModifier;
    private final int maxDamage;
    private final List<SwordType> defaultType;
    private final List<ResourceLocation> specialEffects;

    private PropertiesDefinition(ResourceLocation comboRoot, ResourceLocation specialAttackType,
                                 float baseAttackModifier, int damage, List<SwordType> defaultType, List<ResourceLocation> specialEffects) {
        this.comboRoot = comboRoot;
        this.specialAttackType = specialAttackType;
        this.baseAttackModifier = baseAttackModifier;
        this.maxDamage = damage;
        this.defaultType = defaultType;
        this.specialEffects = specialEffects;
    }


    public List<ResourceLocation> getSpecialEffects() {
        return specialEffects;
    }

    public ResourceLocation getComboRoot() {
        return comboRoot;
    }

    public ResourceLocation getSpecialAttackType() {
        return specialAttackType;
    }

    public float getBaseAttackModifier() {
        return baseAttackModifier;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public List<SwordType> getDefaultType() {
        return defaultType;
    }

    public static class Builder {

        private ResourceLocation comboRoot;
        private ResourceLocation specialAttackType;
        private float baseAttackModifier;
        private int maxDamage;
        private List<SwordType> defaultType;
        private final List<ResourceLocation> specialEffects;

        private Builder() {
            this.comboRoot = ComboStateRegistry.STANDBY.getId();
            this.specialAttackType = SlashArtsRegistry.JUDGEMENT_CUT.getId();
            this.baseAttackModifier = 4.0F;
            this.maxDamage = 40;
            this.defaultType = Lists.newArrayList();
            this.specialEffects = Lists.newArrayList();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder rootComboState(ResourceLocation comboRoot) {
            this.comboRoot = comboRoot;
            return this;
        }

        public Builder slashArtsType(ResourceLocation specialAttackType) {
            this.specialAttackType = specialAttackType;
            return this;
        }

        public Builder baseAttackModifier(float baseAttackModifier) {
            this.baseAttackModifier = baseAttackModifier;
            return this;
        }

        public Builder maxDamage(int maxDamage) {
            this.maxDamage = maxDamage;
            return this;
        }

        public Builder defaultSwordType(List<SwordType> defaultType) {
            this.defaultType = defaultType;
            return this;
        }

        public Builder addSpecialEffect(ResourceLocation se) {
            this.specialEffects.add(se);
            return this;
        }

        public PropertiesDefinition build() {
            return new PropertiesDefinition(comboRoot, specialAttackType, baseAttackModifier, maxDamage, defaultType, specialEffects);
        }
    }

}
