package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.*;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static mods.flammpfeil.slashblade.SlashBladeConfig.TRAPEZOHEDRON_MAX_REFINE;

public class SlashBladeItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SlashBlade.MODID);

    public static final RegistryObject<Item> PROUDSOUL = ITEMS.register("proudsoul", () ->
            new ItemProudSoul(new Item.Properties()) {
                @Override
                public int getEnchantmentValue(ItemStack stack) {
                    return 50;
                }
            });
    public static final RegistryObject<Item> PROUDSOUL_INGOT = ITEMS.register("proudsoul_ingot", () ->
            new ItemProudSoul((new Item.Properties())) {
                @Override
                public int getEnchantmentValue(ItemStack stack) {
                    return 100;
                }
            });
    public static final RegistryObject<Item> PROUDSOUL_TINY = ITEMS.register("proudsoul_tiny", () ->
            new ItemProudSoul((new Item.Properties())) {
                @Override
                public int getEnchantmentValue(ItemStack stack) {
                    return 10;
                }
            });
    public static final RegistryObject<Item> PROUDSOUL_SPHERE = ITEMS.register("proudsoul_sphere", () ->
            new ItemProudSoul((new Item.Properties()).rarity(Rarity.UNCOMMON)) {
                @Override
                public int getEnchantmentValue(ItemStack stack) {
                    return 150;
                }

                @Override
                public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
                    if (stack.getTag() != null) {
                        CompoundTag tag = stack.getTag();
                        if (tag.contains("SpecialAttackType")) {
                            ResourceLocation SA = new ResourceLocation(tag.getString("SpecialAttackType"));
                            if (SlashArtsRegistry.REGISTRY.get().containsKey(SA) && !Objects.equals(SlashArtsRegistry.REGISTRY.get().getValue(SA), SlashArtsRegistry.NONE.get())) {
                                components.add(Component.translatable("slashblade.tooltip.slash_art", Objects.requireNonNull(SlashArtsRegistry.REGISTRY.get().getValue(SA)).getDescription()).withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                    super.appendHoverText(stack, level, components, flag);
                }
            });
    public static final RegistryObject<Item> PROUDSOUL_CRYSTAL = ITEMS.register("proudsoul_crystal", () ->
            new ItemProudSoul((new Item.Properties()).rarity(Rarity.RARE)) {
                @Override
                public int getEnchantmentValue(ItemStack stack) {
                    return 200;
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
                    if (stack.getTag() != null) {
                        CompoundTag tag = stack.getTag();
                        if (tag.contains("SpecialEffectType")) {
                            Minecraft mcinstance = Minecraft.getInstance();
                            Player player = mcinstance.player;
                            ResourceLocation se = new ResourceLocation(tag.getString("SpecialEffectType"));
                            if (SpecialEffectsRegistry.REGISTRY.get().containsKey(se)) {
                                if (player != null) {
                                    components.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
                                                    Component.literal(String.valueOf(SpecialEffect.getRequestLevel(se)))
                                                            .withStyle(SpecialEffect.isEffective(se, player.experienceLevel) ? ChatFormatting.RED
                                                                    : ChatFormatting.DARK_GRAY))
                                            .withStyle(ChatFormatting.GRAY));
                                }
                            }
                        }
                    }
                    super.appendHoverText(stack, level, components, flag);
                }
            });
    public static final RegistryObject<Item> PROUDSOUL_TRAPEZOHEDRON = ITEMS.register("proudsoul_trapezohedron", () ->
            new ItemProudSoul((new Item.Properties()).rarity(Rarity.EPIC)) {
                @Override
                public int getEnchantmentValue(ItemStack stack) {
                    return TRAPEZOHEDRON_MAX_REFINE.get();
                }
            });

    public static final RegistryObject<Item> BLADESTAND_1 = ITEMS.register("bladestand_1", () ->
            new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> BLADESTAND_2 = ITEMS.register("bladestand_2", () ->
            new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> BLADESTAND_V = ITEMS.register("bladestand_v", () ->
            new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> BLADESTAND_S = ITEMS.register("bladestand_s", () ->
            new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> BLADESTAND_1_W = ITEMS.register("bladestand_1w", () ->
            new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON),true));
    public static final RegistryObject<Item> BLADESTAND_2_W = ITEMS.register("bladestand_2w", () ->
            new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON),true));

    public static final RegistryObject<Item> SLASHBLADE_WOOD = ITEMS.register("slashblade_wood", () ->
            new ItemSlashBladeDetune(new ItemTierSlashBlade(60, 2F), 2, 0.0F,
                    new Item.Properties()).setDestructable()
                    .setTexture(SlashBlade.prefix("model/wood.png")));
    public static final RegistryObject<Item> SLASHBLADE_BAMBOO = ITEMS.register("slashblade_bamboo", () ->
            new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 3F), 3, 0.0F,
                    new Item.Properties()).setDestructable()
                    .setTexture(SlashBlade.prefix("model/bamboo.png")));
    public static final RegistryObject<Item> SLASHBLADE_SILVERBAMBOO = ITEMS.register("slashblade_silverbamboo", () ->
            new ItemSlashBladeDetune(new ItemTierSlashBlade(40, 3F), 3, 0.0F,
                    new Item.Properties()).setTexture(SlashBlade.prefix("model/silverbamboo.png")));
    public static final RegistryObject<Item> SLASHBLADE_WHITE = ITEMS.register("slashblade_white", () ->
            new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 4F), 4, 0.0F,
                    new Item.Properties()).setTexture(SlashBlade.prefix("model/white.png")));
    public static final RegistryObject<Item> SLASHBLADE = ITEMS.register("slashblade", () ->
            new ItemSlashBlade(new ItemTierSlashBlade(40, 4F), 4, 0.0F, new Item.Properties()));
}
