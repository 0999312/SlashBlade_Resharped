package mods.flammpfeil.slashblade.slasharts;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.KnockBacks;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class CircleSlash {
    public static void doCircleSlashAttack(LivingEntity living, float yRot) {
        if (living.level().isClientSide())
            return;

        Vec3 pos = living.position().add(0.0D, (double) living.getEyeHeight() * 0.75D, 0.0D)
                .add(living.getLookAngle().scale(0.3f));

        pos = pos.add(VectorHelper.getVectorForRotation(-90.0F, living.getViewYRot(0)).scale(Vec3.ZERO.y))
                .add(VectorHelper.getVectorForRotation(0, living.getViewYRot(0) + 90).scale(Vec3.ZERO.z))
                .add(living.getLookAngle().scale(Vec3.ZERO.z));

        EntitySlashEffect jc = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, living.level()) {

            @Override
            public SoundEvent getSlashSound() {
                return SoundEvents.EMPTY;
            }
        };
        jc.setPos(pos.x, pos.y, pos.z);
        jc.setOwner(living);

        jc.setRotationRoll(0);
        jc.setYRot(living.getYRot() - 22.5F + yRot);
        jc.setXRot(0);

        int colorCode = living.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                .map(state -> state.getColorCode()).orElseGet(() -> 0xFFFFFF);
        jc.setColor(colorCode);

        jc.setMute(false);
        jc.setIsCritical(true);

        jc.setDamage(0.325D);

        jc.setKnockBack(KnockBacks.cancel);

        if (living != null)
            living.getCapability(ConcentrationRankCapabilityProvider.RANK_POINT)
                    .ifPresent(rank -> jc.setRank(rank.getRankLevel(living.level().getGameTime())));

        living.level().addFreshEntity(jc);
    }

}
