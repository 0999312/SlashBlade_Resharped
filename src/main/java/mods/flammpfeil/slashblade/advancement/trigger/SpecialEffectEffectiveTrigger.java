package mods.flammpfeil.slashblade.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.advancement.SlashBladeItemPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SpecialEffectEffectiveTrigger extends SimpleCriterionTrigger<SpecialEffectEffectiveTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }
    
    public void trigger(ServerPlayer player, ItemStack item, ResourceLocation specialEffect) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(item, specialEffect));
    }
    
    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<ResourceLocation> specialEffect,
                                  Optional<ItemPredicate> item,
                                  Optional<SlashBladeItemPredicate> slashBladeItemPredicate
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_337356_ -> p_337356_.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceLocation.CODEC.optionalFieldOf("special_effect").forGetter(TriggerInstance::specialEffect),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                SlashBladeItemPredicate.CODEC.optionalFieldOf("blade").forGetter(TriggerInstance::slashBladeItemPredicate)
            ).apply(p_337356_, TriggerInstance::new)
        );
        
        public boolean matches(ItemStack item, ResourceLocation specialEffect) {
            boolean flag = this.specialEffect.map(s -> s.equals(specialEffect)).orElse(false);
            if (this.item.isPresent()) {
                flag &= this.item.map(s -> s.test(item)).orElse(false);
            }
            if (this.slashBladeItemPredicate.isPresent()) {
                flag &= this.slashBladeItemPredicate.map(s -> s.matches(item)).orElse(false);
            }
            return flag;
        }
    }
}
