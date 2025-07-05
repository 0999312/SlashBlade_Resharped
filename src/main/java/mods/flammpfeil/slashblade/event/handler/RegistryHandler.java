package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void onDatapackRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SlashBladeDefinition.REGISTRY_KEY, SlashBladeDefinition.CODEC,
                SlashBladeDefinition.CODEC);

        event.dataPackRegistry(EntityDropEntry.REGISTRY_KEY, EntityDropEntry.CODEC, EntityDropEntry.CODEC);
    }

    @SubscribeEvent
    public static void registerSerializers(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> CraftingHelper
                .register(SlashBlade.prefix("blade"), SlashBladeIngredient.Serializer.INSTANCE));
    }
}