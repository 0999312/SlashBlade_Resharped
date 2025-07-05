package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.Objects;

public class Guard {
    private static final class SingletonHolder {
        private static final Guard instance = new Guard();
    }

    public static Guard getInstance() {
        return Guard.SingletonHolder.instance;
    }

    private Guard() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    static public final ResourceLocation ADVANCEMENT_GUARD = new ResourceLocation(SlashBlade.MODID, "abilities/guard");
    static public final ResourceLocation ADVANCEMENT_GUARD_JUST = new ResourceLocation(SlashBlade.MODID,
            "abilities/guard_just");

    final static EnumSet<InputCommand> move = EnumSet.of(InputCommand.FORWARD, InputCommand.BACK, InputCommand.LEFT,
            InputCommand.RIGHT);

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        // begin executable check -----------------
        // item check
        ItemStack stack = victim.getMainHandItem();
        LazyOptional<ISlashBladeState> slashBlade = stack.getCapability(CapabilitySlashBlade.BLADESTATE);
        if (!slashBlade.isPresent()) {
            return;
        }
        if (slashBlade.filter(ISlashBladeState::isBroken).isPresent()) {
            return;
        }
        if (stack.getEnchantmentLevel(Enchantments.THORNS) <= 0) {
            return;
        }

        // user check
        if (!victim.onGround()) {
            return;
        }
        LazyOptional<IInputState> input = victim.getCapability(CapabilityInputState.INPUT_STATE);
        if (!input.isPresent()) {
            return;
        }

        // commanc check
        InputCommand targetCommand = InputCommand.SNEAK;
        boolean handleCommand = input.filter(i -> i.getCommands().contains(targetCommand)
                && i.getCommands().stream().noneMatch(move::contains)).isPresent();

        if (handleCommand) {
            AdvancementHelper.grantCriterion(victim, ADVANCEMENT_GUARD);
        }

        // ninja run
        handleCommand |= (input.filter(i -> i.getCommands().contains(InputCommand.SPRINT)).isPresent()
                && victim.isSprinting());

        if (!handleCommand) {
            return;
        }

        // range check
        if (!isInsideGuardableRange(source, victim)) {
            return;
        }

        // performance branch -----------------
        // just check
        long timeStartPress = input.map(i -> {
            long l = i.getLastPressTime(targetCommand);
            return l;
        }).orElse(0L);
        long timeCurrent = victim.level().getGameTime();

        int soulSpeedLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, victim);
        int justAcceptancePeriod = 5 + soulSpeedLevel;

        boolean isJust = false;
        if (timeCurrent - timeStartPress < justAcceptancePeriod) {
            isJust = true;
            AdvancementHelper.grantedIf(Enchantments.SOUL_SPEED, victim);
        }

        // rank check
        boolean isHighRank = false;
        LazyOptional<IConcentrationRank> rank = victim.getCapability(CapabilityConcentrationRank.RANK_POINT);
        if (rank.filter(r -> IConcentrationRank.ConcentrationRanks.S.level <= r.getRank(timeCurrent).level).isPresent()) {
            isHighRank = true;
        }

        // damage sauce check
        boolean isProjectile = source.is(DamageTypeTags.IS_PROJECTILE)
                || source.getDirectEntity() instanceof Projectile;

        // after executable check -----------------
        if (!isJust) {
            if (!isProjectile) {
                return;
            }
            if (!isHighRank && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                return;
            }

            boolean inMotion = slashBlade.filter(s -> {
                ResourceLocation current = s.resolvCurrentComboState(victim);
                ComboState currentCS = ComboStateRegistry.REGISTRY.get().getValue(current);
                return !current.equals(ComboStateRegistry.NONE.getId()) && current == Objects.requireNonNull(currentCS).getNext(victim);
            }).isPresent();
            if (inMotion) {
                return;
            }
        } else {
            if (!isProjectile && !(source.getDirectEntity() instanceof LivingEntity)) {
                return;
            }
        }

        // execute performance------------------
        // damage cancel
        event.setCanceled(true);

        // Motion
        if (isJust) {
            slashBlade.ifPresent(s -> s.updateComboSeq(victim, ComboStateRegistry.COMBO_A1.getId()));
        } else {
            slashBlade.ifPresent(s -> s.updateComboSeq(victim, ComboStateRegistry.COMBO_A1_END2.getId()));
        }

        // DirectAttack knockback
        if (!isProjectile) {
            Entity entity = source.getDirectEntity();
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).knockback(0.5D, entity.getX() - victim.getX(), entity.getZ() - victim.getZ());
            }
        }

        // untouchable time
        if (isJust) {
            Untouchable.setUntouchable(victim, 10);
        }

        // rankup
        if (isJust) {
            rank.ifPresent(r -> r.addRankPoint(victim.level().damageSources().thorns(victim)));
        }

        // play sound
        if (victim instanceof Player) {
            victim.playSound(SoundEvents.TRIDENT_HIT_GROUND, 1.0F,
                    1.0F + victim.level().getRandom().nextFloat() * 0.4F);
        }

        // advancement
        if (isJust) {
            AdvancementHelper.grantCriterion(victim, ADVANCEMENT_GUARD_JUST);
        }

        // cost-------------------------
        if (!isJust && !isHighRank) {
            slashBlade.ifPresent(s -> stack.hurtAndBreak(1, victim, ItemSlashBlade.getOnBroken(stack)));
        }

    }

    public boolean isInsideGuardableRange(DamageSource source, LivingEntity victim) {
        Vec3 sPos = source.getSourcePosition();
        if (sPos != null) {
            Vec3 viewVec = victim.getViewVector(1.0F);
            Vec3 attackVec = sPos.vectorTo(victim.position()).normalize();
            attackVec = new Vec3(attackVec.x, 0.0D, attackVec.z);
            return attackVec.dot(viewVec) < 0.0D;
        }
        return false;
    }
}
