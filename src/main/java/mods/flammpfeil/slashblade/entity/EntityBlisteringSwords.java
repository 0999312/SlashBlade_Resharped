package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.capability.inputstate.InputStateCapabilityProvider;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.KnockBacks;
import mods.flammpfeil.slashblade.util.RayTraceHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

import java.util.Optional;
import java.util.stream.Stream;

public class EntityBlisteringSwords extends EntityAbstractSummonedSword {
    private static final EntityDataAccessor<Boolean> IT_FIRED = SynchedEntityData.defineId(EntityBlisteringSwords.class,
            EntityDataSerializers.BOOLEAN);

    public EntityBlisteringSwords(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);

        this.setPierce((byte) 5);
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

    public static EntityBlisteringSwords createInstance(PlayMessages.SpawnEntity packet, Level worldIn) {
        return new EntityBlisteringSwords(SlashBlade.RegistryEvents.BlisteringSwords, worldIn);
    }

    long fireTime = -1;

    @Override
    public void tick() {
        if (!itFired()) {
            if (getVehicle() == null && this.getOwner() != null) {
                startRiding(this.getOwner(), true);
            }
        }

        super.tick();
    }

    @Override
    public void rideTick() {
        if (itFired() && fireTime <= tickCount) {
            faceEntityStandby();
            Entity vehicle = getVehicle();
            Vec3 dir = this.getViewVector(0);
            if (!(vehicle instanceof LivingEntity sender)) {
                this.shoot(dir.x, dir.y, dir.z, 3.0f, 1.0f);
                return;
            }

            this.stopRiding();

            this.tickCount = 0;

            Level worldIn;
            worldIn = sender.level();
            Entity lockTarget = null;
            if (sender instanceof LivingEntity) {
                lockTarget = sender.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                        .filter(state -> state.getTargetEntity(worldIn) != null)
                        .map(state -> state.getTargetEntity(worldIn)).orElse(null);
            }

            Optional<Entity> foundTarget;
            foundTarget = Stream
                    .of(Optional.ofNullable(lockTarget),
                            RayTraceHelper
                                    .rayTrace(sender.level(), sender, sender.getEyePosition(1.0f),
                                            sender.getLookAngle(), 12, 12, (e) -> true)
                                    .filter(r -> r.getType() == HitResult.Type.ENTITY).filter(r -> {
                                        EntityHitResult er = (EntityHitResult) r;
                                        Entity target = er.getEntity();

                                        boolean isMatch = true;
                                        if (target instanceof LivingEntity) {
                                            isMatch = TargetSelector.test.test(sender, (LivingEntity) target);
                                        }

                                        if (target instanceof IShootable) {
                                            isMatch = ((IShootable) target).getShooter() != sender;
                                        }

                                        return isMatch;
                                    }).map(r -> ((EntityHitResult) r).getEntity()))
                    .filter(Optional::isPresent).map(Optional::get).findFirst();

            Vec3 targetPos = foundTarget.map((e) -> new Vec3(e.getX(), e.getY() + e.getEyeHeight() * 0.5, e.getZ()))
                    .orElseGet(() -> {
                        Vec3 start = sender.getEyePosition(1.0f);
                        Vec3 end = start.add(sender.getLookAngle().scale(40));
                        HitResult result;
                        result = worldIn.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE, sender));
                        return result.getLocation();
                    });

            Vec3 pos = this.getPosition(0.0f);
            dir = targetPos.subtract(pos).normalize();

            this.shoot(dir.x, dir.y, dir.z, 3.0f, 1.0f);
            if (sender instanceof ServerPlayer) {
                ((ServerPlayer) sender).playNotifySound(SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return;
        }

        // this.startRiding()
        this.setDeltaMovement(Vec3.ZERO);
        if (canUpdate()) {
            this.baseTick();
        }

        faceEntityStandby();
        // this.getVehicle().positionRider(this);

        // lifetime check
        if (!itFired() && getVehicle() instanceof LivingEntity owner) {
            owner.getCapability(InputStateCapabilityProvider.INPUT_STATE).ifPresent(s -> {
                if (!s.getCommands().contains(InputCommand.M_DOWN)) {

                    fireTime = tickCount + getDelay();
                    doFire();
                }
            });
        }

        /*
         * if(!level().isClientSide()) hitCheck();
         */
    }

    private void faceEntityStandby() {
        int spawnNum = getDelay();

        boolean isRight = spawnNum % 2 == 0;
        int level = spawnNum / 2;

        Vec3 pos = new Vec3(0, 0, 0);

        if (this.getVehicle() == null) {
            doFire();
            return;
        }

        pos = pos.add(this.getVehicle().position()).add(0, this.getVehicle().getEyeHeight() * 0.8, 0);

        double xOffset = (1 - 0.1 * level) * (isRight ? 1 : -1);
        double yOffset = 0.25 * level;
        double zOffset = -0.1 * level;

        Vec3 offset = new Vec3(xOffset, yOffset, zOffset);

        offset = offset.xRot((float) Math.toRadians(-this.getVehicle().getXRot()));
        offset = offset.yRot((float) Math.toRadians(-this.getVehicle().getYRot()));

        pos = pos.add(offset);

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();

        // ■初期位置・初期角度等の設定
        setPos(pos);

        setRot(-this.getVehicle().getYRot(), -this.getVehicle().getXRot());

    }

    @Override
    protected void onHitBlock(BlockHitResult blockraytraceresult) {
        burst();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {

        Entity targetEntity = entityHitResult.getEntity();
        if (targetEntity instanceof LivingEntity) {
            KnockBacks.cancel.action.accept((LivingEntity) targetEntity);
            StunManager.setStun((LivingEntity) targetEntity);
        }

        super.onHitEntity(entityHitResult);
    }
}
