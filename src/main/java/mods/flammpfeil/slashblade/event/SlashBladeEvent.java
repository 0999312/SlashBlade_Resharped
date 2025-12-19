package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import mods.flammpfeil.slashblade.util.KnockBacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class SlashBladeEvent extends Event {
    private final ItemStack blade;
    private final ISlashBladeState state;

    public SlashBladeEvent(ItemStack blade, ISlashBladeState state) {
        this.blade = blade;
        this.state = state;
    }

    public ItemStack getBlade() {
        return blade;
    }

    public ISlashBladeState getSlashBladeState() {
        return state;
    }

    @Cancelable
    public static class BreakEvent extends SlashBladeEvent {
        public BreakEvent(ItemStack blade, ISlashBladeState state) {
            super(blade, state);
        }
    }

    public static class PowerBladeEvent extends SlashBladeEvent {
        private final LivingEntity user;
        private boolean isPowered;

        public PowerBladeEvent(ItemStack blade, ISlashBladeState state, LivingEntity user, boolean isPowered) {
            super(blade, state);
            this.user = user;
            this.setPowered(isPowered);
        }

        public boolean isPowered() {
            return isPowered;
        }

        public void setPowered(boolean isPowered) {
            this.isPowered = isPowered;
        }

        public LivingEntity getUser() {
            return user;
        }

    }

    public static class AddProudSoulEvent extends SlashBladeEvent {
        private final int originCount;
        private int newCount;

        public AddProudSoulEvent(ItemStack blade, ISlashBladeState state, int count) {
            super(blade, state);
            this.originCount = count;
            this.setNewCount(count);
        }

        public int getOriginCount() {
            return originCount;
        }

        public int getNewCount() {
            return newCount;
        }

        public void setNewCount(int newCount) {
            this.newCount = newCount;
        }

    }

    public static class AddKillCountEvent extends SlashBladeEvent {
        private final int originCount;
        private int newCount;

        public AddKillCountEvent(ItemStack blade, ISlashBladeState state, int count) {
            super(blade, state);
            this.originCount = count;
            this.setNewCount(count);
        }

        public int getOriginCount() {
            return originCount;
        }

        public int getNewCount() {
            return newCount;
        }

        public void setNewCount(int newCount) {
            this.newCount = newCount;
        }

    }

    public static class UpdateAttackEvent extends SlashBladeEvent {
        private final double originDamage;
        private double newDamage;

        public UpdateAttackEvent(ItemStack blade, ISlashBladeState state, double damage) {
            super(blade, state);
            this.originDamage = damage;
            this.newDamage = damage;
        }

        public double getNewDamage() {
            return newDamage;
        }

        public void setNewDamage(double newDamage) {
            this.newDamage = newDamage;
        }

        public double getOriginDamage() {
            return originDamage;
        }
    }

    @Cancelable
    public static class BladeStandAttackEvent extends SlashBladeEvent {
        private final BladeStandEntity bladeStand;
        private final DamageSource damageSource;

        public BladeStandAttackEvent(ItemStack blade, ISlashBladeState state, BladeStandEntity bladeStand, DamageSource damageSource) {
            super(blade, state);
            this.bladeStand = bladeStand;
            this.damageSource = damageSource;
        }

        public BladeStandEntity getBladeStand() {
            return bladeStand;
        }

        public DamageSource getDamageSource() {
            return damageSource;
        }

    }

    public static class BladeStandTickEvent extends SlashBladeEvent {
        private final BladeStandEntity bladeStand;

        public BladeStandTickEvent(ItemStack blade, ISlashBladeState state, BladeStandEntity bladeStand) {
            super(blade, state);
            this.bladeStand = bladeStand;
        }

        public BladeStandEntity getBladeStand() {
            return bladeStand;
        }

    }

    @Cancelable
    public static class HitEvent extends SlashBladeEvent {
        private final LivingEntity target;
        private final LivingEntity user;

        public HitEvent(ItemStack blade, ISlashBladeState state, LivingEntity target, LivingEntity user) {
            super(blade, state);
            this.target = target;
            this.user = user;
        }

        public LivingEntity getUser() {
            return user;
        }

        public LivingEntity getTarget() {
            return target;
        }

    }

    @Cancelable
    public static class UpdateEvent extends SlashBladeEvent {
        private final Level level;
        private final Entity entity;
        private final int itemSlot;
        private final boolean isSelected;

        public UpdateEvent(ItemStack blade, ISlashBladeState state,
                           Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
            super(blade, state);
            this.level = worldIn;
            this.entity = entityIn;
            this.itemSlot = itemSlot;
            this.isSelected = isSelected;
        }

        public Level getLevel() {
            return level;
        }

        public Entity getEntity() {
            return entity;
        }

        public int getItemSlot() {
            return itemSlot;
        }

        public boolean isSelected() {
            return isSelected;
        }

    }

    @Cancelable
    public static class DoSlashEvent extends SlashBladeEvent {
        private final LivingEntity user;
        private float roll;
        private boolean critical;
        private double damage;
        private KnockBacks knockback;
        private float yRot = 0F;

        public DoSlashEvent(ItemStack blade, ISlashBladeState state, LivingEntity user,
                            float roll, boolean critical, double damage, KnockBacks knockback) {
            super(blade, state);
            this.user = user;
            this.roll = roll;
            this.critical = critical;
            this.knockback = knockback;
            this.damage = damage;
        }

        public LivingEntity getUser() {
            return user;
        }

        public float getRoll() {
            return roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }

        public boolean isCritical() {
            return critical;
        }

        public void setCritical(boolean critical) {
            this.critical = critical;
        }

        public double getDamage() {
            return damage;
        }

        public void setDamage(double damage) {
            this.damage = damage;
        }

        public KnockBacks getKnockback() {
            return knockback;
        }

        public void setKnockback(KnockBacks knockback) {
            this.knockback = knockback;
        }

        public float getYRot() {
            return yRot;
        }

        public void setYRot(float yRot) {
            this.yRot = yRot;
        }

    }

    @Cancelable
    public static class ChargeActionEvent extends Event {
        private final LivingEntity entityLiving;
        private final int elapsed;
        private final ISlashBladeState state;
        private ResourceLocation comboState;
        private final SlashArts.ArtsType type;

        public ChargeActionEvent(LivingEntity entityLiving, int elapsed, ISlashBladeState state, ResourceLocation comboState, SlashArts.ArtsType type) {
            this.entityLiving = entityLiving;
            this.elapsed = elapsed;
            this.state = state;
            this.comboState = comboState;
            this.type = type;
        }

        public LivingEntity getEntityLiving() {
            return entityLiving;
        }

        public int getElapsed() {
            return elapsed;
        }

        public ISlashBladeState getSlashBladeState() {
            return state;
        }

        public ResourceLocation getComboState() {
            return comboState;
        }

        public void setComboState(ResourceLocation comboState) {
            this.comboState = comboState;
        }

        public SlashArts.ArtsType getType() {
            return type;
        }
    }

    public static class SummonedSwordOnHitEntityEvent extends Event {
        private final EntityAbstractSummonedSword summonedSword;
        private final Entity target;

        public SummonedSwordOnHitEntityEvent(EntityAbstractSummonedSword summonedSword, Entity target) {
            this.summonedSword = summonedSword;
            this.target = target;
        }

        public EntityAbstractSummonedSword getSummonedSword() {
            return summonedSword;
        }

        public Entity getTarget() {
            return target;
        }
    }
}
