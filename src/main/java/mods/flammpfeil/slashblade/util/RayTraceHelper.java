package mods.flammpfeil.slashblade.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class RayTraceHelper {

    public static Optional<HitResult> rayTrace(Level worldIn, Entity entityIn, Vec3 start, Vec3 dir, double blockReach,
                                               double entityReach, Predicate<Entity> selector) {
        Vec3 end = start.add(dir.scale(blockReach));

        HitResult raytraceresult = worldIn
                .clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entityIn));
        if (raytraceresult.getType() != HitResult.Type.MISS) {
            end = raytraceresult.getLocation();
            entityReach = start.distanceTo(end);
        } else {
            end = start.add(dir.scale(entityReach));
        }

        AABB area = entityIn.getBoundingBox().expandTowards(dir.scale(entityReach)).inflate(1.0D);
        EntityHitResult entityraytraceresult = rayTrace(worldIn, entityIn, start, end, area, selector);

        if (entityraytraceresult != null) {
            raytraceresult = entityraytraceresult;
        }

        return Optional.of(raytraceresult);
    }

    @Nullable
    public static EntityHitResult rayTrace(Level worldIn, Entity entityIn, Vec3 start, Vec3 end, AABB boundingBox,
                                           Predicate<Entity> selector) {
        return rayTrace(worldIn, entityIn, start, end, boundingBox, selector, Double.MAX_VALUE);
    }

    @Nullable
    public static EntityHitResult rayTrace(Level worldIn, Entity entityIn, Vec3 start, Vec3 end, AABB boundingBox,
                                           Predicate<Entity> selector, double limitDist) {
        double currentDist = limitDist;
        Entity resultEntity = null;

        for (Entity foundEntity : worldIn.getEntities(entityIn, boundingBox, selector)) {
            AABB axisalignedbb = foundEntity.getBoundingBox().inflate(0.5F);
            Optional<Vec3> optional = axisalignedbb.clip(start, end);
            if (optional.isPresent()) {
                double newDist = start.distanceToSqr(optional.get());
                if (newDist < currentDist) {
                    resultEntity = foundEntity;
                    currentDist = newDist;
                }
            }
        }

        if (resultEntity == null) {
            return null;
        } else {
            return new EntityHitResult(resultEntity);
        }
    }
}
