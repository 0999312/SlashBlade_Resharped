package mods.flammpfeil.slashblade;

import mods.flammpfeil.slashblade.ability.*;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeDataComponents;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.compat.jei.JEICompat;
import mods.flammpfeil.slashblade.event.BladeMotionEventBroadcaster;
import mods.flammpfeil.slashblade.event.handler.*;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.recipe.RecipeSerializerRegistry;
import mods.flammpfeil.slashblade.registry.*;
import mods.flammpfeil.slashblade.registry.combo.ComboCommands;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SlashBlade.MODID)
public class SlashBlade {
    public static final String MODID = "slashblade";
    
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static IEventBus MOD_EVENT_BUS;
    
    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, path);
    }
    
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    
    public SlashBlade(IEventBus modEventBus, ModContainer container) {
        SlashBlade.MOD_EVENT_BUS = modEventBus;
        
        container.registerConfig(ModConfig.Type.COMMON, SlashBladeConfig.COMMON_CONFIG);
        
        modEventBus.addListener(this::setup);
        modEventBus.addListener(NetworkManager::register);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        
        SlashBladeItems.ITEMS.register(modEventBus);
        CapabilityInputState.ATTACHMENT_TYPES.register(modEventBus);
        CapabilityMobEffect.ATTACHMENT_TYPES.register(modEventBus);
        CapabilityConcentrationRank.ATTACHMENT_TYPES.register(modEventBus);
        ComboStateRegistry.COMBO_STATE.register(modEventBus);
        SlashArtsRegistry.SLASH_ARTS.register(modEventBus);
        SlashBladeCreativeGroup.CREATIVE_MODE_TABS.register(modEventBus);
        RecipeSerializerRegistry.RECIPE_TYPES.register(modEventBus);
        RecipeSerializerRegistry.RECIPE_SERIALIZER.register(modEventBus);
        RecipeSerializerRegistry.INGREDIENT_TYPES.register(modEventBus);
        SpecialEffectsRegistry.SPECIAL_EFFECT.register(modEventBus);
        SlashBladeDataComponents.COMPONENTS.register(modEventBus);
        SlashBladeCriteriaTriggerRegistry.CRITERION_TRIGGERS.register(modEventBus);
        
        if (FMLEnvironment.dist == Dist.CLIENT && LoadingModList.get().getModFileById("jei") != null) {
            modEventBus.addListener(JEICompat::registerClientReloadListener);
        }
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        
        NeoForge.EVENT_BUS.addListener(KnockBackHandler::onLivingKnockBack);
        
        Guard.getInstance().register();
        
        NeoForge.EVENT_BUS.register(new StunManager());
        
        RefineHandler.getInstance().register();
        AnvilRarityHandler.getInstance().register();
        KillCounter.getInstance().register();
        RankPointHandler.getInstance().register();
        AllowFlightOverrwrite.getInstance().register();
        BladeMotionEventBroadcaster.getInstance().register();
        
        NeoForge.EVENT_BUS.addListener(TargetSelector::onInputChange);
        SummonedSwordArts.getInstance().register();
        SlayerStyleArts.getInstance().register();
        Untouchable.getInstance().register();
        EnemyStep.getInstance().register();
        KickJump.getInstance().register();
        SuperSlashArts.getInstance().register();
        
        ComboCommands.initDefaultStandByCommands();
        
    }
    
    public static Registry<SlashBladeDefinition> getSlashBladeDefinitionRegistry(Level level) {
        if (level.isClientSide()) {
            return BladeModelManager.getClientSlashBladeRegistry();
        }
        return level.registryAccess().registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }
    
    public static HolderLookup.RegistryLookup<SlashBladeDefinition> getSlashBladeDefinitionRegistry(HolderLookup.Provider access) {
        return access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }
}
