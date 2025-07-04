package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class SlashBladeShapedRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category = RecipeCategory.COMBAT;
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    @Nullable
    private String group;
    private boolean showNotification = true;
    private ResourceLocation blade = null;

    public SlashBladeShapedRecipeBuilder(ItemLike item, int count) {
        this.result = item.asItem();
        this.count = count;
    }

    public static SlashBladeShapedRecipeBuilder shaped(ResourceLocation blade) {
        return shaped(SBItems.slashblade, 1).blade(blade);
    }

    public static SlashBladeShapedRecipeBuilder shaped(ItemLike result) {
        return shaped(result, 1);
    }

    public static SlashBladeShapedRecipeBuilder shaped(ItemLike result, int count) {
        return new SlashBladeShapedRecipeBuilder(result, count);
    }

    public SlashBladeShapedRecipeBuilder define(Character key, TagKey<Item> tag) {
        return this.define(key, Ingredient.of(tag));
    }

    public SlashBladeShapedRecipeBuilder define(Character key, ItemLike tag) {
        return this.define(key, Ingredient.of(tag));
    }

    public SlashBladeShapedRecipeBuilder blade(ResourceLocation blade) {
        this.blade = blade;
        return this;
    }

    public SlashBladeShapedRecipeBuilder define(Character key, Ingredient ingredient) {
        if (this.key.containsKey(key)) {
            throw new IllegalArgumentException("Symbol '" + key + "' is already defined!");
        } else if (key == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(key, ingredient);
            return this;
        }
    }

    public SlashBladeShapedRecipeBuilder pattern(String pattern) {
        if (!this.rows.isEmpty() && pattern.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pattern);
            return this;
        }
    }

    @Override
    public @NotNull SlashBladeShapedRecipeBuilder unlockedBy(@NotNull String key, @NotNull CriterionTriggerInstance trigger) {
        this.advancement.addCriterion(key, trigger);
        return this;
    }

    @Override
    public @NotNull SlashBladeShapedRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public SlashBladeShapedRecipeBuilder showNotification(boolean show) {
        this.showNotification = show;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer) {
        this.save(consumer, this.blade != null ? this.blade : Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.getResult())));
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        consumer.accept(new SlashBladeShapedRecipeBuilder.Result(id, this.result, this.count, this.blade,
                this.group == null ? "" : this.group, this.rows, this.key, this.advancement,
                id.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.showNotification));
    }

    private void ensureValid(ResourceLocation p_126144_) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + p_126144_ + "!");
        } else {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');

            for (String s : this.rows) {
                for (int i = 0; i < s.length(); ++i) {
                    char c0 = s.charAt(i);
                    if (!this.key.containsKey(c0) && c0 != ' ') {
                        throw new IllegalStateException(
                                "Pattern in recipe " + p_126144_ + " uses undefined symbol '" + c0 + "'");
                    }

                    set.remove(c0);
                }
            }

            if (!set.isEmpty()) {
                throw new IllegalStateException(
                        "Ingredients are defined but not used in pattern for recipe " + p_126144_);
            } else if (this.rows.size() == 1 && this.rows.get(0).length() == 1) {
                throw new IllegalStateException("Shaped recipe " + p_126144_
                        + " only takes in a single item - should it be a shapeless recipe instead?");
            } else if (this.advancement.getCriteria().isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + p_126144_);
            }
        }
    }

    public static class Result extends CraftingRecipeBuilder.CraftingResult {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final ResourceLocation bladeId;
        private final boolean showNotification;

        public Result(ResourceLocation id, Item result, int count, ResourceLocation bladeId, String group,
                      List<String> pattern, Map<Character, Ingredient> key, Advancement.Builder advancement,
                      ResourceLocation advancementId, boolean showNotification) {
            super(CraftingBookCategory.EQUIPMENT);
            this.id = id;
            this.result = result;
            this.count = count;
            this.group = group;
            this.pattern = pattern;
            this.key = key;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.bladeId = bladeId;
            this.showNotification = showNotification;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            super.serializeRecipeData(json);
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();

            for (String s : this.pattern) {
                jsonarray.add(s);
            }

            json.add("pattern", jsonarray);
            JsonObject jsonobject = new JsonObject();

            for (Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }

            json.add("key", jsonobject);
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result)).toString());
            if (this.count > 1) {
                jsonobject1.addProperty("count", this.count);
            }

            json.add("result", jsonobject1);

            if (this.bladeId != null) {
                json.addProperty("blade", this.bladeId.toString());
            }

            json.addProperty("show_notification", this.showNotification);
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return SlashBladeShapedRecipe.SERIALIZER;
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Override
        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
