package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.util.KnockBacks;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

public class EntityStormSwords extends EntityAbstractSummonedSword {
    private static final EntityDataAccessor<Boolean> IT_FIRED = SynchedEntityData.defineId(EntityStormSwords.class,
            EntityDataSerializers.BOOLEAN);

    public EntityStormSwords(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);

        this.setPierce((byte) 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(IT_FIRED, false);
    }

    public void doFire() {
        this.getEntityData().set(IT_FIRED, true);
    }

    public boolean itFired() {
        return this.getEntityData().get(IT_FIRED);
    }

    public static EntityStormSwords createInstance(PlayMessages.SpawnEntity packet, Level worldIn) {
        return new EntityStormSwords(SlashBlade.RegistryEvents.StormSwords, worldIn);
    }

    @Override
    public void rideTick() {
        if (itFired()) {
            faceEntityStandby();
            Entity target = getVehicle();
            this.stopRiding();

            this.tickCount = 0;
            Vec3 dir = this.getViewVector(1.0f);
            if (target != null) {
                dir = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
            }
            this.shoot(dir.x, dir.y, dir.z, 3.0f, 1.0f);
            return;
        }

        // this.startRiding()
        this.setDeltaMovement(Vec3.ZERO);
        if (canUpdate()) {
            this.baseTick();
        }

        faceEntityStandby();
        // this.getVehicle().positionRider(this);

        // todo: add lifetime
        if (20 <= this.tickCount) {
            doFire();
        }

        if (!level().isClientSide()) {
            hitCheck();
        }
    }

    private void hitCheck() {
        Vec3 positionVec = this.position();
        Vec3 dirVec = this.getViewVector(1.0f);
        EntityHitResult raytraceresult = null;

        // todo : replace TargetSelector
        EntityHitResult entityraytraceresult = this.getRayTrace(positionVec, dirVec);
        if (entityraytraceresult != null) {
            raytraceresult = entityraytraceresult;
        }

        if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
            Entity entity = raytraceresult.getEntity();
            Entity entity1 = this.getShooter();
            if (entity instanceof Player && entity1 instanceof Player
                    && !((Player) entity1).canHarmPlayer((Player) entity)) {
                raytraceresult = null;
            }
        }

        if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            this.onHit(raytraceresult);
            this.resetAlreadyHits();
            this.hasImpulse = true;
        }
    }

    private void faceEntityStandby() {

        long cycle = 5 + this.tickCount;
        long tickOffset = 0;
        if (this.level().isClientSide()) {
            tickOffset = 1;
        }

        // int ticks = (int)((this.level().getGameTime() + tickOffset) % cycle);
        int ticks = (int) ((this.tickCount + tickOffset) % cycle);

        /*
         * if ((getInterval() - waitTime) < ticks) { ticks = getInterval() - waitTime; }
         */

        double rotParTick = 360.0 / (double) cycle;
        double offset = getDelay();
        double degYaw = (ticks * rotParTick + offset) % 360.0;
        double yaw = Math.toRadians(degYaw);

        Vec3 dir = new Vec3(0, 0, 1);

        // yaw
        dir = dir.yRot((float) -yaw);
        dir = dir.normalize().scale(4);

        if (this.getVehicle() != null) {
            dir = dir.add(this.getVehicle().position());
            dir = dir.add(0, this.getVehicle().getEyeHeight() / 2.0, 0);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();

        setPos(dir);

        setRot((float) (-degYaw) - 180, 0);

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {

        Entity targetEntity = entityHitResult.getEntity();
        if (targetEntity instanceof LivingEntity) {
            KnockBacks.toss.action.accept((LivingEntity) targetEntity);
            StunManager.setStun((LivingEntity) targetEntity);
        }

        super.onHitEntity(entityHitResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockraytraceresult) {
        burst();
    }
}
