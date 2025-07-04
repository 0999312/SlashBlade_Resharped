package mods.flammpfeil.slashblade.data;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipeBuilder;
import mods.flammpfeil.slashblade.registry.slashblade.EnchantmentDefinition;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SlashBladeRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public SlashBladeRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, SBItems.slashblade_wood).pattern("  L").pattern(" L ")
                .pattern("B  ").define('B', Items.WOODEN_SWORD).define('L', ItemTags.LOGS)
                .unlockedBy(getHasName(Items.WOODEN_SWORD), has(Items.WOODEN_SWORD)).save(consumer);
        SlashBladeShapedRecipeBuilder.shaped(SBItems.slashblade_bamboo).pattern("  L").pattern(" L ").pattern("B  ")
                .define('B', SBItems.slashblade_wood).define('L', SlashBladeItemTags.BAMBOO)
                .unlockedBy(getHasName(SBItems.slashblade_wood), has(SBItems.slashblade_wood)).save(consumer);
        SlashBladeShapedRecipeBuilder.shaped(SBItems.slashblade_silverbamboo).pattern(" EI").pattern("SBD")
                .pattern("PS ").define('B', SBItems.slashblade_bamboo).define('I', Tags.Items.INGOTS_IRON)
                .define('S', Tags.Items.STRING).define('P', Items.PAPER).define('E', Items.EGG)
                .define('D', Tags.Items.DYES_BLACK)
                .unlockedBy(getHasName(SBItems.slashblade_bamboo), has(SBItems.slashblade_bamboo)).save(consumer);
        SlashBladeShapedRecipeBuilder.shaped(SBItems.slashblade_white).pattern("  L").pattern(" L ").pattern("BG ")
                .define('B', SBItems.slashblade_wood).define('L', SBItems.proudsoul_ingot)
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy(getHasName(SBItems.slashblade_wood), has(SBItems.slashblade_wood)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.YAMATO.location())
                .pattern("PPP")
                .pattern("PBP")
                .pattern("PPP")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.YAMATO.location()).addSwordType(SwordType.BROKEN)
                                .addSwordType(SwordType.SEALED).build()))
                .define('P', SBItems.proudsoul_sphere)
                .unlockedBy(getHasName(SBItems.proudsoul_sphere), has(SBItems.proudsoul_sphere))
                .save(consumer, SlashBlade.prefix("yamato_fix"));

        SlashBladeShapedRecipeBuilder.shaped(SBItems.slashblade).pattern(" EI").pattern("PBD").pattern("SI ")
                .define('B',
                        SlashBladeIngredient.of(SBItems.slashblade_white,
                                RequestDefinition.Builder.newInstance().addSwordType(SwordType.BROKEN).build()))
                .define('I', Tags.Items.INGOTS_GOLD).define('S', Tags.Items.STRING).define('P', Tags.Items.DYES_BLUE)
                .define('E', Tags.Items.RODS_BLAZE).define('D', Tags.Items.STORAGE_BLOCKS_COAL)
                .unlockedBy(getHasName(SBItems.slashblade_white), has(SBItems.slashblade_white)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.RUBY.location()).pattern("DPI").pattern("PB ")
                .pattern("S  ")
                .define('B',
                        SlashBladeIngredient.of(SBItems.slashblade_silverbamboo,
                                RequestDefinition.Builder.newInstance().addSwordType(SwordType.BROKEN).build()))
                .define('I', SBItems.proudsoul).define('S', Tags.Items.STRING).define('P', SBItems.proudsoul_ingot)
                .define('D', Tags.Items.DYES_RED)
                .unlockedBy(getHasName(SBItems.slashblade_silverbamboo), has(SBItems.slashblade_silverbamboo))
                .save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.FOX_BLACK.location()).pattern(" EF")
                .pattern("BCS").pattern("WQ ").define('W', Tags.Items.CROPS_WHEAT)
                .define('Q', Tags.Items.STORAGE_BLOCKS_QUARTZ).define('B', Items.BLAZE_POWDER)
                .define('S', SBItems.proudsoul_crystal).define('E', Tags.Items.OBSIDIAN)
                .define('F', Tags.Items.FEATHERS)
                .define('C', SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                        .name(SlashBladeBuiltInRegistry.RUBY.location())
                        .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.SMITE), 1)).build()))

                .unlockedBy(getHasName(SBItems.slashblade_silverbamboo), has(SBItems.slashblade_silverbamboo))
                .save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.FOX_WHITE.location()).pattern(" EF")
                .pattern("BCS").pattern("WQ ").define('W', Tags.Items.CROPS_WHEAT)
                .define('Q', Tags.Items.STORAGE_BLOCKS_QUARTZ).define('B', Items.BLAZE_POWDER)
                .define('S', SBItems.proudsoul_crystal).define('E', Tags.Items.OBSIDIAN)
                .define('F', Tags.Items.FEATHERS)
                .define('C',
                        SlashBladeIngredient.of(
                                RequestDefinition.Builder.newInstance().name(SlashBladeBuiltInRegistry.RUBY.location())

                                        .addEnchantment(new EnchantmentDefinition(
                                                getEnchantmentID(Enchantments.MOB_LOOTING), 1))
                                        .build()))

                .unlockedBy(getHasName(SBItems.slashblade_silverbamboo), has(SBItems.slashblade_silverbamboo))
                .save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.MURAMASA.location()).pattern("SSS")
                .pattern("SBS").pattern("SSS")
                .define('B',
                        SlashBladeIngredient
                                .of(RequestDefinition.Builder.newInstance().proudSoul(10000).refineCount(20).build()))
                .define('S', Ingredient.of(SBItems.proudsoul_sphere))
                .unlockedBy(getHasName(SBItems.slashblade), has(SBItems.slashblade)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.TAGAYASAN.location()).pattern("SES")
                .pattern("DBD").pattern("SES")
                .define('B',
                        SlashBladeIngredient.of(SBItems.slashblade_wood, RequestDefinition.Builder.newInstance()
                                .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.UNBREAKING), 1))
                                .proudSoul(1000).refineCount(10).build()))
                .define('S', Ingredient.of(SBItems.proudsoul_sphere)).define('E', Ingredient.of(Items.ENDER_EYE))
                .define('D', Ingredient.of(Items.ENDER_PEARL))
                .unlockedBy(getHasName(SBItems.slashblade_wood), has(SBItems.slashblade_wood)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.AGITO.location()).pattern(" S ").pattern("SBS")
                .pattern(" S ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.AGITO_RUST.location()).killCount(100).build()))
                .define('S', Ingredient.of(SBItems.proudsoul))
                .unlockedBy(getHasName(SBItems.proudsoul), has(SBItems.proudsoul)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.OROTIAGITO_SEALED.location()).pattern(" S ")
                .pattern("SBS").pattern(" S ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.OROTIAGITO_RUST.location()).killCount(100).build()))
                .define('S', Ingredient.of(SBItems.proudsoul))
                .unlockedBy(getHasName(SBItems.proudsoul), has(SBItems.proudsoul)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.OROTIAGITO.location()).pattern("PSP")
                .pattern("SBS").pattern("PSP")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.OROTIAGITO_SEALED.location()).killCount(1000)
                                .proudSoul(1000).refineCount(10).build()))
                .define('P', Ingredient.of(SBItems.proudsoul)).define('S', Ingredient.of(SBItems.proudsoul_sphere))
                .unlockedBy(getHasName(SBItems.proudsoul_sphere), has(SBItems.proudsoul_sphere)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.DOUTANUKI.location()).pattern("  P")
                .pattern(" B ").pattern("P  ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.SABIGATANA.location()).killCount(100).proudSoul(1000)
                                .refineCount(10).build()))
                .define('P', Ingredient.of(SBItems.proudsoul_sphere))
                .unlockedBy(getHasName(SBItems.proudsoul_sphere), has(SBItems.proudsoul_sphere)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.SABIGATANA.location()).pattern("  P")
                .pattern(" P ").pattern("B  ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.SABIGATANA.location()).addSwordType(SwordType.BROKEN)
                                .addSwordType(SwordType.SEALED).build()))
                .define('P', Ingredient.of(SBItems.proudsoul_ingot))
                .unlockedBy(getHasName(SBItems.proudsoul_ingot), has(SBItems.proudsoul_ingot)).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.TUKUMO.location()).pattern("ESD").pattern("RBL")
                .pattern("ISG").define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .define('L', Tags.Items.STORAGE_BLOCKS_LAPIS).define('G', Tags.Items.STORAGE_BLOCKS_GOLD)
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON).define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('E', Tags.Items.STORAGE_BLOCKS_EMERALD)
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .addEnchantment(
                                        new EnchantmentDefinition(getEnchantmentID(Enchantments.FIRE_ASPECT), 1))
                                .build()))
                .define('S', Ingredient.of(SBItems.proudsoul_sphere))
                .unlockedBy(getHasName(SBItems.slashblade), has(SBItems.slashblade)).save(consumer);

        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_WOODEN.location(), Items.WOODEN_SWORD, consumer);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_STONE.location(), Items.STONE_SWORD, consumer);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_IRON.location(), Items.IRON_SWORD, consumer);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_GOLDEN.location(), Items.GOLDEN_SWORD, consumer);
        rodaiAdvRecipe(SlashBladeBuiltInRegistry.RODAI_DIAMOND.location(), Items.DIAMOND_SWORD, consumer);
        rodaiAdvRecipe(SlashBladeBuiltInRegistry.RODAI_NETHERITE.location(), Items.NETHERITE_SWORD, consumer);
    }

    private void rodaiRecipe(ResourceLocation rodai, ItemLike sword, Consumer<FinishedRecipe> consumer) {
        SlashBladeShapedRecipeBuilder.shaped(rodai).pattern("  P").pattern(" B ").pattern("WS ").define('B',
                        SlashBladeIngredient.of(SBItems.slashblade_silverbamboo,
                                RequestDefinition.Builder.newInstance().killCount(100).addSwordType(SwordType.BROKEN).build()))
                .define('W', Ingredient.of(sword)).define('S', Ingredient.of(Tags.Items.STRING))
                .define('P', Ingredient.of(SBItems.proudsoul_crystal))
                .unlockedBy(getHasName(SBItems.slashblade_silverbamboo), has(SBItems.slashblade_silverbamboo))
                .save(consumer);
    }

    private void rodaiAdvRecipe(ResourceLocation rodai, ItemLike sword, Consumer<FinishedRecipe> consumer) {
        SlashBladeShapedRecipeBuilder.shaped(rodai).pattern("  P").pattern(" B ").pattern("WS ").define('B',
                        SlashBladeIngredient.of(SBItems.slashblade_silverbamboo,
                                RequestDefinition.Builder.newInstance().killCount(100).addSwordType(SwordType.BROKEN).build()))
                .define('W', Ingredient.of(sword)).define('S', Ingredient.of(Tags.Items.STRING))
                .define('P', Ingredient.of(SBItems.proudsoul_trapezohedron))
                .unlockedBy(getHasName(SBItems.slashblade_silverbamboo), has(SBItems.slashblade_silverbamboo))
                .save(consumer);
    }

    private static ResourceLocation getEnchantmentID(Enchantment enchantment) {
        return ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
    }
}
