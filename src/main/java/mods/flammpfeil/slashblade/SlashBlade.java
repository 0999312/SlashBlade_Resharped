package mods.flammpfeil.slashblade;

import com.google.common.base.CaseFormat;
import mods.flammpfeil.slashblade.ability.*;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.client.renderer.entity.*;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.entity.*;
import mods.flammpfeil.slashblade.event.*;
import mods.flammpfeil.slashblade.item.BladeStandItem;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDetune;
import mods.flammpfeil.slashblade.item.ItemTierSlashBlade;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.recipe.RecipeSerializerRegistry;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.ModAttributes;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboCommands;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static mods.flammpfeil.slashblade.SlashBladeConfig.TRAPEZOHEDRON_MAX_REFINE;

@Mod(SlashBlade.MODID)
public class SlashBlade {
    public static final String MODID = "slashblade";

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(SlashBlade.MODID, path);
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public SlashBlade() {
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SlashBladeConfig.COMMON_CONFIG);

        modEventBus.addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        NetworkManager.register();

        ComboStateRegistry.COMBO_STATE.register(modEventBus);
        SlashArtsRegistry.SLASH_ARTS.register(modEventBus);
        SlashBladeCreativeGroup.CREATIVE_MODE_TABS.register(modEventBus);
        RecipeSerializerRegistry.RECIPE_SERIALIZER.register(modEventBus);
        SpecialEffectsRegistry.SPECIAL_EFFECT.register(modEventBus);


    }

    private void setup(final FMLCommonSetupEvent event) {

        MinecraftForge.EVENT_BUS.addListener(KnockBackHandler::onLivingKnockBack);

        FallHandler.getInstance().register();
        LockOnManager.getInstance().register();
        Guard.getInstance().register();

        MinecraftForge.EVENT_BUS.register(new CapabilityAttachHandler());
        MinecraftForge.EVENT_BUS.register(new StunManager());

        RefineHandler.getInstance().register();
        KillCounter.getInstance().register();
        RankPointHandler.getInstance().register();
        AllowFlightOverrwrite.getInstance().register();
        BlockPickCanceller.getInstance().register();
        BladeMotionEventBroadcaster.getInstance().register();

        MinecraftForge.EVENT_BUS.addListener(TargetSelector::onInputChange);
        SummonedSwordArts.getInstance().register();
        SlayerStyleArts.getInstance().register();
        Untouchable.getInstance().register();
        EnemyStep.getInstance().register();
        KickJump.getInstance().register();
        SuperSlashArts.getInstance().register();

        ComboCommands.initDefaultStandByCommands();
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        public static final ResourceLocation BladeItemEntityLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(BladeItemEntity.class));
        public static EntityType<BladeItemEntity> BladeItem;

        public static final ResourceLocation BladeStandEntityLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(BladeStandEntity.class));
        public static EntityType<BladeStandEntity> BladeStand;

        public static final ResourceLocation SummonedSwordLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityAbstractSummonedSword.class));
        public static EntityType<EntityAbstractSummonedSword> SummonedSword;
        public static final ResourceLocation SpiralSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntitySpiralSwords.class));
        public static EntityType<EntitySpiralSwords> SpiralSwords;

        public static final ResourceLocation StormSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityStormSwords.class));
        public static EntityType<EntityStormSwords> StormSwords;
        public static final ResourceLocation BlisteringSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityBlisteringSwords.class));
        public static EntityType<EntityBlisteringSwords> BlisteringSwords;
        public static final ResourceLocation HeavyRainSwordsLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityHeavyRainSwords.class));
        public static EntityType<EntityHeavyRainSwords> HeavyRainSwords;

        public static final ResourceLocation JudgementCutLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityJudgementCut.class));
        public static EntityType<EntityJudgementCut> JudgementCut;

        public static final ResourceLocation SlashEffectLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntitySlashEffect.class));
        public static EntityType<EntitySlashEffect> SlashEffect;

        public static final ResourceLocation DriveLoc = new ResourceLocation(SlashBlade.MODID,
                classToString(EntityDrive.class));
        public static EntityType<EntityDrive> Drive;


        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {

                helper.register(new ResourceLocation(MODID, "slashblade_wood"),
                        (ItemSlashBladeDetune) (new ItemSlashBladeDetune(new ItemTierSlashBlade(60, 2F), 2, 0.0F,
                                (new Item.Properties()))).setDestructable()
                                        .setTexture(SlashBlade.prefix("model/wood.png")));

                helper.register(new ResourceLocation(MODID, "slashblade_bamboo"),
                        (ItemSlashBladeDetune) (new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 3F), 3, 0.0F,
                                (new Item.Properties()))).setDestructable()
                                        .setTexture(SlashBlade.prefix("model/bamboo.png")));

                helper.register(new ResourceLocation(MODID, "slashblade_silverbamboo"),
                        (ItemSlashBladeDetune) (new ItemSlashBladeDetune(new ItemTierSlashBlade(40, 3F), 3, 0.0F,
                                (new Item.Properties()))).setTexture(SlashBlade.prefix("model/silverbamboo.png")));

                helper.register(new ResourceLocation(MODID, "slashblade_white"),
                        (ItemSlashBladeDetune) (new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 4F), 4, 0.0F,
                                (new Item.Properties()))).setTexture(SlashBlade.prefix("model/white.png")));

                helper.register(new ResourceLocation(MODID, "slashblade"),
                        new ItemSlashBlade(new ItemTierSlashBlade(40, 4F), 4, 0.0F, (new Item.Properties())));

                helper.register(new ResourceLocation(MODID, "proudsoul"), new Item((new Item.Properties())) {
                    @Override
                    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {

                        if (entity instanceof BladeItemEntity)
                            return false;

                        CompoundTag tag = entity.serializeNBT();
                        tag.putInt("Health", 50);
                        entity.deserializeNBT(tag);

                        if (entity.isCurrentlyGlowing()) {
                            entity.setDeltaMovement(
                                    entity.getDeltaMovement().multiply(0.8, 0.0, 0.8).add(0.0D, +0.04D, 0.0D));
                        } else if (entity.isOnFire()) {
                            entity.setDeltaMovement(
                                    entity.getDeltaMovement().multiply(0.8, 0.5, 0.8).add(0.0D, +0.04D, 0.0D));
                        }

                        return false;
                    }
                    
                    

                    @Override
                    public boolean isFoil(ItemStack stack) {
                        return true;// super.hasEffect(stack);
                    }

                    @Override
                    public int getEnchantmentValue(ItemStack stack) {
                        return 50;
                    }
                });

                helper.register(new ResourceLocation(MODID, "proudsoul_ingot"), new Item((new Item.Properties())) {
                    @Override
                    public boolean isFoil(ItemStack stack) {
                        return true;// super.hasEffect(stack);
                    }

                    @Override
                    public int getEnchantmentValue(ItemStack stack) {
                        return 100;
                    }
                });

                helper.register(new ResourceLocation(MODID, "proudsoul_tiny"), new Item((new Item.Properties())) {
                    @Override
                    public boolean isFoil(ItemStack stack) {
                        return true;// super.hasEffect(stack);
                    }

                    @Override
                    public int getEnchantmentValue(ItemStack stack) {
                        return 10;
                    }
                });

                helper.register(new ResourceLocation(MODID, "proudsoul_sphere"),
                        new Item((new Item.Properties()).rarity(Rarity.UNCOMMON)) {
                            @Override
                            public boolean isFoil(ItemStack stack) {
                                return true;// super.hasEffect(stack);
                            }

                            @Override
                            public int getEnchantmentValue(ItemStack stack) {
                                return 150;
                            }

                            @Override
                            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag)
                            {
                                if (stack.getTag() != null)
                                {
                                    CompoundTag tag = stack.getTag();
                                    if (tag.contains("SpecialAttackType"))
                                    {
                                        ResourceLocation SA = new ResourceLocation(tag.getString("SpecialAttackType"));
                                        if (SlashArtsRegistry.REGISTRY.get().containsKey(SA) && !SlashArtsRegistry.REGISTRY.get().getValue(SA).equals(SlashArtsRegistry.NONE.get()))
                                        {
                                            components.add(Component.translatable("slashblade.tooltip.slash_art", SlashArtsRegistry.REGISTRY.get().getValue(SA).getDescription()).withStyle(ChatFormatting.GRAY));
                                        }
                                    }
                                }
                                super.appendHoverText(stack, level, components, flag);
                            }
                        });

                helper.register(new ResourceLocation(MODID, "proudsoul_crystal"),
                        new Item((new Item.Properties()).rarity(Rarity.RARE)) {
                            @Override
                            public boolean isFoil(ItemStack stack) {
                                return true;// super.hasEffect(stack);
                            }

                            @Override
                            public int getEnchantmentValue(ItemStack stack) {
                                return 200;
                            }
                            
                            @Override
                            @OnlyIn(Dist.CLIENT)
                            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag)
                            {
                                if (stack.getTag() != null)
                                {
                                    CompoundTag tag = stack.getTag();
                                    if (tag.contains("SpecialEffectType"))
                                    {
                                    	Minecraft mcinstance = Minecraft.getInstance();
                                		Player player = mcinstance.player;
                                        ResourceLocation se = new ResourceLocation(tag.getString("SpecialEffectType"));
                                        if (SpecialEffectsRegistry.REGISTRY.get().containsKey(se))
                                        {
                                        	components.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
                                					Component.literal(String.valueOf(SpecialEffect.getRequestLevel(se)))
                                							.withStyle(SpecialEffect.isEffective(se, player.experienceLevel) ? ChatFormatting.RED
                                									: ChatFormatting.DARK_GRAY))
                                					.withStyle(ChatFormatting.GRAY));
                                        }
                                    }
                                }
                                super.appendHoverText(stack, level, components, flag);
                            }
                        });

                helper.register(new ResourceLocation(MODID, "proudsoul_trapezohedron"),
                        new Item((new Item.Properties()).rarity(Rarity.EPIC)) {
                            @Override
                            public boolean isFoil(ItemStack stack) {
                                return true;// super.hasEffect(stack);
                            }

                            @Override
                            public int getEnchantmentValue(ItemStack stack) {
                                return TRAPEZOHEDRON_MAX_REFINE.get();
                            }
                        });

                helper.register(new ResourceLocation(MODID, "bladestand_1"),
                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
                helper.register(new ResourceLocation(MODID, "bladestand_2"),
                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
                helper.register(new ResourceLocation(MODID, "bladestand_v"),
                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
                helper.register(new ResourceLocation(MODID, "bladestand_s"),
                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
                helper.register(new ResourceLocation(MODID, "bladestand_1w"),
                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON), true));
                helper.register(new ResourceLocation(MODID, "bladestand_2w"),
                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON), true));
            });

            event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
                {
                    EntityType<EntityAbstractSummonedSword> entity = SummonedSword = EntityType.Builder
                            .of(EntityAbstractSummonedSword::new, MobCategory.MISC).sized(0.5F, 0.5F)
                            .setTrackingRange(4).setUpdateInterval(20)
                            .setCustomClientFactory(EntityAbstractSummonedSword::createInstance)
                            .build(SummonedSwordLoc.toString());
                    helper.register(SummonedSwordLoc, entity);
                }

                {
                    EntityType<EntityStormSwords> entity = StormSwords = EntityType.Builder
                            .of(EntityStormSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityStormSwords::createInstance)
                            .build(StormSwordsLoc.toString());
                    helper.register(StormSwordsLoc, entity);
                }

                {
                    EntityType<EntitySpiralSwords> entity = SpiralSwords = EntityType.Builder
                            .of(EntitySpiralSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntitySpiralSwords::createInstance)
                            .build(SpiralSwordsLoc.toString());
                    helper.register(SpiralSwordsLoc, entity);
                }

                {
                    EntityType<EntityBlisteringSwords> entity = BlisteringSwords = EntityType.Builder
                            .of(EntityBlisteringSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityBlisteringSwords::createInstance)
                            .build(BlisteringSwordsLoc.toString());
                    helper.register(BlisteringSwordsLoc, entity);
                }

                {
                    EntityType<EntityHeavyRainSwords> entity = HeavyRainSwords = EntityType.Builder
                            .of(EntityHeavyRainSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityHeavyRainSwords::createInstance)
                            .build(HeavyRainSwordsLoc.toString());
                    helper.register(HeavyRainSwordsLoc, entity);
                }

                {
                    EntityType<EntityJudgementCut> entity = JudgementCut = EntityType.Builder
                            .of(EntityJudgementCut::new, MobCategory.MISC).sized(2.5F, 2.5F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntityJudgementCut::createInstance)
                            .build(JudgementCutLoc.toString());
                    helper.register(JudgementCutLoc, entity);
                }

                {
                    EntityType<BladeItemEntity> entity = BladeItem = EntityType.Builder
                            .of(BladeItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(BladeItemEntity::createInstanceFromPacket)
                            .build(BladeItemEntityLoc.toString());
                    helper.register(BladeItemEntityLoc, entity);
                }

                {
                    EntityType<BladeStandEntity> entity = BladeStand = EntityType.Builder
                            .of(BladeStandEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(10)
                            .setUpdateInterval(20).setShouldReceiveVelocityUpdates(false)
                            .setCustomClientFactory(BladeStandEntity::createInstance)
                            .build(BladeStandEntityLoc.toString());
                    helper.register(BladeStandEntityLoc, entity);
                }

                {
                    EntityType<EntitySlashEffect> entity = SlashEffect = EntityType.Builder
                            .of(EntitySlashEffect::new, MobCategory.MISC).sized(3.0F, 3.0F).setTrackingRange(4)
                            .setUpdateInterval(20).setCustomClientFactory(EntitySlashEffect::createInstance)
                            .build(SlashEffectLoc.toString());
                    helper.register(SlashEffectLoc, entity);
                }

                {
                    EntityType<EntityDrive> entity = Drive = EntityType.Builder.of(EntityDrive::new, MobCategory.MISC)
                            .sized(3.0F, 3.0F).setTrackingRange(4).setUpdateInterval(20)
                            .setCustomClientFactory(EntityDrive::createInstance).build(DriveLoc.toString());
                    helper.register(DriveLoc, entity);
                }

            });

            event.register(ForgeRegistries.Keys.STAT_TYPES, helper -> {
                SWORD_SUMMONED = registerCustomStat("sword_summoned");
            });

        }

        private static String classToString(Class<? extends Entity> entityClass) {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName())
                    .replace("entity_", "");
        }

        @SubscribeEvent
        public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(RegistryEvents.SummonedSword, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.StormSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.SpiralSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.BlisteringSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.HeavyRainSwords, SummonedSwordRenderer::new);
            event.registerEntityRenderer(RegistryEvents.JudgementCut, JudgementCutRenderer::new);
            event.registerEntityRenderer(RegistryEvents.BladeItem, BladeItemEntityRenderer::new);
            event.registerEntityRenderer(RegistryEvents.BladeStand, BladeStandEntityRenderer::new);
            event.registerEntityRenderer(RegistryEvents.SlashEffect, SlashEffectRenderer::new);
            event.registerEntityRenderer(RegistryEvents.Drive, DriveRenderer::new);

        }

        @SubscribeEvent
        public static void onRegisterCapability(final RegisterCapabilitiesEvent event) {
            CapabilitySlashBlade.register(event);
            CapabilityMobEffect.register(event);
            CapabilityInputState.register(event);
            CapabilityConcentrationRank.register(event);
        }

        @SubscribeEvent
        public static void onEntityAttributeModificationEvent(final EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER, ModAttributes.SLASHBLADE_DAMAGE.get());
        }

        public static ResourceLocation SWORD_SUMMONED;

        private static ResourceLocation registerCustomStat(String name) {
            ResourceLocation resourcelocation = new ResourceLocation(MODID, name);
            Registry.register(BuiltInRegistries.CUSTOM_STAT, name, resourcelocation);
            Stats.CUSTOM.get(resourcelocation, StatFormatter.DEFAULT);
            return resourcelocation;
        }

        /**
         * /scoreboard objectives add stat minecraft.custom:slashblade.sword_summoned
         * /scoreboard objectives setdisplay sidebar stat
         */
    }

    public static Registry<SlashBladeDefinition> getSlashBladeDefinitionRegistry(Level level) {
        if (level.isClientSide())
            return BladeModelManager.getClientSlashBladeRegistry();
        return level.registryAccess().registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }

    public static HolderLookup.RegistryLookup<SlashBladeDefinition> getSlashBladeDefinitionRegistry(HolderLookup.Provider access) {
        return access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }
}
