package mods.flammpfeil.slashblade.data;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.data.advancement.SlashBladeAdvancements;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeEntityDropBuiltInRegistry;
import mods.flammpfeil.slashblade.data.tag.SlashBladeEntityTypeTagProvider;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTagProvider;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class DataGen {
    @SubscribeEvent
    public static void dataGen(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        final RegistrySetBuilder bladeBuilder = new RegistrySetBuilder().add(SlashBladeDefinition.REGISTRY_KEY,
            SlashBladeBuiltInRegistry::registerAll);
        
        final RegistrySetBuilder bladeDropBuilder = new RegistrySetBuilder().add(EntityDropEntry.REGISTRY_KEY,
            SlashBladeEntityDropBuiltInRegistry::registerAll);
        
        dataGenerator.addProvider(event.includeServer(), new SlashBladeRecipeProvider(packOutput, lookupProvider));
        dataGenerator.addProvider(event.includeServer(),
            new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, bladeBuilder, Set.of("minecraft", SlashBlade.MODID)) {
                
                @Override
                public String getName() {
                    return "SlashBlade Definition Registry";
                }
                
            });
        dataGenerator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider,
            bladeDropBuilder, Set.of(SlashBlade.MODID)) {
            
            @Override
            public String getName() {
                return "SlashBlade Entity Drop Entry Registry";
            }
            
        });
        dataGenerator.addProvider(event.includeServer(),
            new SlashBladeEntityTypeTagProvider(packOutput, lookupProvider, SlashBlade.MODID, existingFileHelper));
        
        var itemTagsProvider = new SlashBladeItemTagProvider(packOutput, lookupProvider,
            CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
            existingFileHelper);
        dataGenerator.addProvider(event.includeServer(), itemTagsProvider);
        dataGenerator.addProvider(event.includeServer(), new SlashBladeAdvancements(packOutput, lookupProvider, existingFileHelper));
    }
    
}
