package mods.flammpfeil.slashblade.capability.mobeffect;

import net.minecraft.world.effect.MobEffect;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface IMobEffectState {
    default void setManagedStun(long now, long duration) {
        if (duration <= 0) {
            return;
        }

        long solvedDuration = Math.min(duration, this.getStunLimit());
        long timeout = now + solvedDuration;
        if (this.getStunTimeOut() < timeout) {
            this.setStunTimeOut(timeout);
        }
    }

    void setStunTimeOut(long timeout);

    default void clearStunTimeOut() {
        setStunTimeOut(-1);
    }

    long getStunTimeOut();

    default boolean isStun(long now) {
        return isStun(now, false);
    }

    default boolean isStun(long now, boolean isVirtual) {

        long timeout = getStunTimeOut();

        // not stun
        if (timeout <= 0) {
            return false;
        }

        // timeout
        timeout = timeout - now;
        if (timeout <= 0 || getStunLimit() < timeout) {
            if (!isVirtual) {
                clearStunTimeOut();
            }
            return false;
        }

        // it is in Effect
        return true;
    }

    int getStunLimit();

    void setStunLimit(int limit);

    int getUntouchableLimit();

    void setUntouchableLimit(int limit);

    default void setManagedUntouchable(long now, long duration) {
        if (duration <= 0) {
            return;
        }

        long solvedDuration = Math.min(duration, this.getUntouchableLimit());
        long timeout = now + solvedDuration;
        if (this.getUntouchableTimeOut().isEmpty() || this.getUntouchableTimeOut().get() < timeout) {
            this.setUntouchableTimeOut(Optional.of(timeout));
        }
    }

    void setUntouchableTimeOut(Optional<Long> timeout);

    default void clearUntouchableTimeOut(boolean isVirtual) {
        if (!isVirtual) {
            setUntouchableTimeOut(Optional.empty());
        }
    }

    Optional<Long> getUntouchableTimeOut();

    default boolean isUntouchable(long now) {
        return isUntouchable(now, false);
    }

    default boolean isUntouchable(long now, boolean isVirtual) {
        return getUntouchableTimeOut().filter(timeout -> now < timeout).map(t -> {
            setUntouchableWorked();
            return true;
        }).orElseGet(() -> {
            this.clearUntouchableTimeOut(isVirtual);
            return false;
        });
    }

    Set<MobEffect> getEffectSet();

    default void storeEffects(Collection<MobEffect> effects) {
        this.getEffectSet().clear();
        this.getEffectSet().addAll(effects);
    }

    boolean hasUntouchableWorked();

    void setUntouchableWorked(boolean value);

    default void setUntouchableWorked() {
        setUntouchableWorked(true);
    }

    float getStoredHealth();

    void storeHealth(float health);

    Optional<Long> getAvoidCooldown();

    int getAvoidCount();

    void setAvoidCooldown(Optional<Long> time);

    void setAvoidCount(int value);

    int AVOID_MAX = 3;
    int COOLDOWN_TICKS = 20;

    default boolean checkCanAvoid(long now) {
        if (getAvoidCount() < AVOID_MAX) {
            return true;
        }

        return getAvoidCooldown().filter(ct -> now < ct).isEmpty();
    }

    default int doAvoid(long now) {
        if (!checkCanAvoid(now)) {
            return 0;
        }

        return getAvoidCooldown().filter(ct -> now < ct).map(ct -> {
            int count = getAvoidCount() + 1;
            setAvoidCount(count);
            return count;
        }).orElseGet(() -> {
            setAvoidCount(1);
            setAvoidCooldown(Optional.of(now + COOLDOWN_TICKS));
            return 1;
        });
    }
}
