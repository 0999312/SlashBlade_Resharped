package mods.flammpfeil.slashblade.util;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

import mods.flammpfeil.slashblade.event.handler.KnockBackHandler;

public enum KnockBacks {
    cancel((e) -> KnockBackHandler.setFactor(e, 0, 0, 0)), toss((e) -> KnockBackHandler.setVertical(e, 0.75f)),
    meteor((e) -> KnockBackHandler.setVertical(e, -5.5f)), smash((e) -> KnockBackHandler.setSmash(e, 1.5f)),
    ;

    public final Consumer<LivingEntity> action;

    KnockBacks(Consumer<LivingEntity> action) {
        this.action = action;
    }
}
