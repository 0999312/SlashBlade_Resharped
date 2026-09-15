package mods.flammpfeil.slashblade.item;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import mods.flammpfeil.slashblade.RegistryEvents;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class ItemSlashBlade extends SwordItem {
    protected static final ResourceLocation PLAYER_REACH_AMPLIFIER = ResourceLocation.fromNamespaceAndPath(
        SlashBlade.MODID, "mainhand_reach");
    
    public static final Set<ResourceKey<Enchantment>> EX_ENCHANTMENTS = Set.of(
        Enchantments.SOUL_SPEED,
        Enchantments.POWER,
        Enchantments.FEATHER_FALLING,
        Enchantments.FIRE_PROTECTION,
        Enchantments.THORNS);
    
    protected final Tier tier;
    protected final int attackDamageIn;
    protected final float attackSpeedIn;
    
    public ItemSlashBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, builder);
        this.tier = tier;
        this.attackDamageIn = attackDamageIn;
        this.attackSpeedIn = attackSpeedIn;
    }
    
    private static boolean isExtraEnchantment(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(EX_ENCHANTMENTS::contains).orElse(false);
    }
    
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        
        return isExtraEnchantment(enchantment)
            || super.supportsEnchantment(stack, enchantment);
    }
    
    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (isExtraEnchantment(enchantment)) {
            return true;
        }
        
        if (stack.is(Items.BOOK)) {
            return true;
        }
        
        Optional<HolderSet<Item>> primaryItems = enchantment.value().definition().primaryItems();
        return this.supportsEnchantment(stack, enchantment)
            && (primaryItems.isEmpty() || stack.is(primaryItems.get()));
    }
    
    @Override
    public String getCreatorModId(ItemStack itemStack) {
        return this.getBladeId(itemStack).getNamespace();
    }
    
    public static void updateRarity(ItemStack stack) {
        var swordType = SwordType.from(stack);
        if (swordType.contains(SwordType.BEWITCHED)) {
            stack.set(DataComponents.RARITY, Rarity.EPIC);
        } else if (swordType.contains(SwordType.ENCHANTED)) {
            stack.set(DataComponents.RARITY, Rarity.RARE);
        } else {
            stack.remove(DataComponents.RARITY);
        }
    }
    
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        var builder = ItemAttributeModifiers.builder();
        var state = BladeStateAccess.of(stack).orElse(null);
        
        if (state == null) {
            builder.add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                    this.attackDamageIn + this.tier.getAttackDamageBonus(),
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, this.attackSpeedIn,
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
            return builder.build();
        }
        
        var swordType = SwordType.from(stack);
        float baseAttackModifier = state.getBaseAttackModifier();
        int refine = state.getRefine();
        
        float attackAmplifier;
        if (state.isBroken()) {
            attackAmplifier = -0.5F - baseAttackModifier;
        } else {
            float refineFactor = swordType.contains(SwordType.FIERCEREDGE) ? 0.1F : 0.05F;
            attackAmplifier = (1.0F - (1.0F / (1.0F + (refineFactor * refine)))) * baseAttackModifier;
        }
        
        double damage = (double) baseAttackModifier + attackAmplifier - 1F;
        var event = new SlashBladeEvent.UpdateAttackEvent(stack, state, damage);
        NeoForge.EVENT_BUS.post(event);
        
        builder.add(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, event.getNewDamage(),
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED,
            new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, this.attackSpeedIn,
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(PLAYER_REACH_AMPLIFIER,
                state.isBroken() ? ReachModifier.BrokendReach() : ReachModifier.BladeReach(),
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (handIn == InteractionHand.OFF_HAND && !(playerIn.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
            return InteractionResultHolder.pass(itemstack);
        }
        boolean result = BladeStateAccess.of(itemstack).map((state) -> {
            
            playerIn.getData(CapabilityInputState.INPUT_STATE.get()).getCommands().add(InputCommand.R_CLICK);
            
            ResourceLocation combo = state.progressCombo(playerIn);
            
            playerIn.getData(CapabilityInputState.INPUT_STATE.get()).getCommands().remove(InputCommand.R_CLICK);
            
            if (!Objects.equals(combo, ComboStateRegistry.NONE.getId())) {
                playerIn.swing(handIn);
            }
            
            return true;
        }).orElse(false);
        
        playerIn.startUsingItem(handIn);
        return new InteractionResultHolder<>(result ? InteractionResult.SUCCESS : InteractionResult.FAIL, itemstack);
    }
    
    @Override
    public boolean onLeftClickEntity(ItemStack itemstack, Player playerIn, Entity entity) {
        Optional<ISlashBladeState> stateHolder = BladeStateAccess.of(itemstack)
            .filter((state) -> !state.onClick());
        
        stateHolder.ifPresent((state) -> {
            playerIn.getData(CapabilityInputState.INPUT_STATE.get()).getCommands().add(InputCommand.L_CLICK);
            
            state.progressCombo(playerIn);
            
            playerIn.getData(CapabilityInputState.INPUT_STATE.get()).getCommands().remove(InputCommand.L_CLICK);
        });
        
        return stateHolder.isPresent();
    }
    
    public static final String BREAK_ACTION_TIMEOUT = "BreakActionTimeout";
    
    @Override
    public void setDamage(ItemStack stack, int damage) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage < 0) {
            return;
        }
        var state = BladeStateAccess.of(stack).orElseThrow();
        if (state.isBroken()) {
            if (damage <= 0 && !state.isSealed()) {
                state.setBroken(false);
            } else if (maxDamage < damage) {
                damage = Math.min(damage, maxDamage - 1);
            }
        }
        state.setDamage(damage);
    }
    
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        if (stack.getMaxDamage() <= 0) {
            return 0;
        }
        
        if (amount <= 0) {
            return 0;
        }
        
        var cap = BladeStateAccess.of(stack).orElseThrow();
        boolean current = cap.isBroken();
        
        if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
            amount = 0;
            stack.setDamageValue(stack.getMaxDamage() - 1);
            cap.setBroken(!NeoForge.EVENT_BUS.post(new SlashBladeEvent.BreakEvent(stack, cap)).isCanceled());
        }
        
        if (current != cap.isBroken()) {
            onBroken.accept(stack.getItem());
            if (entity instanceof ServerPlayer player) {
                CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            }
            
            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_BROKEN.get(stack.getItem()));
            }
        }
        
        if (cap.isBroken() && this.isDestructable(stack)) {
            stack.shrink(1);
        }
        
        return amount;
    }
    
    public static Consumer<LivingEntity> getOnBroken(ItemStack stack) {
        return (user) -> {
            
            var state = BladeStateAccess.of(stack).orElseThrow();
            if (stack.isEnchanted()) {
                int count = state.getProudSoulCount() >= SlashBladeConfig.MAX_ENCHANTED_PROUDSOUL_DROP.get() * 100 ?
                    SlashBladeConfig.MAX_ENCHANTED_PROUDSOUL_DROP.get() : Math.max(1, state.getProudSoulCount() / 100);
                
                List<Holder.Reference<Enchantment>> enchantments = user.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .listElements()
                    .filter(stack::supportsEnchantment)
                    .filter(enchantment -> !SlashBladeConfig.NON_DROPPABLE_ENCHANTMENT.get()
                        .contains(enchantment.key().location().toString()))
                    .toList();
                if (enchantments.isEmpty()) {
                    return;
                }
                
                for (int i = 0; i < count; i++) {
                    ItemStack enchanted_soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                    Holder<Enchantment> enchant = enchantments.get(user.getRandom().nextInt(0, enchantments.size()));
                    if (enchant != null) {
                        enchanted_soul.enchant(enchant, 1);
                        ItemEntity itemEntity = new ItemEntity(user.level(), user.getX(), user.getY(), user.getZ(),
                            enchanted_soul);
                        itemEntity.setDefaultPickUpDelay();
                        user.level().addFreshEntity(itemEntity);
                    }
                    state.setProudSoulCount(state.getProudSoulCount() - 100);
                }
            }
            
            ItemStack soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
            
            int count = state.getProudSoulCount() >= SlashBladeConfig.MAX_PROUDSOUL_DROP.get() * 100 ?
                SlashBladeConfig.MAX_PROUDSOUL_DROP.get() : Math.max(1, state.getProudSoulCount() / 100);
            
            soul.setCount(count);
            
            state.setProudSoulCount(state.getProudSoulCount() - (count * 100));
            
            ItemEntity itementity = new ItemEntity(user.level(), user.getX(), user.getY(), user.getZ(), soul);
            BladeItemEntity e = new BladeItemEntity(RegistryEvents.BladeItem, user.level()) {
                static final String isReleased = "isReleased";
                
                @Override
                public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource ds) {
                    
                    CompoundTag tag = this.getPersistentData();
                    
                    if (!tag.getBoolean(isReleased)) {
                        this.getPersistentData().putBoolean(isReleased, true);
                        
                        if (this.level() instanceof ServerLevel) {
                            Entity thrower = getOwner();
                            
                            if (thrower != null) {
                                thrower.getPersistentData().remove(BREAK_ACTION_TIMEOUT);
                            }
                        }
                    }
                    
                    return super.causeFallDamage(distance, damageMultiplier, ds);
                }
            };
            
            e.restoreFrom(itementity);
            e.init();
            e.push(0, 0.4, 0);
            
            e.setModel(state.getModel().orElse(DefaultResources.resourceDefaultModel));
            e.setTexture(state.getTexture().orElse(DefaultResources.resourceDefaultTexture));
            
            e.setPickUpDelay(20 * 2);
            e.setGlowingTag(true);
            
            e.setAirSupply(-1);
            
            e.setThrower(user);
            
            user.level().addFreshEntity(e);
            
            user.getPersistentData().putLong(BREAK_ACTION_TIMEOUT, user.level().getGameTime() + 20 * 5);
        };
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        
        BladeStateAccess.of(stack).ifPresent((state) -> {
            ResourceLocation loc = state.resolvCurrentComboState(attacker);
            ComboState cs = Objects.requireNonNullElse(ComboStateRegistry.REGISTRY.get(loc), ComboStateRegistry.NONE.get());
            
            if (NeoForge.EVENT_BUS.post(new SlashBladeEvent.HitEvent(stack, state, target, attacker)).isCanceled()) {
                return;
            }
            
            cs.hitEffect(target, attacker);
            if (attacker.level() instanceof ServerLevel serverLevel) {
                var blade = stack.copy();
                stack.hurtAndBreak(1, serverLevel, attacker, item -> {
                    attacker.onEquippedItemBroken(item, EquipmentSlot.MAINHAND);
                    ItemSlashBlade.getOnBroken(blade).accept(attacker);
                });
            }
            
        });
        
        return true;
    }
    
    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos,
                             LivingEntity entityLiving) {
        
        if (state.getDestroySpeed(worldIn, pos) != 0.0F) {
            BladeStateAccess.of(stack).ifPresent((s) -> {
                if (worldIn instanceof ServerLevel serverLevel) {
                    var blade = stack.copy();
                    stack.hurtAndBreak(1, serverLevel, entityLiving, item -> {
                        entityLiving.onEquippedItemBroken(item, EquipmentSlot.MAINHAND);
                        ItemSlashBlade.getOnBroken(blade).accept(entityLiving);
                    });
                }
            });
        }
        
        return true;
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        int elapsed = this.getUseDuration(stack, entityLiving) - timeLeft;
        
        if (!worldIn.isClientSide()) {
            
            BladeStateAccess.of(stack).ifPresent((state) -> {
                
                var swordType = SwordType.from(stack);
                if (state.isBroken() || state.isSealed() || !(swordType.contains(SwordType.ENCHANTED))) {
                    return;
                }
                
                ResourceLocation sa = state.doChargeAction(entityLiving, elapsed);
                boolean isCreative = false;
                // sa.tickAction(entityLiving);
                if (!Objects.equals(sa, ComboStateRegistry.NONE.getId())) {
                    if (entityLiving instanceof Player player) {
                        isCreative = player.getAbilities().instabuild;
                    }
                    if (!isCreative) {
                        var cost = state.getSlashArts().getProudSoulCost();
                        if (state.getProudSoulCount() >= cost) {
                            state.setProudSoulCount(state.getProudSoulCount() - cost);
                        } else {
                            if (worldIn instanceof ServerLevel serverLevel) {
                                var blade = stack.copy();
                                stack.hurtAndBreak(1, serverLevel, entityLiving, item -> {
                                    entityLiving.onEquippedItemBroken(item, EquipmentSlot.MAINHAND);
                                    ItemSlashBlade.getOnBroken(blade).accept(entityLiving);
                                });
                            }
                        }
                    }
                    entityLiving.swing(InteractionHand.MAIN_HAND);
                }
            });
        }
    }
    
    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        
        BladeStateAccess.of(stack).ifPresent((state) -> {
            
            Objects.requireNonNullElse(ComboStateRegistry.REGISTRY.get(state.getComboSeq()), ComboStateRegistry.NONE.get())
                .holdAction(player);
            int ticks = player.getTicksUsingItem();
            
            SlashBladeEvent.ChargeActionEvent event = new SlashBladeEvent.ChargeActionEvent(player, ticks, state);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return;
            }
            
            if (!player.level().isClientSide()) {
                
                int fullChargeTicks = state.getFullChargeTicks(player);
                if (0 < ticks) {
                    if (ticks == fullChargeTicks) {// state.getFullChargeTicks(player)){
                        Vec3 pos = player.getEyePosition(1.0f).add(player.getLookAngle());
                        ((ServerLevel) player.level()).sendParticles(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 7, 0.7,
                            0.7, 0.7, 0.02);
                    }
                }
            }
            
        });
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        
        BladeStateAccess.of(stack).ifPresent((state) -> {
            if (NeoForge.EVENT_BUS
                .post(new SlashBladeEvent.UpdateEvent(stack, state, worldIn, entityIn, itemSlot, isSelected)).isCanceled()) {
                return;
            }
            
            var swordType = SwordType.from(stack);
            
            if (!isSelected) {
                if (entityIn instanceof Player player) {
                    if (!SlashBladeConfig.SELF_REPAIR_ENABLE.get()) {
                        return;
                    }
                    MobEffectInstance effect = player.getEffect(MobEffects.HUNGER);
                    boolean hasHunger = effect != null && SlashBladeConfig.HUNGER_CAN_REPAIR.get();
                    if (swordType.contains(SwordType.BEWITCHED) || hasHunger) {
                        if (stack.getDamageValue() > 0 && player.getFoodData().getFoodLevel() > 0) {
                            int hungerAmplifier = hasHunger ? effect.getAmplifier() : 0;
                            int level = 1 + hungerAmplifier;
                            Boolean expCostFlag = SlashBladeConfig.SELF_REPAIR_COST_EXP.get();
                            int expCost = SlashBladeConfig.BEWITCHED_EXP_COST.get() * level;
                            
                            if (expCostFlag && player.experienceLevel < expCost) {
                                return;
                            }
                            
                            player.giveExperiencePoints(expCostFlag ? -expCost : 0);
                            player.causeFoodExhaustion(
                                SlashBladeConfig.BEWITCHED_HUNGER_EXHAUSTION.get().floatValue() * level);
                            stack.setDamageValue(stack.getDamageValue() - level);
                        }
                    }
                }
            }
            if (entityIn instanceof LivingEntity living) {
                entityIn.getData(CapabilityInputState.INPUT_STATE.get()).getScheduler().onTick(living);
                
                /*
                 * if(0.5f > state.getDamage()) state.setDamage(0.99f);
                 */
                ResourceLocation loc = state.resolvCurrentComboState(living);
                ComboState cs = Objects.requireNonNullElse(ComboStateRegistry.REGISTRY.get(loc), ComboStateRegistry.NONE.get());
                
                if (isInMainhand(stack, isSelected, living)) {
                    cs.tickAction(living);
                }
            }
        });
    }
    
    public static boolean isInMainhand(ItemStack stack, boolean isSelected, LivingEntity living) {
        return isSelected && ItemStack.isSameItemSameComponents(stack, living.getMainHandItem());
    }
    
    // damage ----------------------------------------------------------
    
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }
    
    @Override
    public String getDescriptionId(ItemStack stack) {
        return BladeStateAccess.of(stack).filter((s) -> !s.getTranslationKey().isBlank())
            .map(ISlashBladeState::getTranslationKey).orElseGet(() -> super.getDescriptionId(stack));
    }
    
    public ResourceLocation getBladeId(ItemStack stack) {
        return BladeStateAccess.of(stack).filter((s) -> !s.getTranslationKey().isBlank())
            .map((state) -> parseBladeID(state.getTranslationKey())).orElseGet(() -> BuiltInRegistries.ITEM.getKey(this));
    }
    
    @Nullable
    public static ResourceLocation parseBladeID(String key) {
        return ResourceLocation.tryParse(key.substring(5).replaceFirst("\\.", ":"));
    }
    
    public boolean isDestructable(ItemStack stack) {
        return BladeStateAccess.of(stack).map(ISlashBladeState::isDestructable).orElse(false);
    }
    
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        
        if (Ingredient.of(ItemTags.STONE_TOOL_MATERIALS).test(repair)) {
            return true;
        }
        
        /*
         * Tag<Item> tags = ItemTags.getCollection().get(new
         * ResourceLocation("slashblade","proudsouls"));
         *
         * if(tags != null){ boolean result = Ingredient.fromTag(tags).test(repair); }
         */
        
        // todo: repair custom material
        if (repair.is(SlashBladeItemTags.PROUD_SOULS)) {
            return true;
        }
        return super.isValidRepairItem(toRepair, repair);
    }
    
    RangeMap<Comparable<?>, Object> refineColor = ImmutableRangeMap.builder()
        .put(Range.lessThan(10), ChatFormatting.GRAY).put(Range.closedOpen(10, 50), ChatFormatting.YELLOW)
        .put(Range.closedOpen(50, 100), ChatFormatting.GREEN).put(Range.closedOpen(100, 150), ChatFormatting.AQUA)
        .put(Range.closedOpen(150, 200), ChatFormatting.BLUE).put(Range.atLeast(200), ChatFormatting.LIGHT_PURPLE)
        .build();
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        Level worldIn = context.level();
        BladeStateAccess.of(stack).ifPresent(s -> {
            this.appendSwordType(stack, worldIn, tooltip, flagIn); // √
            this.appendProudSoulCount(tooltip, stack);
            this.appendKillCount(tooltip, stack);
            this.appendSlashArt(stack, tooltip, s); // √
            this.appendRefineCount(tooltip, stack);
            this.appendSpecialEffects(tooltip, s); // √
        });
        
        super.appendHoverText(stack, context, tooltip, flagIn);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void appendSlashArt(ItemStack stack, List<Component> tooltip, ISlashBladeState s) {
        var swordType = SwordType.from(stack);
        if (swordType.contains(SwordType.BEWITCHED) && !swordType.contains(SwordType.SEALED)) {
            tooltip.add(Component.translatable("slashblade.tooltip.slash_art", s.getSlashArts().getDescription())
                .withStyle(ChatFormatting.GRAY));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public void appendRefineCount(List<Component> tooltip, ItemStack stack) {
        BladeStateAccess.of(stack).ifPresent(s -> {
            int refine = s.getRefine();
            if (refine > 0) {
                tooltip.add(Component.translatable("slashblade.tooltip.refine", refine)
                    .withStyle((ChatFormatting) refineColor.get(refine)));
            }
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public void appendProudSoulCount(List<Component> tooltip, ItemStack stack) {
        BladeStateAccess.of(stack).ifPresent(s -> {
            int proudsoul = s.getProudSoulCount();
            if (proudsoul > 0) {
                MutableComponent countComponent = Component.translatable("slashblade.tooltip.proud_soul", proudsoul)
                    .withStyle(ChatFormatting.GRAY);
                if (proudsoul > 10000) {
                    countComponent = countComponent.withStyle(ChatFormatting.DARK_PURPLE);
                }
                tooltip.add(countComponent);
            }
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public void appendKillCount(List<Component> tooltip, ItemStack stack) {
        BladeStateAccess.of(stack).ifPresent(s -> {
            int killCount = s.getKillCount();
            if (killCount > 0) {
                MutableComponent killCountComponent = Component.translatable("slashblade.tooltip.killcount", killCount)
                    .withStyle(ChatFormatting.GRAY);
                if (killCount > 1000) {
                    killCountComponent = killCountComponent.withStyle(ChatFormatting.DARK_PURPLE);
                }
                tooltip.add(killCountComponent);
            }
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public void appendSpecialEffects(List<Component> tooltip, ISlashBladeState s) {
        if (s.getSpecialEffects().isEmpty()) {
            return;
        }
        
        Minecraft mcinstance = Minecraft.getInstance();
        Player player = mcinstance.player;
        
        s.getSpecialEffects().forEach(se -> {
            
            boolean showingLevel = SpecialEffect.getRequestLevel(se) > 0;
            
            if (player != null) {
                tooltip.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
                        Component.literal(showingLevel ? String.valueOf(SpecialEffect.getRequestLevel(se)) : "")
                            .withStyle(SpecialEffect.isEffective(se, player) ? ChatFormatting.RED
                                : ChatFormatting.DARK_GRAY))
                    .withStyle(ChatFormatting.GRAY));
            }
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public void appendSwordType(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        var swordType = SwordType.from(stack);
        boolean goldenFlag = swordType.containsAll(List.of(SwordType.SOULEATER, SwordType.FIERCEREDGE));
        if (swordType.contains(SwordType.SEALED)) {
            return;
        }
        if (swordType.contains(SwordType.BEWITCHED)) {
            tooltip.add(
                Component.translatable("slashblade.sword_type.bewitched")
                    .withStyle(goldenFlag ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE));
        } else if (swordType.contains(SwordType.ENCHANTED)) {
            tooltip.add(Component.translatable("slashblade.sword_type.enchanted").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("slashblade.sword_type.noname").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }
    
    /**
     * 原来的方法替换掉落实体时无法Copy假物品实体相关的NBT，因为获取物品指令是先生成的物品实体再设置的假物品
     */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!(entity instanceof BladeItemEntity)) {
            Level world = entity.level();
            BladeItemEntity e = new BladeItemEntity(RegistryEvents.BladeItem, world);
            e.restoreFrom(entity);
            e.init();
            entity.discard();
            world.addFreshEntity(e);
        }
        return false;
    }
    
    @Override
    public int getEntityLifespan(ItemStack itemStack, Level world) {
        return super.getEntityLifespan(itemStack, world);// Short.MAX_VALUE;
    }
    
    public EnumSet<SwordType> getSwordType(EnumSet<SwordType> from, ItemStack itemStack, Optional<ISlashBladeState> state) {
        return from;
    }
}
