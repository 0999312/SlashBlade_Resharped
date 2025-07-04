package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class AdvancementHelper {

    static public final ResourceLocation ADVANCEMENT_COMBO_A = SlashBlade.prefix("arts/combo_a");
    static public final ResourceLocation ADVANCEMENT_COMBO_A_EX = SlashBlade.prefix("arts/combo_a_ex");
    static public final ResourceLocation ADVANCEMENT_COMBO_B = SlashBlade.prefix("arts/combo_b");
    static public final ResourceLocation ADVANCEMENT_COMBO_B_MAX = SlashBlade.prefix("arts/combo_b_max");
    static public final ResourceLocation ADVANCEMENT_COMBO_C = SlashBlade.prefix("arts/combo_c");
    static public final ResourceLocation ADVANCEMENT_AERIAL_A = SlashBlade.prefix("arts/aerial_a");
    static public final ResourceLocation ADVANCEMENT_AERIAL_B = SlashBlade.prefix("arts/aerial_b");
    static public final ResourceLocation ADVANCEMENT_UPPERSLASH = SlashBlade.prefix("arts/upperslash");
    static public final ResourceLocation ADVANCEMENT_UPPERSLASH_JUMP = SlashBlade.prefix("arts/upperslash_jump");
    static public final ResourceLocation ADVANCEMENT_AERIAL_CLEAVE = SlashBlade.prefix("arts/aerial_cleave");
    static public final ResourceLocation ADVANCEMENT_RISING_STAR = SlashBlade.prefix("arts/rising_star");
    static public final ResourceLocation ADVANCEMENT_RAPID_SLASH = SlashBlade.prefix("arts/rapid_slash");
    static public final ResourceLocation ADVANCEMENT_JUDGEMENT_CUT = SlashBlade.prefix("arts/judgement_cut");
    static public final ResourceLocation ADVANCEMENT_JUDGEMENT_CUT_JUST = SlashBlade.prefix("arts/judgement_cut_just");
    static public final ResourceLocation ADVANCEMENT_QUICK_CHARGE = SlashBlade.prefix("arts/quick_charge");

    public static void grantCriterion(LivingEntity entity, ResourceLocation resourcelocation) {
        if (entity instanceof ServerPlayer) {
            grantCriterion((ServerPlayer) entity, resourcelocation);
        }
    }

    public static void grantCriterion(ServerPlayer player, ResourceLocation resourcelocation) {
        Advancement adv = Objects.requireNonNull(player.getServer()).getAdvancements().getAdvancement(resourcelocation);
        if (adv == null) {
            return;
        }

        AdvancementProgress advancementprogress = player.getAdvancements().getOrStartProgress(adv);
        if (advancementprogress.isDone()) {
            return;
        }

        for (String s : advancementprogress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, s);
        }
    }

    static final ResourceLocation EXEFFECT_ENCHANTMENT = SlashBlade.prefix("enchantment/");

    static public void grantedIf(Enchantment enchantment, LivingEntity owner) {
        int level = owner.getMainHandItem().getEnchantmentLevel(enchantment);
        if (0 < level) {
            grantCriterion(owner, EXEFFECT_ENCHANTMENT.withSuffix("root"));
            grantCriterion(owner,
                    EXEFFECT_ENCHANTMENT.withSuffix(Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantment)).getPath()));
        }
    }
}
