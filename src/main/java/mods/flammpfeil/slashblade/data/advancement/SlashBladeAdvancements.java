package mods.flammpfeil.slashblade.data.advancement;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SlashBladeAdvancements extends AdvancementProvider {
    public SlashBladeAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new Generator()));
    }
    
    public static class Generator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .display(SlashBladeItems.SLASHBLADE.get(),
                        Component.translatable("itemGroup.slashblade"),
                        Component.translatable("itemGroup.slashblade.desc"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/chiseled_quartz_block.png"),
                        AdvancementType.TASK, true, true, false)
                    .addCriterion("attackTheEntity", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntity())
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("simple_slashblade"))
                        .addRecipe(SlashBlade.prefix("s_wood"))
                        .addLootTable(lootTable("rewards/first"))),
                "root");
            
            AdvancementHolder movesets = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(root)
                    .display(Items.COMMAND_BLOCK,
                        Component.translatable("adv.slashblade.movesets"),
                        Component.translatable("adv.slashblade.movesets.desc"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("crafting", hasItem(SlashBladeItems.SLASHBLADE.get())),
                "movesets");
            
            
            proudsouls(saver, existingFileHelper, root);
            swords(saver, existingFileHelper, root);
            reforge(saver, existingFileHelper, root);
            bladestand(saver, existingFileHelper, root);
            exeffect(saver, existingFileHelper, root);
            abilities(saver, existingFileHelper, movesets);
            arts(saver, existingFileHelper, movesets);
            summonedSwords(saver, existingFileHelper, movesets);
        }
        
        public static void proudsouls(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder root) {
            AdvancementHolder proudsoul_tiny = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(root)
                    .display(SlashBladeItems.PROUDSOUL_TINY.get(),
                        Component.translatable("item.slashblade.proudsoul_tiny"),
                        Component.translatable("adv.slashblade.proudsoul_tiny"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("BrokenBlade", hasItem(SlashBladeItems.PROUDSOUL_TINY.get())),
                "material/proudsoul_tiny");
            
            AdvancementHolder proudsoul = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(proudsoul_tiny)
                    .display(SlashBladeItems.PROUDSOUL.get(),
                        Component.translatable("item.slashblade.proudsoul"),
                        Component.translatable("adv.slashblade.proudsoul"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("BrokenBlade", hasItem(SlashBladeItems.PROUDSOUL.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("material/tiny"))
                        .addRecipe(SlashBlade.prefix("material/ingot"))
                        .addRecipe(SlashBlade.prefix("material/soul"))
                        .addRecipe(SlashBlade.prefix("bladestand/bladestand_1"))
                        .addRecipe(SlashBlade.prefix("bladestand/bladestand_2"))),
                "material/proudsoul");
            
            AdvancementHolder proudsoul_ingot = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(proudsoul)
                    .display(SlashBladeItems.PROUDSOUL_INGOT.get(),
                        Component.translatable("item.slashblade.proudsoul_ingot"),
                        Component.translatable("adv.slashblade.proudsoul_ingot"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("BrokenBlade", hasItem(SlashBladeItems.PROUDSOUL_INGOT.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("material/sphere_smelting"))),
                "material/proudsoul_ingot");
            
            AdvancementHolder proudsoul_sphere = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(proudsoul_ingot)
                    .display(SlashBladeItems.PROUDSOUL_SPHERE.get(),
                        Component.translatable("item.slashblade.proudsoul_sphere"),
                        Component.translatable("adv.slashblade.proudsoul_sphere"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("BrokenBlade", hasItem(SlashBladeItems.PROUDSOUL_SPHERE.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("material/crystal_blasting"))),
                "material/proudsoul_sphere");
            
            AdvancementHolder proudsoul_crystal = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(proudsoul_sphere)
                    .display(SlashBladeItems.PROUDSOUL_CRYSTAL.get(),
                        Component.translatable("item.slashblade.proudsoul_crystal"),
                        Component.translatable("adv.slashblade.proudsoul_crystal"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("BrokenBlade", hasItem(SlashBladeItems.PROUDSOUL_CRYSTAL.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("material/trapezohedron_smoking"))),
                "material/proudsoul_crystal");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(proudsoul_crystal)
                    .display(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get(),
                        Component.translatable("item.slashblade.proudsoul_trapezohedron"),
                        Component.translatable("adv.slashblade.proudsoul_trapezohedron"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("BrokenBlade", hasItem(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get())),
                "material/proudsoul_trapezohedron");
        }
        
        public static void swords(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder root) {
            AdvancementHolder s_wood = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(root)
                    .display(SlashBladeItems.SLASHBLADE_WOOD.get(),
                        Component.translatable("item.slashblade.slashblade_wood"),
                        Component.translatable("item.slashblade.slashblade_wood.desc"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("crafting", hasItem(SlashBladeItems.SLASHBLADE_WOOD.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("slashblade_white"))
                        .addRecipe(SlashBlade.prefix("slashblade_bamboo"))
                        .addLootTable(lootTable("rewards/wood"))),
                "blade/s_wood");
            
            AdvancementHolder s_white = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(s_wood)
                    .display(SlashBladeItems.SLASHBLADE_WHITE.get(),
                        Component.translatable("item.slashblade.slashblade_white"),
                        Component.translatable("item.slashblade.slashblade_white.desc"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("crafting", hasItem(SlashBladeItems.SLASHBLADE_WHITE.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("slashblade"))),
                "blade/s_white");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(s_white)
                    .display(SlashBladeItems.SLASHBLADE.get(),
                        Component.translatable("item.slashblade.slashblade"),
                        Component.translatable("item.slashblade.slashblade.desc"),
                        null, AdvancementType.TASK, true, true, true)
                    .addCriterion("crafting",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().build())),
                "blade/s_nameless");
            
            AdvancementHolder s_bamboo = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(s_wood)
                    .display(SlashBladeItems.SLASHBLADE_BAMBOO.get(),
                        Component.translatable("item.slashblade.slashblade_bamboo"),
                        Component.translatable("item.slashblade.slashblade_bamboo.desc"),
                        null, AdvancementType.TASK, true, true, true)
                    .addCriterion("crafting", hasItem(SlashBladeItems.SLASHBLADE_BAMBOO.get()))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("slashblade_silverbamboo"))),
                "blade/s_bamboo");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(s_bamboo)
                    .display(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                        Component.translatable("item.slashblade.slashblade_silverbamboo"),
                        Component.translatable("item.slashblade.slashblade_silverbamboo.desc"),
                        null, AdvancementType.TASK, true, true, true)
                    .addCriterion("crafting", hasItem(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get())),
                "blade/s_silverbamboo");
        }
        
        public static void reforge(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder root) {
            AdvancementHolder reforge = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(root)
                    .display(Items.ANVIL,
                        Component.translatable("adv.slashblade.reforge"),
                        Component.translatable("adv.slashblade.reforge.desc"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "tips/reforge");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(reforge)
                    .display(Items.ANVIL,
                        Component.translatable("adv.slashblade.refine"),
                        Component.translatable("adv.slashblade.refine.desc"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "tips/refine");
        }
        
        public static void bladestand(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder root) {
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(root)
                    .display(SlashBladeItems.BLADESTAND_2.get(),
                        Component.translatable("adv.slashblade.bladestand"),
                        Component.translatable("adv.slashblade.bladestand.desc"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("getitem1", hasItem(SlashBladeItems.BLADESTAND_1.get()))
                    .addCriterion("getitem2", hasItem(SlashBladeItems.BLADESTAND_2.get()))
                    .requirements(new AdvancementRequirements(List.of(List.of("getitem1", "getitem2"))))
                    .rewards(AdvancementRewards.Builder.recipe(SlashBlade.prefix("bladestand/bladestand_v"))
                        .addRecipe(SlashBlade.prefix("bladestand/bladestand_s"))
                        .addRecipe(SlashBlade.prefix("bladestand/bladestand_1w"))
                        .addRecipe(SlashBlade.prefix("bladestand/bladestand_2w"))),
                "tips/bladestand");
        }
        
        public static void exeffect(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder root) {
            AdvancementHolder enchantment = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(root)
                    .display(Items.ENCHANTED_BOOK,
                        Component.translatable("adv.slashblade.exeffect"),
                        Component.translatable("adv.slashblade.exeffect.desc"),
                        null, AdvancementType.TASK, true, true, true)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "enchantment/root");
            
            AdvancementHolder feather_falling = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(enchantment)
                    .display(Items.FEATHER,
                        Component.translatable("adv.slashblade.exeffect.feather_falling"),
                        Component.translatable("adv.slashblade.exeffect.feather_falling.desc"),
                        null, AdvancementType.TASK, true, true, true)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "enchantment/feather_falling");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(feather_falling)
                    .display(Items.CLOCK,
                        Component.translatable("adv.slashblade.exeffect.soul_speed"),
                        Component.translatable("adv.slashblade.exeffect.soul_speed.desc"),
                        null, AdvancementType.TASK, true, true, true)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "enchantment/soul_speed");
        }
        
        public static void abilities(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder movesets) {
            AdvancementHolder air_trick = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(movesets)
                    .display(Items.ENDER_EYE,
                        Component.translatable("adv.slashblade.air_trick"),
                        Component.translatable("adv.slashblade.air_trick.desc",
                            Component.keybind("key.slashblade.special_move"),
                            Component.keybind("key.forward"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/air_trick");
            
            AdvancementHolder guard = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(air_trick)
                    .display(Items.SHIELD,
                        Component.translatable("adv.slashblade.guard"),
                        Component.translatable("adv.slashblade.guard.desc",
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/guard");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(guard)
                    .display(Items.DIAMOND_BLOCK,
                        Component.translatable("adv.slashblade.guard_just"),
                        Component.translatable("adv.slashblade.guard_just.desc",
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/guard_just");
            
            AdvancementHolder trick_up = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(air_trick)
                    .display(Items.ENDER_PEARL,
                        Component.translatable("adv.slashblade.trick_up"),
                        Component.translatable("adv.slashblade.trick_up.desc",
                            Component.keybind("key.slashblade.special_move"),
                            Component.keybind("key.forward"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/trick_up");
            
            AdvancementHolder trick_down = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(air_trick)
                    .display(Items.ANVIL,
                        Component.translatable("adv.slashblade.trick_down"),
                        Component.translatable("adv.slashblade.trick_down.desc",
                            Component.keybind("key.slashblade.special_move"),
                            Component.keybind("key.back"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/trick_down");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(trick_down)
                    .display(Items.FEATHER,
                        Component.translatable("adv.slashblade.trick_dodge"),
                        Component.translatable("adv.slashblade.trick_dodge.desc",
                            Component.keybind("key.slashblade.special_move")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/trick_dodge");
            
            AdvancementHolder enemy_step = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(trick_up)
                    .display(Items.LEATHER_BOOTS,
                        Component.translatable("adv.slashblade.enemy_step"),
                        Component.translatable("adv.slashblade.enemy_step.desc",
                            Component.keybind("key.jump")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/enemy_step");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(enemy_step)
                    .display(Items.IRON_BOOTS,
                        Component.translatable("adv.slashblade.kick_jump"),
                        Component.translatable("adv.slashblade.kick_jump.desc",
                            Component.keybind("key.jump")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "abilities/kick_jump");
        }
        
        public static void arts(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder movesets) {
            AdvancementHolder combo_a = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(movesets)
                    .display(Items.GOLDEN_SWORD,
                        Component.translatable("adv.slashblade.combo_a"),
                        Component.translatable("adv.slashblade.combo_a.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/combo_a");
            
            AdvancementHolder combo_b = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(combo_a)
                    .display(Items.IRON_SWORD,
                        Component.translatable("adv.slashblade.combo_b"),
                        Component.translatable("adv.slashblade.combo_b.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/combo_b");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(combo_b)
                    .display(Items.MUSIC_DISC_11,
                        Component.translatable("adv.slashblade.combo_b_max"),
                        Component.translatable("adv.slashblade.combo_b_max.desc"),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/combo_b_max");
            
            AdvancementHolder combo_c = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(combo_b)
                    .display(Items.DIAMOND_SWORD,
                        Component.translatable("adv.slashblade.combo_c"),
                        Component.translatable("adv.slashblade.combo_c.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/combo_c");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(combo_c)
                    .display(Items.NETHERITE_SWORD,
                        Component.translatable("adv.slashblade.combo_a_ex"),
                        Component.translatable("adv.slashblade.combo_a_ex.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/combo_a_ex");
            
            AdvancementHolder upperslash = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(combo_a)
                    .display(Items.PISTON,
                        Component.translatable("adv.slashblade.upperslash"),
                        Component.translatable("adv.slashblade.upperslash.desc",
                            Component.keybind("key.use"),
                            Component.keybind("key.back"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/upperslash");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(upperslash)
                    .display(Items.FEATHER,
                        Component.translatable("adv.slashblade.upperslash_jump"),
                        Component.translatable("adv.slashblade.upperslash_jump.desc",
                            Component.keybind("key.use"),
                            Component.keybind("key.back"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/upperslash_jump");
            
            AdvancementHolder rapid_slash = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(upperslash)
                    .display(Items.REDSTONE,
                        Component.translatable("adv.slashblade.rapid_slash"),
                        Component.translatable("adv.slashblade.rapid_slash.desc",
                            Component.keybind("key.use"),
                            Component.keybind("key.forward"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/rapid_slash");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(rapid_slash)
                    .display(Items.BLAZE_POWDER,
                        Component.translatable("adv.slashblade.rising_star"),
                        Component.translatable("adv.slashblade.rising_star.desc",
                            Component.keybind("key.use"),
                            Component.keybind("key.forward"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/rising_star");
            
            AdvancementHolder judgement_cut = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(rapid_slash)
                    .display(Items.BOOK,
                        Component.translatable("adv.slashblade.judgement_cut"),
                        Component.translatable("adv.slashblade.judgement_cut.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/judgement_cut");
            
            AdvancementHolder judgement_cut_just = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(judgement_cut)
                    .display(Items.ENCHANTED_BOOK,
                        Component.translatable("adv.slashblade.judgement_cut_just"),
                        Component.translatable("adv.slashblade.judgement_cut_just.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/judgement_cut_just");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(judgement_cut_just)
                    .display(Items.WRITABLE_BOOK,
                        Component.translatable("adv.slashblade.quick_charge"),
                        Component.translatable("adv.slashblade.quick_charge.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/quick_charge");
            
            AdvancementHolder aerial_a = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(combo_a)
                    .display(Items.WOODEN_SWORD,
                        Component.translatable("adv.slashblade.aerial_a"),
                        Component.translatable("adv.slashblade.aerial_a.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/aerial_a");
            
            AdvancementHolder aerial_b = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(aerial_a)
                    .display(Items.STONE_SWORD,
                        Component.translatable("adv.slashblade.aerial_b"),
                        Component.translatable("adv.slashblade.aerial_b.desc",
                            Component.keybind("key.use")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/aerial_b");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(aerial_b)
                    .display(Items.IRON_AXE,
                        Component.translatable("adv.slashblade.aerial_cleave"),
                        Component.translatable("adv.slashblade.aerial_cleave.desc",
                            Component.keybind("key.use"),
                            Component.keybind("key.back"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/aerial_cleave");
        }
        
        public static void summonedSwords(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, AdvancementHolder movesets) {
            AdvancementHolder summonedswords = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(movesets)
                    .display(Items.ARROW,
                        Component.translatable("adv.slashblade.summonedswords"),
                        Component.translatable("adv.slashblade.summonedswords.desc",
                            Component.translatable("key.pickItem"),
                            Component.keybind("key.pickItem")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/shooting/summonedswords");
            
            AdvancementHolder spiral_swords = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(summonedswords)
                    .display(Items.SHIELD,
                        Component.translatable("adv.slashblade.spiral_swords"),
                        Component.translatable("adv.slashblade.spiral_swords.desc",
                            Component.translatable("key.pickItem"),
                            Component.keybind("key.pickItem")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/shooting/spiral_swords");
            
            AdvancementHolder storm_swords = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(spiral_swords)
                    .display(Items.COBWEB,
                        Component.translatable("adv.slashblade.storm_swords"),
                        Component.translatable("adv.slashblade.storm_swords.desc",
                            Component.translatable("key.pickItem"),
                            Component.keybind("key.pickItem"),
                            Component.keybind("key.back"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/shooting/storm_swords");
            
            AdvancementHolder blisteringSwords = save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(storm_swords)
                    .display(Items.FIREWORK_ROCKET,
                        Component.translatable("adv.slashblade.blistering_swords"),
                        Component.translatable("adv.slashblade.blistering_swords.desc",
                            Component.translatable("key.pickItem"),
                            Component.keybind("key.pickItem"),
                            Component.keybind("key.forward"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/shooting/blistering_swords");
            
            save(saver, existingFileHelper,
                Advancement.Builder.advancement()
                    .parent(blisteringSwords)
                    .display(Items.GHAST_TEAR,
                        Component.translatable("adv.slashblade.heavy_rain_swords"),
                        Component.translatable("adv.slashblade.heavy_rain_swords.desc",
                            Component.translatable("key.pickItem"),
                            Component.keybind("key.pickItem"),
                            Component.keybind("key.back"),
                            Component.keybind("key.forward"),
                            Component.keybind("key.sneak")),
                        null, AdvancementType.TASK, true, true, false)
                    .addCriterion("customtrigger", impossibleTrigger()),
                "arts/shooting/heavy_rain_swords");
        }
        
        public static AdvancementHolder save(Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper, Advancement.Builder builder, String path) {
            return builder.save(saver, SlashBlade.prefix(path), existingFileHelper);
        }
        
        public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item) {
            return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item));
        }
        
        public static Criterion<ImpossibleTrigger.TriggerInstance> impossibleTrigger() {
            return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
        }
        
        public static ResourceKey<LootTable> lootTable(String path) {
            return ResourceKey.create(Registries.LOOT_TABLE, SlashBlade.prefix(path));
        }
    }
}
