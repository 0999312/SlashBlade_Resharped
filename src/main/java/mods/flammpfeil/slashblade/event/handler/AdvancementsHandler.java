package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.SlashBladeCriteriaTriggerRegistry;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class AdvancementsHandler {
    @SubscribeEvent
    public static void onSpecialEffectEffectiveCheck(SlashBladeEvent.UpdateEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            event.getSlashBladeState().getSpecialEffects().forEach(se -> {
                if (SpecialEffect.isEffective(se, serverPlayer)) {
                    SlashBladeCriteriaTriggerRegistry.SPECIAL_EFFECT_EFFECTIVE.get().trigger(serverPlayer, event.getBlade(), se);
                }
            });
        }
    }
}
