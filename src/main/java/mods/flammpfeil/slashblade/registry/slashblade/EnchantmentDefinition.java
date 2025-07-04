package mods.flammpfeil.slashblade.registry.slashblade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class EnchantmentDefinition {
    public static final Codec<EnchantmentDefinition> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf("id").forGetter(EnchantmentDefinition::getEnchantmentID),
                    Codec.INT.optionalFieldOf("lvl", 1).forGetter(EnchantmentDefinition::getEnchantmentLevel))
            .apply(instance, EnchantmentDefinition::new));

    private final ResourceLocation id;
    private final int lvl;

    public EnchantmentDefinition(ResourceLocation enchantment, int level) {
        this.id = enchantment;
        this.lvl = level;
    }

    public ResourceLocation getEnchantmentID() {
        return id;
    }

    public int getEnchantmentLevel() {
        return lvl;
    }
}
