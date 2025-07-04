package mods.flammpfeil.slashblade.entity;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.util.AttackManager;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.NBTHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static mods.flammpfeil.slashblade.SlashBladeConfig.SLASHBLADE_DAMAGE_MULTIPLIER;

public class EntityAbstractSummonedSword extends Projectile implements IShootable {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLAGS = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HIT_ENTITY_ID = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> OFFSET_YAW = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> PIERCE = SynchedEntityData.defineId(EntityAbstractSummonedSword.class,
            EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> MODEL = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData
            .defineId(EntityAbstractSummonedSword.class, EntityDataSerializers.INT);

    private int ticksInGround;
    private boolean inGround;
    private BlockState inBlockState;
    private int ticksInAir;
    private double damage = 1.0D;

    private IntOpenHashSet alreadyHits;

    private Entity hitEntity = null;

    static final int ON_GROUND_LIFE_TIME = 20 * 5;

    private final SoundEvent hitEntitySound = SoundEvents.TRIDENT_HIT;
    private final SoundEvent hitEntityPlayerSound = SoundEvents.TRIDENT_HIT;
    private final SoundEvent hitGroundSound = SoundEvents.TRIDENT_HIT_GROUND;

    protected SoundEvent getHitEntitySound() {
        return this.hitEntitySound;
    }

    protected SoundEvent getHitEntityPlayerSound() {
        return this.hitEntityPlayerSound;
    }

    protected SoundEvent getHitGroundSound() {
        return this.hitGroundSound;
    }

    public EntityAbstractSummonedSword(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.setNoGravity(true);
        // this.setGlowing(true);
    }

    public static EntityAbstractSummonedSword createInstance(PlayMessages.SpawnEntity packet, Level worldIn) {
        return new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COLOR, 0x3333FF);
        this.entityData.define(FLAGS, 0);
        this.entityData.define(HIT_ENTITY_ID, -1);
        this.entityData.define(OFFSET_YAW, 0f);
        this.entityData.define(ROLL, 0f);
        this.entityData.define(PIERCE, (byte) 0);
        this.entityData.define(MODEL, "");
        this.entityData.define(DELAY, 10);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        NBTHelper.getNBTCoupler(compound).put("Color", this.getColor()).put("life", (short) this.ticksInGround)
                .put("inBlockState", (this.inBlockState != null ? NbtUtils.writeBlockState(this.inBlockState) : null))
                .put("inGround", this.inGround).put("damage", this.damage).put("crit", this.getIsCritical())
                .put("clip", this.isNoClip()).put("PierceLevel", this.getPierce()).put("model", this.getModelName())
                .put("Delay", this.getDelay());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        NBTHelper.getNBTCoupler(compound).get("Color", this::setColor)
                .get("life", ((Integer v) -> this.ticksInGround = v))
                .get("inBlockState",
                        ((CompoundTag v) -> this.inBlockState = NbtUtils
                                .readBlockState(this.level().holderLookup(Registries.BLOCK), v)))
                .get("inGround", ((Boolean v) -> this.inGround = v))
                .get("damage", ((Double v) -> this.damage = v), this.damage).get("crit", this::setIsCritical)
                .get("clip", this::setNoClip).get("PierceLevel", this::setPierce).get("model", this::setModelName)
                .get("Delay", this::setDelay);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec3d = (new Vec3(x, y, z)).normalize()
                .add(this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy,
                        this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy,
                        this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy)
                .scale(velocity);
        this.setDeltaMovement(vec3d);
        float f = Mth.sqrt((float) vec3d.horizontalDistanceSqr());
        this.setPos(this.position());
        this.setYRot((float) (Mth.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vec3d.y, f) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.ticksInGround = 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 10.0D;
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements,
                       boolean teleport) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = Mth.sqrt((float) (x * x + z * z));
            this.setXRot((float) (Mth.atan2(y, f) * (double) (180F / (float) Math.PI)));
            this.setYRot((float) (Mth.atan2(x, z) * (double) (180F / (float) Math.PI)));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            this.ticksInGround = 0;
        }

    }

    public enum FlagsState {
        Critical, NoClip,
    }

    protected EnumSet<FlagsState> flags = EnumSet.noneOf(FlagsState.class);
    protected int intFlags = 0;

    protected void setFlags(FlagsState value) {
        this.flags.add(value);
        refreshFlags();
    }

    protected void removeFlags(FlagsState value) {
        this.flags.remove(value);
        refreshFlags();
    }

    private void refreshFlags() {
        if (this.level().isClientSide()) {
            int newValue = this.entityData.get(FLAGS);
            if (intFlags != newValue) {
                intFlags = newValue;
                flags = EnumSetConverter.convertToEnumSet(FlagsState.class, intFlags);
            }
        } else {
            int newValue = EnumSetConverter.convertToInt(this.flags);
            if (this.intFlags != newValue) {
                this.entityData.set(FLAGS, newValue);
                this.intFlags = newValue;
            }
        }
    }

    public void setIsCritical(boolean value) {
        if (value) {
            setFlags(FlagsState.Critical);
        } else {
            removeFlags(FlagsState.Critical);
        }
    }

    public boolean getIsCritical() {
        refreshFlags();
        return flags.contains(FlagsState.Critical);
    }

    public void setNoClip(boolean value) {
        this.noPhysics = value;
        if (value) {
            setFlags(FlagsState.NoClip);
        } else {
            removeFlags(FlagsState.NoClip);
        }
    }

    // disallowedHitBlock
    public boolean isNoClip() {
        if (!this.level().isClientSide()) {
            return this.noPhysics;
        } else {
            refreshFlags();
            return flags.contains(FlagsState.NoClip);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (getHitEntity() != null) {
            Entity hits = getHitEntity();

            if (!hits.isAlive()) {
                this.burst();
            } else {
                this.setPos(hits.getX(), hits.getY() + hits.getEyeHeight() * 0.5f, hits.getZ());

                int delay = getDelay();
                delay--;
                setDelay(delay);

                if (!this.level().isClientSide() && delay < 0) {
                    this.burst();
                }
            }

            return;
        }

        boolean disallowedHitBlock = this.isNoClip();

        BlockPos blockpos = this.getOnPos();
        BlockState blockstate = this.level().getBlockState(blockpos);
        if (!blockstate.isAir() && !disallowedHitBlock) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                for (AABB axisalignedbb : voxelshape.toAabbs()) {
                    if (axisalignedbb.move(blockpos).contains(new Vec3(this.getX(), this.getY(), this.getZ()))) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (this.inGround && !disallowedHitBlock) {
            if (this.inBlockState != blockstate && this.level().noCollision(this.getBoundingBox().inflate(0.06D))) {
                // block breaked
                this.burst();
            } else if (!this.level().isClientSide()) {
                // onBlock
                this.tryDespawn();
            }
        } else {
            // process pose
            Vec3 motionVec = this.getDeltaMovement();
            if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
                float f = Mth.sqrt((float) motionVec.horizontalDistanceSqr());
                this.setYRot((float) (Mth.atan2(motionVec.x, motionVec.z) * (double) (180F / (float) Math.PI)));
                this.setXRot((float) (Mth.atan2(motionVec.y, f) * (double) (180F / (float) Math.PI)));
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
            }

            // process inAir
            ++this.ticksInAir;
            Vec3 positionVec = this.position();
            Vec3 movedVec = positionVec.add(motionVec);
            HitResult raytraceresult = this.level().clip(
                    new ClipContext(positionVec, movedVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (raytraceresult.getType() != HitResult.Type.MISS) {
                movedVec = raytraceresult.getLocation();
            }

            while (this.isAlive()) {
                // todo : replace TargetSelector
                EntityHitResult entityraytraceresult = this.getRayTrace(positionVec, movedVec);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = null;
                    if (raytraceresult instanceof EntityHitResult) {
                        entity = ((EntityHitResult) raytraceresult).getEntity();
                    }
                    Entity entity1 = this.getShooter();
                    if (entity instanceof LivingEntity && entity1 instanceof LivingEntity) {
                        if (!TargetSelector.test.test((LivingEntity) entity1, (LivingEntity) entity)) {
                            raytraceresult = null;
                            entityraytraceresult = null;
                        }
                    }
                }

                if (raytraceresult != null && !(disallowedHitBlock && raytraceresult.getType() == HitResult.Type.BLOCK)
                        && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.hasImpulse = true;
                }

                if (entityraytraceresult == null || this.getPierce() <= 0) {
                    break;
                }

                raytraceresult = null;
            }

            motionVec = this.getDeltaMovement();
            double mx = motionVec.x;
            double my = motionVec.y;
            double mz = motionVec.z;
            if (this.getIsCritical()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.CRIT, this.getX() + mx * (double) i / 4.0D,
                            this.getY() + my * (double) i / 4.0D, this.getZ() + mz * (double) i / 4.0D, -mx, -my + 0.2D,
                            -mz);
                }
            }

            this.setPos(this.getX() + mx, this.getY() + my, this.getZ() + mz);
            float f4 = Mth.sqrt((float) motionVec.horizontalDistanceSqr());
            if (disallowedHitBlock) {
                this.setYRot((float) (Mth.atan2(-mx, -mz) * (double) (180F / (float) Math.PI)));
            } else {
                this.setYRot((float) (Mth.atan2(mx, mz) * (double) (180F / (float) Math.PI)));
            }

            for (this.setXRot((float) (Mth.atan2(my, f4) * (double) (180F / (float) Math.PI))); this.getXRot()
                    - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
            }

            while (this.getXRot() - this.xRotO >= 180.0F) {
                this.xRotO += 360.0F;
            }

            while (this.getYRot() - this.yRotO < -180.0F) {
                this.yRotO -= 360.0F;
            }

            while (this.getYRot() - this.yRotO >= 180.0F) {
                this.yRotO += 360.0F;
            }

            this.setXRot(Mth.lerp(0.2F, this.xRotO, this.getXRot()));
            this.setYRot(Mth.lerp(0.2F, this.yRotO, this.getYRot()));
            float f1 = 0.99F;
            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - mx * 0.25D, this.getY() - my * 0.25D,
                            this.getZ() - mz * 0.25D, mx, my, mz);
                }
            }

            this.setDeltaMovement(motionVec.scale(f1));
            if (!this.isNoGravity() && !disallowedHitBlock) {
                Vec3 vec3d3 = this.getDeltaMovement();
                this.setDeltaMovement(vec3d3.x, vec3d3.y - (double) 0.05F, vec3d3.z);
            }

            // this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
            this.checkInsideBlocks();
        }

        if (!this.level().isClientSide() && ticksInGround <= 0 && 100 < this.tickCount) {
            this.remove(RemovalReason.DISCARDED);
        }

    }

    protected void tryDespawn() {
        ++this.ticksInGround;
        if (ON_GROUND_LIFE_TIME <= this.ticksInGround) {
            this.burst();
        }

    }

    @Override
    protected void onHit(HitResult raytraceResultIn) {
        HitResult.Type type = raytraceResultIn.getType();
        switch (type) {
            case ENTITY:
                this.onHitEntity((EntityHitResult) raytraceResultIn);
                break;
            case BLOCK:
                this.onHitBlock((BlockHitResult) raytraceResultIn);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockraytraceresult) {
        BlockState blockstate = this.level().getBlockState(blockraytraceresult.getBlockPos());
        this.inBlockState = blockstate;
        Vec3 vec3d = blockraytraceresult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3d);
        Vec3 vec3d1 = this.position().subtract(vec3d.normalize().scale(0.05F));
        this.setPos(vec3d1.x, vec3d1.y, vec3d1.z);
        this.playSound(this.getHitGroundSound(), 1.0F, 2.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;
        this.setIsCritical(false);
        this.setPierce((byte) 0);
        this.resetAlreadyHits();
        blockstate.onProjectileHit(this.level(), blockstate, blockraytraceresult, this);
    }

    public void doForceHitEntity(Entity target) {
        onHitEntity(new EntityHitResult(target));
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity targetEntity = entityHitResult.getEntity();
        int i = Mth.ceil(this.getDamage());
        if (this.getPierce() > 0) {
            if (this.alreadyHits == null) {
                this.alreadyHits = new IntOpenHashSet(5);
            }

            if (this.alreadyHits.size() >= this.getPierce() + 1) {
                this.burst();
                return;
            }

            this.alreadyHits.add(targetEntity.getId());
        }

        if (this.getIsCritical()) {
            i += this.random.nextInt(i / 2 + 2);
        }

        Entity shooter = this.getShooter();
        DamageSource damagesource;
        if (shooter == null) {
            damagesource = this.damageSources().indirectMagic(this, this);
        } else {
            damagesource = this.damageSources().indirectMagic(this, shooter);
            if (shooter instanceof LivingEntity) {
                Entity hits = targetEntity;
                if (targetEntity instanceof PartEntity) {
                    hits = ((PartEntity<?>) targetEntity).getParent();
                }
                ((LivingEntity) shooter).setLastHurtMob(hits);
            }
        }

        int fireTime = targetEntity.getRemainingFireTicks();
        if (this.isOnFire() && !(targetEntity instanceof EnderMan)) {
            targetEntity.setSecondsOnFire(5);
        }

        // todo: attack manager
        targetEntity.invulnerableTime = 0;
        float scale = 1f;
        if (shooter instanceof LivingEntity living) {
            scale = (float) (AttackManager.getSlashBladeDamageScale(living) * SLASHBLADE_DAMAGE_MULTIPLIER.get());
        }
        float damageValue = i * scale;
        if (targetEntity.hurt(damagesource, damageValue)) {
            Entity hits = targetEntity;
            if (targetEntity instanceof PartEntity) {
                hits = ((PartEntity<?>) targetEntity).getParent();
            }

            if (hits instanceof LivingEntity targetLivingEntity) {

                StunManager.setStun(targetLivingEntity);

                if (!this.level().isClientSide() && this.getPierce() <= 0) {
                    setHitEntity(hits);
                }

                if (!this.level().isClientSide() && shooter instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(targetLivingEntity, shooter);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) shooter, targetLivingEntity);
                }

                // this.arrowHit(targetLivingEntity);

                affectEntity(targetLivingEntity, getPotionEffects(), 1.0f);

                if (targetLivingEntity != shooter && targetLivingEntity instanceof Player
                        && shooter instanceof ServerPlayer) {
                    ((ServerPlayer) shooter).playNotifySound(this.getHitEntityPlayerSound(), SoundSource.PLAYERS, 0.18F,
                            0.45F);
                }
            }

            this.playSound(this.getHitEntitySound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierce() <= 0 && (getHitEntity() == null || !getHitEntity().isAlive())) {
                this.burst();
            }
        } else {
            targetEntity.setRemainingFireTicks(fireTime);
            // this.setMotion(this.getMotion().scale(-0.1D));
            // this.setYRot(this.getYRot() + 180.0F);
            // this.yRotO += 180.0F;
            this.ticksInAir = 0;
            if (!this.level().isClientSide() && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (getPierce() <= 1) {
                    this.burst();
                } else {
                    this.setPierce((byte) (getPierce() - 1));
                }
            }
        }

    }

    public int getColor() {
        return this.getEntityData().get(COLOR);
    }

    public void setColor(int value) {
        this.getEntityData().set(COLOR, value);
    }

    public byte getPierce() {
        return this.getEntityData().get(PIERCE);
    }

    public void setPierce(byte value) {
        this.getEntityData().set(PIERCE, value);
    }

    public int getDelay() {
        return this.getEntityData().get(DELAY);
    }

    public void setDelay(int value) {
        this.getEntityData().set(DELAY, value);
    }

    @Nullable
    protected EntityHitResult getRayTrace(Vec3 p_213866_1_, Vec3 p_213866_2_) {
        return ProjectileUtil.getEntityHitResult(this.level(), this, p_213866_1_, p_213866_2_,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (entity) -> !entity.isSpectator() && entity.canBeHitByProjectile()
                        && (entity != this.getShooter() || this.ticksInAir >= 5)
                        && (this.alreadyHits == null || !this.alreadyHits.contains(entity.getId())));
    }

    @Nullable
    @Override
    public Entity getShooter() {
        return this.getOwner();
    }

    @Override
    public void setShooter(Entity shooter) {
        setOwner(shooter);
    }

    public List<MobEffectInstance> getPotionEffects() {
        List<MobEffectInstance> effects = PotionUtils.getAllEffects(this.getPersistentData());

        if (effects.isEmpty()) {
            effects.add(new MobEffectInstance(MobEffects.POISON, 1, 1));
        }

        return effects;
    }

    public void burst() {
        this.playSound(SoundEvents.GLASS_BREAK, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                        16, 0.5, 0.5, 0.5, 0.25f);
            }

            this.burst(getPotionEffects(), null);
        }

        super.remove(RemovalReason.DISCARDED);
    }

    public void burst(List<MobEffectInstance> effects, @Nullable Entity focusEntity) {
        // AABB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<Entity> list = TargetSelector.getTargettableEntitiesWithinAABB(this.level(), 2, this);
        // this.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb);

        list.stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).forEach(e -> {
            double distanceSq = this.distanceToSqr(e);
            if (distanceSq < 9.0D) {
                double factor = 1.0D - Math.sqrt(distanceSq) / 4.0D;
                if (e == focusEntity) {
                    factor = 1.0D;
                }

                affectEntity(e, effects, factor);
            }
        });
    }

    public void affectEntity(LivingEntity focusEntity, List<MobEffectInstance> effects, double factor) {
        for (MobEffectInstance effectinstance : getPotionEffects()) {
            MobEffect effect = effectinstance.getEffect();
            if (effect.isInstantenous()) {
                effect.applyInstantenousEffect(this, this.getShooter(), focusEntity, effectinstance.getAmplifier(),
                        factor);
            } else {
                int duration = (int) (factor * (double) effectinstance.getDuration() + 0.5D);
                if (duration > 0) {
                    focusEntity.addEffect(new MobEffectInstance(effect, duration, effectinstance.getAmplifier(),
                            effectinstance.isAmbient(), effectinstance.isVisible()));
                }
            }
        }
    }

    public void resetAlreadyHits() {
        if (this.alreadyHits != null) {
            alreadyHits.clear();
        }
    }

    public void setHitEntity(Entity hitEntity) {
        if (hitEntity != this) {
            this.entityData.set(HIT_ENTITY_ID, hitEntity.getId());

            this.entityData.set(OFFSET_YAW, this.random.nextFloat() * 360);

            this.setDelay(20 * 5);
        }
    }

    @Nullable
    public Entity getHitEntity() {
        if (hitEntity == null) {
            int id = this.entityData.get(HIT_ENTITY_ID);
            if (0 <= id) {
                hitEntity = this.level().getEntity(id);
            }
        }
        return hitEntity;
    }

    public float getOffsetYaw() {
        return this.entityData.get(OFFSET_YAW);
    }

    public float getRoll() {
        return this.entityData.get(ROLL);
    }

    public void setRoll(float value) {
        this.entityData.set(ROLL, value);
    }

    public void setDamage(double damageIn) {
        this.damage = damageIn;
    }

    @Override
    public double getDamage() {
        return this.damage;
    }

    private static final String defaultModelName = "slashblade:model/util/ss";

    public void setModelName(String name) {
        this.entityData.set(MODEL, Optional.ofNullable(name).orElse(defaultModelName));
    }

    public String getModelName() {
        String name = this.entityData.get(MODEL);
        if (name.isEmpty()) {
            name = defaultModelName;
        }
        return name;
    }

    private static final ResourceLocation defaultModel = new ResourceLocation(defaultModelName + ".obj");
    public LazyOptional<ResourceLocation> modelLoc = LazyOptional
            .of(() -> new ResourceLocation(getModelName() + ".obj"));
    private static final ResourceLocation defaultTexture = new ResourceLocation(defaultModelName + ".png");
    public LazyOptional<ResourceLocation> textureLoc = LazyOptional
            .of(() -> new ResourceLocation(getModelName() + ".png"));

    public ResourceLocation getModelLoc() {
        return modelLoc.orElse(defaultModel);
    }

    public ResourceLocation getTextureLoc() {
        return textureLoc.orElse(defaultTexture);
    }

    @Override
    public void push(@NotNull Entity entityIn) {
        // Suppress velocity change due to collision
        // super.applyEntityCollision(entityIn);
    }

    // todo: 射撃攻撃との相殺 pierce値＝HPにした近接攻撃との相殺
}
