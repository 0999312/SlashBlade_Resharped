package mods.flammpfeil.slashblade.slasharts;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.EntityDrive;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.KnockBacks;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class Drive {
    public static EntityDrive doSlash(LivingEntity playerIn, float roll, int lifetime, Vec3 centerOffset,
                                      boolean critical, double damage, float speed) {
        return doSlash(playerIn, roll, lifetime, centerOffset, critical, damage, KnockBacks.cancel, speed);
    }

    public static EntityDrive doSlash(LivingEntity playerIn, float roll, int lifetime, Vec3 centerOffset,
                                      boolean critical, double damage, KnockBacks knockback, float speed) {

        int colorCode = playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                .map(ISlashBladeState::getColorCode).orElse(0xFF3333FF);

        return doSlash(playerIn, roll, lifetime, colorCode, centerOffset, critical, damage, knockback, speed);
    }

    public static EntityDrive doSlash(LivingEntity playerIn, float roll, int lifetime, int colorCode, Vec3 centerOffset,
                                      boolean critical, double damage, KnockBacks knockback, float speed) {

        if (playerIn.level().isClientSide()) {
            return null;
        }

        Vec3 lookAngle = playerIn.getLookAngle();
        Vec3 pos = playerIn.position().add(0.0D, (double) playerIn.getEyeHeight() * 0.75D, 0.0D)
                .add(lookAngle.scale(0.3f));

        pos = pos.add(VectorHelper.getVectorForRotation(-90.0F, playerIn.getViewYRot(0)).scale(centerOffset.y))
                .add(VectorHelper.getVectorForRotation(0, playerIn.getViewYRot(0) + 90).scale(centerOffset.z))
                .add(lookAngle.scale(centerOffset.z));
        EntityDrive drive = new EntityDrive(SlashBlade.RegistryEvents.Drive, playerIn.level());

        drive.setPos(pos.x, pos.y, pos.z);
        drive.setDamage(damage);
        drive.setSpeed(speed);
        drive.shoot(lookAngle.x, lookAngle.y, lookAngle.z, drive.getSpeed(),
                0);

        drive.setOwner(playerIn);
        drive.setRotationRoll(roll);

        drive.setColor(colorCode);
        drive.setIsCritical(critical);
        drive.setKnockBack(knockback);

        drive.setLifetime(lifetime);

        playerIn.getCapability(ConcentrationRankCapabilityProvider.RANK_POINT)
                .ifPresent(rank -> drive.setRank(rank.getRankLevel(playerIn.level().getGameTime())));

        playerIn.level().addFreshEntity(drive);


        return drive;
    }
}
