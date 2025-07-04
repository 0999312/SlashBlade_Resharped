package mods.flammpfeil.slashblade;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class SlashBladeConfig {
    public static ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.IntValue MAX_PROUDSOUL_DROP;
    public static ForgeConfigSpec.IntValue MAX_ENCHANTED_PROUDSOUL_DROP;

    public static ForgeConfigSpec.BooleanValue HUNGER_CAN_REPAIR;
    public static ForgeConfigSpec.IntValue MAX_PROUD_SOUL_GOT;
    public static ForgeConfigSpec.IntValue SUMMON_SWORD_COST;
    public static ForgeConfigSpec.IntValue SUMMON_SWORD_ART_COST;

    public static ForgeConfigSpec.BooleanValue SELF_REPAIR_ENABLE;
    public static ForgeConfigSpec.BooleanValue SELF_REPAIR_COST_EXP;

    public static ForgeConfigSpec.DoubleValue BEWITCHED_HUNGER_EXHAUSTION;
    public static ForgeConfigSpec.IntValue BEWITCHED_EXP_COST;

    public static ForgeConfigSpec.BooleanValue PVP_ENABLE;
    public static ForgeConfigSpec.BooleanValue FRIENDLY_ENABLE;
    public static ForgeConfigSpec.DoubleValue SABIGATANA_SPAWN_CHANCE;
    public static ForgeConfigSpec.DoubleValue BROKEN_SABIGATANA_SPAWN_CHANCE;
    public static ForgeConfigSpec.IntValue REFINE_LEVEL_COST;

    public static ForgeConfigSpec.DoubleValue SLASHBLADE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue REFINE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.IntValue TRAPEZOHEDRON_MAX_REFINE;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> NON_DROPPABLE_ENCHANTMENT;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");

        MAX_PROUDSOUL_DROP = COMMON_BUILDER.comment("Determining the count for proud soul when blade be broken.")
                .defineInRange("max_proudsoul_drop", 10, 1, 64);
        MAX_ENCHANTED_PROUDSOUL_DROP = COMMON_BUILDER.comment("Determining the count for enchanted proud soul when blade be broken.")
                .defineInRange("max_enchanted_proudsoul_drop", 10, 1, 64);

        SABIGATANA_SPAWN_CHANCE = COMMON_BUILDER.comment("Determining the spawn chance of sabigatana.")
                .defineInRange("sabigatana_spawn_chance", 0.05D, 0.0D, 1.0D);

        BROKEN_SABIGATANA_SPAWN_CHANCE = COMMON_BUILDER.comment("Determining the spawn chance of a broken sabigatana.")
                .defineInRange("broken_sabigatana_spawn_chance", 0.15D, 0.0D, 1.0D);

        HUNGER_CAN_REPAIR = COMMON_BUILDER.comment("Determines whether to make hunger effect repair slashblade.",
                        "If enable, if player has hunger effect, your slashblade in hotbar will be repaired, cost player's hunger.")
                .define("hunger_can_repair", true);
        PVP_ENABLE = COMMON_BUILDER.comment("Determines whether to enable slashblade's PVP.",
                "If enable, player can attack player with SlashBlade.").define("pvp_enable", false);
        FRIENDLY_ENABLE = COMMON_BUILDER.comment("Determines whether to enable slashblade's friendly fire.",
                "If enable, player can attack friendly entity with SlashBlade.").define("friendly_enable", false);

        REFINE_LEVEL_COST = COMMON_BUILDER.comment("Determining the level cost for refine a slashblade.")
                .defineInRange("refine_level_cost", 1, 1, Integer.MAX_VALUE);

        SUMMON_SWORD_COST = COMMON_BUILDER.comment("Determining the proud soul cost for single summon mirage blade.")
                .defineInRange("summon_sword_cost", 2, 1, Integer.MAX_VALUE);
        SUMMON_SWORD_ART_COST = COMMON_BUILDER.comment("Determining the proud soul cost for summon blade arts.")
                .defineInRange("summon_blade_art_cost", 20, 1, Integer.MAX_VALUE);

        MAX_PROUD_SOUL_GOT = COMMON_BUILDER.comment("Determining the max proud soul count for single mobs kill.")
                .defineInRange("max_proud_soul_got", 100, 1, Integer.MAX_VALUE);

        SELF_REPAIR_ENABLE = COMMON_BUILDER.comment("Determines whether to enable slashblade's self-repair.",
                "If enable, bewitched slashblade will try repair itself, cost hunger & exp.").define("self_repair", true);

        BEWITCHED_HUNGER_EXHAUSTION = COMMON_BUILDER
                .comment("Determining the base exhaustion for slashblade's self-repair.")
                .defineInRange("bewitched_hunger_exhaustion", 0.05D, 0.0001D, Double.MAX_VALUE);

        SELF_REPAIR_COST_EXP = COMMON_BUILDER.comment("Determines whether slashblade's self-repair cost experiences.",
                "If enable, self repair will cost player's experiences.").define("self_repair_cost_exp", true);

        BEWITCHED_EXP_COST = COMMON_BUILDER.comment("Determining the base exp cost for slashblade's self-repair.")
                .defineInRange("bewitched_exp_cost", 1, 1, Integer.MAX_VALUE);

        SLASHBLADE_DAMAGE_MULTIPLIER = COMMON_BUILDER.comment("Blade Damage: Base Damage × Multiplier.[Default: 1.0D]")
                .defineInRange("slashblade_damage_multiplier", 1.0D, 0.0D, 1024.0D);

        REFINE_DAMAGE_MULTIPLIER = COMMON_BUILDER.comment("S-Rank Refine Bonus: Each Refine × Multiplier'value Damage.[Default: 1.D]")
                .defineInRange("refine_damage_multiplier", 1.0D, 0.0D, 1024.0D);

        TRAPEZOHEDRON_MAX_REFINE = COMMON_BUILDER.comment("The maximum number of refine of Trapezohedron.[Default: 2147483647(infinity)]")
                .defineInRange("trapezohedron_max_refine", Integer.MAX_VALUE, 200, Integer.MAX_VALUE);

        NON_DROPPABLE_ENCHANTMENT = COMMON_BUILDER.comment("Example: 'minecraft:sharpness', That will prevent the enchantment from dropping the corresponding proudsoul tiny.")
                .defineList("non-droppable_enchantments", new ObjectArrayList<>(), o -> o instanceof String);

        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
