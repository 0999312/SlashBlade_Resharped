package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlashBladeShapedRecipe extends ShapedRecipe {

    public static final RecipeSerializer<SlashBladeShapedRecipe> SERIALIZER = new SlashBladeShapedRecipeSerializer<>(
            RecipeSerializer.SHAPED_RECIPE, SlashBladeShapedRecipe::new);

    private final ResourceLocation outputBlade;

    public SlashBladeShapedRecipe(ShapedRecipe compose, ResourceLocation outputBlade) {
        super(compose.getId(), compose.getGroup(), compose.category(), compose.getWidth(), compose.getHeight(),
                compose.getIngredients(), getResultBlade(outputBlade));
        this.outputBlade = outputBlade;
    }

    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = ForgeRegistries.ITEMS.containsKey(outputBlade) ? ForgeRegistries.ITEMS.getValue(outputBlade)
                : SBItems.slashblade;

        return Objects.requireNonNull(bladeItem).getDefaultInstance();
    }

    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) {
        ItemStack result = SlashBladeShapedRecipe.getResultBlade(this.getOutputBlade());

        if (!Objects.equals(ForgeRegistries.ITEMS.getKey(result.getItem()), getOutputBlade())) {
            result = Objects.requireNonNull(access.registryOrThrow(SlashBladeDefinition.REGISTRY_KEY).get(getOutputBlade()))
                    .getBlade(result.getItem());
        }

        return result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess access) {
        var result = this.getResultItem(access);
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SBItems.slashblade);
        }

        var resultState = result.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        for (var stack : container.getItems()) {
            if (!(stack.getItem() instanceof ItemSlashBlade)) {
                continue;
            }
            var ingredientState = stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

            resultState.setProudSoulCount(resultState.getProudSoulCount() + ingredientState.getProudSoulCount());
            resultState.setKillCount(resultState.getKillCount() + ingredientState.getKillCount());
            resultState.setRefine(resultState.getRefine() + ingredientState.getRefine());
            updateEnchantment(result, stack);
        }

        return result;
    }

    private void updateEnchantment(ItemStack result, ItemStack ingredient) {
        var newItemEnchants = result.getAllEnchantments();
        var oldItemEnchants = ingredient.getAllEnchantments();
        for (Enchantment enchantIndex : oldItemEnchants.keySet()) {

            int destLevel = newItemEnchants.getOrDefault(enchantIndex, 0);
            int srcLevel = oldItemEnchants.get(enchantIndex);

            srcLevel = Math.max(srcLevel, destLevel);
            srcLevel = Math.min(srcLevel, enchantIndex.getMaxLevel());

            boolean canApplyFlag = enchantIndex.canApplyAtEnchantingTable(result);
            if (canApplyFlag) {
                for (Enchantment curEnchantIndex : newItemEnchants.keySet()) {
                    if (curEnchantIndex != enchantIndex
                            && !enchantIndex.isCompatibleWith(curEnchantIndex) /* canApplyTogether */) {
                        canApplyFlag = false;
                        break;
                    }
                }
                if (canApplyFlag) {
                    newItemEnchants.put(enchantIndex, srcLevel);
                }
            }
        }
        EnchantmentHelper.setEnchantments(newItemEnchants, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

}
