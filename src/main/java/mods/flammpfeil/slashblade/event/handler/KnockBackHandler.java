package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

public class KnockBackHandler {

    static final String NBT_KEY = "knockback_factor";

    static public void setCancel(LivingEntity target) {
        setFactor(target, 0, 0, 0);
    }

    static public void setSmash(LivingEntity target, double horizontalFactor) {
        setFactor(target, horizontalFactor, 0, 0);
    }

    static public void setVertical(LivingEntity target, double verticalFactor) {
        setFactor(target, 0, verticalFactor, -verticalFactor);
    }

    static public void setFactor(LivingEntity target, double horizontalFactor, double verticalFactor,
                                 double addFallDistance) {
        NBTHelper.putVector3d(target.getPersistentData(), NBT_KEY,
                new Vec3(horizontalFactor, verticalFactor, addFallDistance));
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();

        CompoundTag nbt = target.getPersistentData();

        if (!nbt.contains(NBT_KEY)) {
            return;
        }

        Vec3 factor = NBTHelper.getVector3d(nbt, NBT_KEY);
        nbt.remove(NBT_KEY);

        if (target.fallDistance < 0) {
            target.fallDistance = 0;
        }

        // z = falldistance factor
        target.fallDistance += (float) factor.z;

        // movement factor is resistable
        if ((target.getRandom().nextDouble() < Objects.requireNonNull(target.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).getValue())) {
            return;
        }

        target.hasImpulse = true;

        Vec3 motion = target.getDeltaMovement();

        // x = strength multiplier
        if (factor.x == 0) {
            event.setCanceled(true);

            motion = motion.multiply(0, 1, 0);
        } else {
            event.setStrength((float) (event.getStrength() * factor.x));
        }

        // y = vertical factor

        if (0 < factor.y) {
            target.setOnGround(false);
            event.getEntity().setDeltaMovement(motion.x, Math.max(motion.y, factor.y), motion.z);
        } else if (factor.y < 0) {
            event.getEntity().setDeltaMovement(motion.x, Math.min(motion.y, factor.y), motion.z);
        }
    }
}
