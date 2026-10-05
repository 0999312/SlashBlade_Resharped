package mods.flammpfeil.slashblade.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public record SlashBladeItemPredicate(RequestDefinition request) implements ItemSubPredicate {
    
    public static final Codec<SlashBladeItemPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        RequestDefinition.CODEC.fieldOf("requestBlade").forGetter(SlashBladeItemPredicate::request)
    ).apply(instance, SlashBladeItemPredicate::new));
    
    @Override
    public boolean matches(ItemStack stack) {
        var name = this.request().name();
        boolean requestCheck = this.request().test(stack);
        if (name.equals(RequestDefinition.EMPTY_NAME)) {
            return requestCheck && stack.is(SlashBladeItems.SLASHBLADE.get());
        }
        if (BuiltInRegistries.ITEM.containsKey(name)) {
            return requestCheck && stack.is(BuiltInRegistries.ITEM.get(name));
        }
        return requestCheck && (stack.getItem() instanceof ItemSlashBlade);
    }
}
