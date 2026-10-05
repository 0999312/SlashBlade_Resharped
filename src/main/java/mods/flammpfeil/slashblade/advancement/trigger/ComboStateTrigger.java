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

import javax.annotation.Nullable;
import java.util.Optional;

public class ComboStateTrigger extends SimpleCriterionTrigger<ComboStateTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }
    
    public void trigger(ServerPlayer player, ResourceLocation comboState, @Nullable ItemStack item) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(comboState, item));
    }
    
    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<ResourceLocation> comboState,
                                  Optional<ItemPredicate> item,
                                  Optional<SlashBladeItemPredicate> slashBladeItemPredicate
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_337356_ -> p_337356_.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceLocation.CODEC.optionalFieldOf("combo_state").forGetter(TriggerInstance::comboState),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                SlashBladeItemPredicate.CODEC.optionalFieldOf("blade").forGetter(TriggerInstance::slashBladeItemPredicate)
            ).apply(p_337356_, TriggerInstance::new)
        );
        
        public boolean matches(ResourceLocation comboState, @Nullable ItemStack item) {
            boolean flag = this.comboState.map(s -> s.equals(comboState)).orElse(false);
            if (!flag) {
                return false;
            }
            if (item != null) {
                if (this.item.isPresent()) {
                    flag = this.item.map(s -> s.test(item)).orElse(false);
                }
                if (this.slashBladeItemPredicate.isPresent()) {
                    flag &= this.slashBladeItemPredicate.map(s -> s.matches(item)).orElse(false);
                }
            }
            return flag;
        }
    }
}
