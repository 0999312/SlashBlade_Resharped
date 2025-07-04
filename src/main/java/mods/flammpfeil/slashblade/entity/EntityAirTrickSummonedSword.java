package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.SlayerStyleArts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.PlayMessages;

public class EntityAirTrickSummonedSword extends EntityAbstractSummonedSword {
    private Entity target;
    private boolean shouldUntouchable;

    public EntityAirTrickSummonedSword(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public static EntityAirTrickSummonedSword createInstance(PlayMessages.SpawnEntity packet, Level worldIn) {
        return new EntityAirTrickSummonedSword(SlashBlade.RegistryEvents.AirTrickSummonedSword, worldIn);
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public boolean isShouldUntouchable() {
        return shouldUntouchable;
    }

    public void setShouldUntouchable(boolean shouldUntouchable) {
        this.shouldUntouchable = shouldUntouchable;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (getOwner() instanceof LivingEntity living) {
            LivingEntity target = living.getLastHurtMob();
            if (target != null && this.getHitEntity() == target) {
                SlayerStyleArts.doTeleport(living, target);
            }
        }
    }

    @Override
    public void tick() {
        if (this.target != null && this.getPersistentData().getBoolean("doForceHit")) {
            this.doForceHitEntity(this.target);
            this.getPersistentData().remove("doForceHit");
        }
        super.tick();
    }
}
