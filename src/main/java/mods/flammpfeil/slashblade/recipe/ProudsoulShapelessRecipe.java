package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ProudsoulShapelessRecipe extends ShapelessRecipe {

    public ProudsoulShapelessRecipe(ResourceLocation p_251840_, String p_249640_, CraftingBookCategory p_249390_,
                                    ItemStack p_252071_, NonNullList<Ingredient> p_250689_) {
        super(p_251840_, p_249640_, p_249390_, p_252071_, p_250689_);
    }

    public static final RecipeSerializer<ProudsoulShapelessRecipe> SERIALIZER = new ProudsoulShapelessRecipe.Serializer();

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess access) {
        ItemStack result = super.assemble(container, access);
        Map<Enchantment, Integer> all = Maps.newHashMap();

        for (int idx = 0; idx < container.getContainerSize(); idx++) {
            ItemStack stack = container.getItem(idx);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.isEnchanted()) {
                continue;
            }

            Map<Enchantment, Integer> emap = EnchantmentHelper.getEnchantments(stack);
            all.putAll(emap);
        }

        EnchantmentHelper.setEnchantments(all, result);
        return result;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
        boolean result = super.matches(container, level);

        if (result) {
            Map<Enchantment, Integer> all = Maps.newHashMap();

            int soulCount = 0;

            for (int idx = 0; idx < container.getContainerSize(); idx++) {
                ItemStack stack = container.getItem(idx);
                if (stack.isEmpty()) {
                    continue;
                }
                if (!stack.isEnchanted()) {
                    continue;
                }

                soulCount++;

                Map<Enchantment, Integer> emap = EnchantmentHelper.getEnchantments(stack);

                for (Map.Entry<Enchantment, Integer> entry : emap.entrySet()) {
                    if (all.containsKey(entry.getKey())) {

                        int value = all.get(entry.getKey()) + entry.getValue();

                        all.put(entry.getKey(), value);
                    } else {
                        all.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            result = all.size() == 1 || all.isEmpty();
            if (result) {
                for (Map.Entry<Enchantment, Integer> entry : all.entrySet()) {
                    result = entry.getValue() == soulCount;
                }
            }
        }

        return result;
    }

    public static class Serializer implements RecipeSerializer<ProudsoulShapelessRecipe> {
        @Override
        public @NotNull ProudsoulShapelessRecipe fromJson(@NotNull ResourceLocation p_44290_, @NotNull JsonObject p_44291_) {
            String s = GsonHelper.getAsString(p_44291_, "group", "");
            @SuppressWarnings("deprecation")
            CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(p_44291_, "category", null), CraftingBookCategory.MISC);
            NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(p_44291_, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (nonnulllist.size() > 3 * 3) {
                throw new JsonParseException("Too many ingredients for shapeless recipe. The maximum is " + (3 * 3));
            } else {
                ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(p_44291_, "result"));
                return new ProudsoulShapelessRecipe(p_44290_, s, craftingbookcategory, itemstack, nonnulllist);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray p_44276_) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < p_44276_.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(p_44276_.get(i), false);
                nonnulllist.add(ingredient);
            }

            return nonnulllist;
        }

        @Override
        public ProudsoulShapelessRecipe fromNetwork(@NotNull ResourceLocation p_44293_, FriendlyByteBuf p_44294_) {
            String s = p_44294_.readUtf();
            CraftingBookCategory craftingbookcategory = p_44294_.readEnum(CraftingBookCategory.class);
            int i = p_44294_.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

            nonnulllist.replaceAll(ignored -> Ingredient.fromNetwork(p_44294_));

            ItemStack itemstack = p_44294_.readItem();
            return new ProudsoulShapelessRecipe(p_44293_, s, craftingbookcategory, itemstack, nonnulllist);
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44281_, ProudsoulShapelessRecipe p_44282_) {
            p_44281_.writeUtf(p_44282_.getGroup());
            p_44281_.writeEnum(p_44282_.category());
            p_44281_.writeVarInt(p_44282_.getIngredients().size());

            for (Ingredient ingredient : p_44282_.getIngredients()) {
                ingredient.toNetwork(p_44281_);
            }

            p_44281_.writeItem(p_44282_.getResultItem(null));
        }
    }

}
