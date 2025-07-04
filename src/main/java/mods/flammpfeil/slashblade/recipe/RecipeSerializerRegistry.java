package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, SlashBlade.MODID);

    public static final RegistryObject<RecipeSerializer<?>> SLASHBLADE_SHAPED = RECIPE_SERIALIZER
            .register("shaped_blade", () -> SlashBladeShapedRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<?>> PROUDSOUL_RECIPE = RECIPE_SERIALIZER
            .register("proudsoul", () -> ProudsoulShapelessRecipe.SERIALIZER);
}
