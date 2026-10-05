package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.advancement.trigger.ComboStateTrigger;
import mods.flammpfeil.slashblade.advancement.trigger.DoSlashArtsTrigger;
import mods.flammpfeil.slashblade.advancement.trigger.SpecialEffectEffectiveTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SlashBladeCriteriaTriggerRegistry {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, SlashBlade.MODID);
    
    public static final DeferredHolder<CriterionTrigger<?>, ComboStateTrigger> COMBO_STATE = CRITERION_TRIGGERS.register("combo_state", ComboStateTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, DoSlashArtsTrigger> DO_SLASH_ARTS = CRITERION_TRIGGERS.register("do_slash_arts", DoSlashArtsTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SpecialEffectEffectiveTrigger> SPECIAL_EFFECT_EFFECTIVE = CRITERION_TRIGGERS.register("special_effect_effective", SpecialEffectEffectiveTrigger::new);
}
