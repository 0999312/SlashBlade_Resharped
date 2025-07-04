package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class ItemTierSlashBlade implements Tier {

    private final int uses;
    private final float attack;

    public ItemTierSlashBlade(int uses, float attack) {
        this.attack = attack;
        this.uses = uses;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public float getAttackDamageBonus() {
        return attack;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(SlashBladeItemTags.PROUD_SOULS);
    }
}
