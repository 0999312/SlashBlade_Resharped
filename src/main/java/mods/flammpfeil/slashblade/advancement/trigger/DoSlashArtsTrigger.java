package mods.flammpfeil.slashblade.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.advancement.SlashBladeItemPredicate;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class DoSlashArtsTrigger extends SimpleCriterionTrigger<DoSlashArtsTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }
    
    public void trigger(ServerPlayer player, ItemStack item, int elapsed, @Nullable SlashArts.ArtsType type) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(item, elapsed, type));
    }
    
    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<ResourceLocation> slashArts,
                                  Optional<ItemPredicate> item,
                                  Optional<SlashBladeItemPredicate> slashBladeItemPredicate,
                                  Optional<SlashArts.ArtsType> artsType,
                                  MinMaxBounds.Ints elapsed
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_337356_ -> p_337356_.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceLocation.CODEC.optionalFieldOf("slash_arts").forGetter(TriggerInstance::slashArts),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                SlashBladeItemPredicate.CODEC.optionalFieldOf("blade").forGetter(TriggerInstance::slashBladeItemPredicate),
                SlashArts.ArtsType.CODEC.optionalFieldOf("type").forGetter(TriggerInstance::artsType),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("elapsed", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::elapsed)
            ).apply(p_337356_, TriggerInstance::new)
        );
        
        public boolean matches(ItemStack item, int elapsed, @Nullable SlashArts.ArtsType type) {
            boolean flag = this.slashArts.map(s -> BladeStateAccess.of(item)
                .map(state -> state.getSlashArtsKey().equals(s)).orElse(false)).orElse(false);
            if (this.item.isPresent()) {
                flag &= this.item.map(s -> s.test(item)).orElse(false);
            }
            if (this.slashBladeItemPredicate.isPresent()) {
                flag &= this.slashBladeItemPredicate.map(s -> s.matches(item)).orElse(false);
            }
            if (this.artsType.isPresent()) {
                flag &= this.artsType.map(type1 -> type1.equals(type)).orElse(false);
            }
            flag &= this.elapsed.matches(elapsed);
            return flag;
        }
    }
}
