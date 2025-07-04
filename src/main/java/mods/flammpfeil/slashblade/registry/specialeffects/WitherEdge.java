package mods.flammpfeil.slashblade.registry.specialeffects;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class WitherEdge extends SpecialEffect {

    public WitherEdge() {
        super(20, true, true);
    }

    @SubscribeEvent
    public static void onSlashBladeUpdate(SlashBladeEvent.UpdateEvent event) {
        ISlashBladeState state = event.getSlashBladeState();
        if (state.hasSpecialEffect(SpecialEffectsRegistry.WITHER_EDGE.getId())) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            if (!event.isSelected()) {
                return;
            }

            int level = player.experienceLevel;

            if (!SpecialEffect.isEffective(SpecialEffectsRegistry.WITHER_EDGE.get(), level)) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        }
    }

    @SubscribeEvent
    public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
        ISlashBladeState state = event.getSlashBladeState();
        if (state.hasSpecialEffect(SpecialEffectsRegistry.WITHER_EDGE.getId())) {
            if (!(event.getUser() instanceof Player player)) {
                return;
            }

            int level = player.experienceLevel;

            if (SpecialEffect.isEffective(SpecialEffectsRegistry.WITHER_EDGE.get(), level)) {
                event.getTarget().addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        }
    }
}
