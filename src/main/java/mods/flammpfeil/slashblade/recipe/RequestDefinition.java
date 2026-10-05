package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.slashblade.EnchantmentDefinition;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record RequestDefinition(ResourceLocation name, int proudSoulCount, int killCount, int refineCount,
                                List<EnchantmentDefinition> enchantments, List<SwordType> defaultType) {
    public static final ResourceLocation EMPTY_NAME = SlashBlade.prefix("none");
    
    public static final Codec<RequestDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("name", EMPTY_NAME)
                .forGetter(RequestDefinition::name),
            Codec.INT.optionalFieldOf("proud_soul", 0).forGetter(RequestDefinition::proudSoulCount),
            Codec.INT.optionalFieldOf("kill", 0).forGetter(RequestDefinition::killCount),
            Codec.INT.optionalFieldOf("refine", 0).forGetter(RequestDefinition::refineCount),
            EnchantmentDefinition.CODEC.listOf().optionalFieldOf("enchantments", Lists.newArrayList())
                .forGetter(RequestDefinition::enchantments),
            SwordType.CODEC.listOf().optionalFieldOf("sword_type", Lists.newArrayList())
                .forGetter(RequestDefinition::defaultType))
        .apply(instance, RequestDefinition::new));
    
    public void initItemStack(ItemStack blade) {
        var state = BladeStateAccess.of(blade).orElseThrow();
        state.setNonEmpty();
        if (!this.name.equals(EMPTY_NAME)) {
            state.setTranslationKey(getTranslationKey());
        }
        state.setProudSoulCount(proudSoulCount());
        state.setKillCount(killCount());
        state.setRefine(refineCount());
        
        this.defaultType.forEach(type -> {
            switch (type) {
                case BEWITCHED -> state.setDefaultBewitched(true);
                case BROKEN -> {
                    blade.setDamageValue(blade.getMaxDamage() - 1);
                    state.setBroken(true);
                }
                case SEALED -> state.setSealed(true);
                default -> {
                }
            }
        });
        state.setDefaultSwordTypes(defaultType);
        
        this.enchantments().forEach(ench -> blade.enchant(ench.getEnchantment(), ench.getEnchantmentLevel()));
        ItemSlashBlade.updateRarity(blade);
    }
    
    public boolean test(ItemStack blade) {
        if (blade.isEmpty()) {
            return false;
        }
        
        if (BladeStateAccess.of(blade).isEmpty()) {
            return false;
        }
        var state = BladeStateAccess.of(blade).orElseThrow();
        boolean nameCheck;
        if (this.name.equals(EMPTY_NAME)) {
            nameCheck = state.getTranslationKey().isBlank();
        } else {
            nameCheck = state.getTranslationKey().equals(getTranslationKey());
        }
        boolean proudCheck = state.getProudSoulCount() >= this.proudSoulCount();
        boolean killCheck = state.getKillCount() >= this.killCount();
        boolean refineCheck = state.getRefine() >= this.refineCount();
        
        for (var enchantment : this.enchantments()) {
            var ench = enchantment.getEnchantment();
            var requiredLevel = enchantment.getEnchantmentLevel();
            
            if (blade.getEnchantmentLevel(ench) < requiredLevel) {
                return false;
            }
            
        }
        
        boolean types = SwordType.from(blade).containsAll(this.defaultType());
        
        return nameCheck && proudCheck && killCheck && refineCheck && types;
    }
    
    public String getTranslationKey() {
        return Util.makeDescriptionId("item", this.name());
    }
    
    public static class Builder {
        private ResourceLocation name;
        private int proudCount;
        private int killCount;
        private int refineCount;
        private final List<EnchantmentDefinition> enchantments;
        private final List<SwordType> defaultType;
        
        private Builder() {
            this.name = EMPTY_NAME;
            this.proudCount = 0;
            this.killCount = 0;
            this.refineCount = 0;
            this.enchantments = new ArrayList<>();
            this.defaultType = new ArrayList<>();
        }
        
        public static Builder newInstance() {
            return new Builder();
        }
        
        public Builder name(ResourceLocation name) {
            this.name = name;
            return this;
        }
        
        public Builder proudSoul(int proudCount) {
            this.proudCount = proudCount;
            return this;
        }
        
        public Builder killCount(int killCount) {
            this.killCount = killCount;
            return this;
        }
        
        public Builder refineCount(int refineCount) {
            this.refineCount = refineCount;
            return this;
        }
        
        public Builder addEnchantment(EnchantmentDefinition... enchantments) {
            Collections.addAll(this.enchantments, enchantments);
            return this;
        }
        
        public Builder addSwordType(SwordType... types) {
            Collections.addAll(this.defaultType, types);
            return this;
        }
        
        public RequestDefinition build() {
            return new RequestDefinition(name, proudCount, killCount, refineCount, enchantments, defaultType);
        }
    }
}
