package mods.flammpfeil.slashblade.client;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.SlashBladeTEISR;
import mods.flammpfeil.slashblade.client.renderer.gui.RankRenderer;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.compat.playerAnim.PlayerAnimationOverrider;
import mods.flammpfeil.slashblade.event.client.BladeRuntimeSyncer;
import mods.flammpfeil.slashblade.event.client.LayerSlashBladeInitEvent;
import mods.flammpfeil.slashblade.event.client.SneakingMotionCanceller;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.event.handler.BlockPickCanceller;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = SlashBlade.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        SlashBlade.MOD_EVENT_BUS.post(new LayerSlashBladeInitEvent());
        
        SneakingMotionCanceller.getInstance().register();
        BladeRuntimeSyncer.getInstance().register();
        
        if (ModList.get().isLoaded("playeranimator")) {
            PlayerAnimationOverrider.getInstance().register();
        } else {
            UserPoseOverrider.getInstance().register();
        }
        
        RankRenderer.getInstance().register();
        BlockPickCanceller.getInstance().register();
        
        event.enqueueWork(() -> {
            ItemProperties.register(SlashBladeItems.SLASHBLADE.get(), ResourceLocation.parse("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });
            
            ItemProperties.register(SlashBladeItems.SLASHBLADE_BAMBOO.get(), ResourceLocation.parse("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });
            
            ItemProperties.register(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), ResourceLocation.parse("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });
            
            ItemProperties.register(SlashBladeItems.SLASHBLADE_WHITE.get(), ResourceLocation.parse("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });
            
            ItemProperties.register(SlashBladeItems.SLASHBLADE_WOOD.get(), ResourceLocation.parse("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });
        });
        
    }
    
    @SubscribeEvent
    public static void onCreativeTagBuilding(BuildCreativeModeTabContentsEvent event) {
        var registries = event.getParameters().holders();
        SlashBlade.getSlashBladeDefinitionRegistry(registries)
            .listElements()
            .sorted(SlashBladeDefinition.COMPARATOR)
            .forEach(entry -> {
                if (!event.getTabKey().location().equals(entry.value().getCreativeGroup())) {
                    return;
                }
                
                var blade = entry.value().getBlade(registries);
                if (!blade.isEmpty()) {
                    event.accept(blade);
                }
            });
    }
    
    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SlashBladeKeyMappings.KEY_SPECIAL_MOVE);
        event.register(SlashBladeKeyMappings.KEY_SUMMON_BLADE);
    }
    
    @SubscribeEvent
    public static void onTextureStitched(TextureAtlasStitchedEvent event) {
        BladeMotionManager.getInstance().reload();
    }
    
    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        var extensions = new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer renderer = new SlashBladeTEISR(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        };
        event.registerItem(extensions, SlashBladeItems.SLASHBLADE.get(),
            SlashBladeItems.SLASHBLADE_WOOD.get(), SlashBladeItems.SLASHBLADE_BAMBOO.get(),
            SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), SlashBladeItems.SLASHBLADE_WHITE.get());
    }
    
    @SubscribeEvent
    public static void Baked(final ModelEvent.ModifyBakingResult event) {
        bakeBlade(SlashBladeItems.SLASHBLADE.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_WHITE.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_WOOD.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_BAMBOO.get(), event);
    }
    
    public static void bakeBlade(Item blade, final ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation loc = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(blade));
        var bakedModel = event.getModels().get(loc);
        if (bakedModel != null) {
            event.getModels().put(loc, new BladeModel(bakedModel, event.getModelBakery()));
        }
    }
    
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        addPlayerLayer(event, PlayerSkin.Model.WIDE);
        addPlayerLayer(event, PlayerSkin.Model.SLIM);
        
        for (EntityType<?> entityType : event.getEntityTypes()) {
            addEntityLayer(event, event.getRenderer(entityType));
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addPlayerLayer(EntityRenderersEvent.AddLayers evt, PlayerSkin.Model skin) {
        var renderer = evt.getSkin(skin);
        
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new LayerMainBlade<>(livingRenderer));
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addEntityLayer(EntityRenderersEvent.AddLayers evt, @Nullable EntityRenderer<?> renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new LayerMainBlade<>(livingRenderer));
        }
    }
    
}
