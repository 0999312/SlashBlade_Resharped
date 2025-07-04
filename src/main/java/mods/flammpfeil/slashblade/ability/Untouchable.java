package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class Untouchable {
    private static final class SingletonHolder {
        private static final Untouchable instance = new Untouchable();
    }

    public static Untouchable getInstance() {
        return Untouchable.SingletonHolder.instance;
    }

    private Untouchable() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void setUntouchable(LivingEntity entity, int ticks) {
        entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(ef -> {
            ef.setManagedUntouchable(entity.level().getGameTime(), ticks);
            ef.storeEffects(entity.getActiveEffectsMap().keySet());
            ef.storeHealth(entity.getHealth());
        });
    }

    private boolean checkUntouchable(LivingEntity entity) {
        Optional<Boolean> isUntouchable = entity.getCapability(CapabilityMobEffect.MOB_EFFECT)
                .map(ef -> ef.isUntouchable(entity.getCommandSenderWorld().getGameTime()));

        return isUntouchable.orElse(false);
    }

    private void doWitchTime(Entity entity) {
        if (entity == null) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        StunManager.setStun((LivingEntity) entity);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (checkUntouchable(event.getEntity())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getEntity());
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (checkUntouchable(event.getEntity())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getEntity());
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (checkUntouchable(event.getEntity())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getEntity());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (checkUntouchable(event.getEntity())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getEntity());

            LivingEntity entity = event.getEntity();

            entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(ef -> {
                if (ef.hasUntouchableWorked()) {
                    List<MobEffect> filterd = entity.getActiveEffectsMap().keySet().stream()
                            .filter(p -> !(ef.getEffectSet().contains(p) || p.isBeneficial())).toList();

                    filterd.forEach(entity::removeEffect);

                    float storedHealth = ef.getStoredHealth();
                    if (ef.getStoredHealth() < storedHealth) {
                        entity.setHealth(ef.getStoredHealth());
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onLivingTicks(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(ef -> {
            if (ef.hasUntouchableWorked()) {
                ef.setUntouchableWorked(false);
                List<MobEffect> filterd = entity.getActiveEffectsMap().keySet().stream()
                        .filter(p -> !(ef.getEffectSet().contains(p) || p.isBeneficial())).toList();

                filterd.forEach(entity::removeEffect);

                float storedHealth = ef.getStoredHealth();
                if (ef.getStoredHealth() < storedHealth) {
                    entity.setHealth(ef.getStoredHealth());
                }
            }
        });
    }

    final static int JUMP_TICKS = 10;

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (!event.getEntity().getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return;
        }

        Untouchable.setUntouchable(event.getEntity(), JUMP_TICKS);
    }
}
