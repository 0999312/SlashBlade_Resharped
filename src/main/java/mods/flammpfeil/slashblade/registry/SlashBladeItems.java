package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.*;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Objects;

import static mods.flammpfeil.slashblade.SlashBladeConfig.TRAPEZOHEDRON_MAX_REFINE;

public class SlashBladeItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SlashBlade.MODID);
    
    public static final DeferredHolder<Item, Item> PROUDSOUL = ITEMS.register("proudsoul", () ->
        new ItemProudSoul(new Item.Properties()) {
            @Override
            public int getEnchantmentValue(ItemStack stack) {
                return 50;
            }
        });
    public static final DeferredHolder<Item, Item> PROUDSOUL_INGOT = ITEMS.register("proudsoul_ingot", () ->
        new ItemProudSoul((new Item.Properties())) {
            @Override
            public int getEnchantmentValue(ItemStack stack) {
                return 100;
            }
        });
    public static final DeferredHolder<Item, Item> PROUDSOUL_TINY = ITEMS.register("proudsoul_tiny", () ->
        new ItemProudSoul((new Item.Properties())) {
            @Override
            public int getEnchantmentValue(ItemStack stack) {
                return 10;
            }
        });
    public static final DeferredHolder<Item, Item> PROUDSOUL_SPHERE = ITEMS.register("proudsoul_sphere", () ->
        new ItemProudSoul((new Item.Properties()).rarity(Rarity.UNCOMMON)) {
            @Override
            public int getEnchantmentValue(ItemStack stack) {
                return 150;
            }
            
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
                CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if (tag.contains("SpecialAttackType")) {
                    ResourceLocation SA = ResourceLocation.tryParse(tag.getString("SpecialAttackType"));
                    if (SA == null) {
                        return;
                    }
                    SlashArts slashArts = SlashArtsRegistry.REGISTRY.get(SA);
                    if (slashArts != null && !Objects.equals(slashArts, SlashArtsRegistry.NONE.get())) {
                        components.add(Component.translatable("slashblade.tooltip.slash_art", slashArts.getDescription()).withStyle(ChatFormatting.GRAY));
                    }
                }
                super.appendHoverText(stack, context, components, flag);
            }
        });
    public static final DeferredHolder<Item, Item> PROUDSOUL_CRYSTAL = ITEMS.register("proudsoul_crystal", () ->
        new ItemProudSoul((new Item.Properties()).rarity(Rarity.RARE)) {
            @Override
            public int getEnchantmentValue(ItemStack stack) {
                return 200;
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
                CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if (tag.contains("SpecialEffectType")) {
                    Minecraft mcinstance = Minecraft.getInstance();
                    Player player = mcinstance.player;
                    ResourceLocation se = ResourceLocation.tryParse(tag.getString("SpecialEffectType"));
                    if (se == null) {
                        return;
                    }
                    if (SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                        if (player != null) {
                            components.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
                                    Component.literal(String.valueOf(SpecialEffect.getRequestLevel(se)))
                                        .withStyle(SpecialEffect.isEffective(se, player) ? ChatFormatting.RED
                                            : ChatFormatting.DARK_GRAY))
                                .withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
                super.appendHoverText(stack, context, components, flag);
            }
        });
    public static final DeferredHolder<Item, Item> PROUDSOUL_TRAPEZOHEDRON = ITEMS.register("proudsoul_trapezohedron", () ->
        new ItemProudSoul((new Item.Properties()).rarity(Rarity.EPIC)) {
            @Override
            public int getEnchantmentValue(ItemStack stack) {
                return TRAPEZOHEDRON_MAX_REFINE.get();
            }
        });
    
    public static final DeferredHolder<Item, Item> BLADESTAND_1 = ITEMS.register("bladestand_1", () ->
        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> BLADESTAND_2 = ITEMS.register("bladestand_2", () ->
        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> BLADESTAND_V = ITEMS.register("bladestand_v", () ->
        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> BLADESTAND_S = ITEMS.register("bladestand_s", () ->
        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> BLADESTAND_1_W = ITEMS.register("bladestand_1w", () ->
        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON), true));
    public static final DeferredHolder<Item, Item> BLADESTAND_2_W = ITEMS.register("bladestand_2w", () ->
        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON), true));
    
    public static final DeferredHolder<Item, Item> SLASHBLADE_WOOD = ITEMS.register("slashblade_wood", () ->
        new ItemSlashBladeDetune(new ItemTierSlashBlade(60, 2F), 2, 0.0F,
            new Item.Properties()).setDestructable()
            .setTexture(SlashBlade.prefix("model/wood.png")));
    public static final DeferredHolder<Item, Item> SLASHBLADE_BAMBOO = ITEMS.register("slashblade_bamboo", () ->
        new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 3F), 3, 0.0F,
            new Item.Properties()).setDestructable()
            .setTexture(SlashBlade.prefix("model/bamboo.png")));
    public static final DeferredHolder<Item, Item> SLASHBLADE_SILVERBAMBOO = ITEMS.register("slashblade_silverbamboo", () ->
        new ItemSlashBladeDetune(new ItemTierSlashBlade(40, 3F), 3, 0.0F,
            new Item.Properties()).setTexture(SlashBlade.prefix("model/silverbamboo.png")));
    public static final DeferredHolder<Item, Item> SLASHBLADE_WHITE = ITEMS.register("slashblade_white", () ->
        new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 4F), 4, 0.0F,
            new Item.Properties()).setTexture(SlashBlade.prefix("model/white.png")));
    public static final DeferredHolder<Item, Item> SLASHBLADE = ITEMS.register("slashblade", () ->
        new ItemSlashBlade(new ItemTierSlashBlade(40, 4F), 4, 0.0F, new Item.Properties()));
}
