package mods.flammpfeil.slashblade.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class Projectile extends net.minecraft.world.entity.projectile.Projectile {
    private static final EntityDataAccessor<Integer> OWNERID = SynchedEntityData.defineId(Projectile.class,
            EntityDataSerializers.INT);

    protected Projectile(EntityType<? extends net.minecraft.world.entity.projectile.Projectile> p_37248_,
                         Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNERID, -1);
    }

    @Nullable
    @Override
    public Entity getOwner() {
        int id = this.entityData.get(OWNERID);

        if (0 <= id) {
            Entity tmp = this.level().getEntity(id);
            if (super.getOwner() != tmp) {
                this.setOwner(tmp);
            }
        } else {
            this.setOwner(null);
        }

        return super.getOwner();
    }

    @Override
    public void setOwner(@Nullable Entity p_37263_) {
        if (p_37263_ != null) {
            this.entityData.set(OWNERID, p_37263_.getId());
        } else {
            this.entityData.set(OWNERID, -1);
        }

        super.setOwner(p_37263_);
    }

    @Override
    public boolean fireImmune() {
        return true;//防火
    }
}
