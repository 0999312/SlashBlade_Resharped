package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Lists;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.data.tag.SlashBladeEntityTypeTagProvider.EntityTypeTags;
import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class TargetSelector {
	static public final TargetingConditions lockon = new SlashBladeTargetingConditions().range(40.0D)
			.selector(new AttackablePredicate());

	static public final TargetingConditions test = new SlashBladeTargetingConditions()
			.selector(new AttackablePredicate());

	static final String AttackableTag = "RevengeAttacker";

	static boolean isAttackable(Entity revengeTarget, Entity attacker) {
		return revengeTarget != null && attacker != null
				&& (revengeTarget == attacker || revengeTarget.isAlliedTo(attacker));
	}

	static public final TargetingConditions areaAttack = new SlashBladeTargetingConditions().range(12.0D)
			.ignoreInvisibilityTesting().selector(new AttackablePredicate());

	static public TargetingConditions getAreaAttackPredicate(double reach) {
		return areaAttack.range(reach);
	}

	static public class AttackablePredicate implements Predicate<LivingEntity> {

		public boolean test(LivingEntity livingentity) {

			if (!SlashBladeConfig.PVP_ENABLE.get() && livingentity instanceof Player)
				return false;

			if (livingentity instanceof ArmorStand)
				if (((ArmorStand) livingentity).isMarker())
					return true;
				else
					return false;

			if (livingentity.getTags().contains(AttackableTag)) {
				livingentity.removeTag(AttackableTag);
				return true;
			}
			
			if (!SlashBladeConfig.FRIENDLY_ENABLE.get() && !(livingentity instanceof Enemy)) {
				return false;
			}

			if (livingentity.hasPassenger(entity -> entity instanceof Player))
				return false;

			if (livingentity.isCurrentlyGlowing())
				return true;

			if (livingentity.getTeam() != null)
				return true;

			return !livingentity.getType().is(EntityTypeTags.ATTACKABLE_BLACKLIST);
		}
	}

	static public List<Entity> getReflectableEntitiesWithinAABB(LivingEntity attacker) {
		double reach = TargetSelector.getResolvedReach(attacker);

		AABB aabb = getResolvedAxisAligned(attacker.getBoundingBox(), attacker.getLookAngle(), reach);
		Level world = attacker.level();
		return Stream.of(world.getEntitiesOfClass(Projectile.class, aabb).stream()
				.filter(e -> ((e.getOwner()/* getThrower() */ == null || e.getOwner()/* getThrower() */ != attacker)
						&& (e instanceof IShootable ? ((IShootable) e).getShooter() != attacker : true))))
				/*
				 * world.getEntitiesWithinAABB(DamagingProjectileEntity.class, aabb).stream()
				 * .filter(e-> (e.shootingEntity == null || e.shootingEntity != attacker)),
				 * world.getEntitiesWithinAABB(AbstractArrowEntity.class, aabb).stream()
				 * .filter(e->e.getShooter() == null || e.getShooter() != attacker))
				 */
				.flatMap(s -> s).filter(e -> (e.distanceToSqr(attacker) < (reach * reach)))
				.collect(Collectors.toList());
	}

	static public List<Entity> getExtinguishableEntitiesWithinAABB(LivingEntity attacker) {
		double reach = TargetSelector.getResolvedReach(attacker);

		AABB aabb = getResolvedAxisAligned(attacker.getBoundingBox(), attacker.getLookAngle(), reach);
		Level world = attacker.level();
		return world.getEntitiesOfClass(PrimedTnt.class, aabb).stream()
				.filter(e -> (e.distanceToSqr(attacker) < (reach * reach))).collect(Collectors.toList());
	}

	static public List<Entity> getTargettableEntitiesWithinAABB(Level world, LivingEntity attacker) {
		return getTargettableEntitiesWithinAABB(world, attacker,
				getResolvedAxisAligned(attacker.getBoundingBox(), attacker.getLookAngle(), getResolvedReach(attacker)));
	}

	static public List<Entity> getTargettableEntitiesWithinAABB(Level world, LivingEntity attacker, AABB aabb) {
		double reach = TargetSelector.getResolvedReach(attacker);

		return getTargettableEntitiesWithinAABB(world, attacker, aabb, reach);
	}

	public static List<Entity> getTargettableEntitiesWithinAABB(Level world, LivingEntity attacker, AABB aabb,
			double reach) {
		List<Entity> list1 = Lists.newArrayList();

		list1.addAll(getReflectableEntitiesWithinAABB(attacker));
		list1.addAll(getExtinguishableEntitiesWithinAABB(attacker));

		list1.addAll(world.getEntitiesOfClass(LivingEntity.class, aabb.inflate(5), e -> e.isMultipartEntity()).stream()
				.flatMap(e -> (e.isMultipartEntity()) ? Stream.of(e.getParts()) : Stream.of(e)).filter(t -> {
					boolean result = false;
					var check = new AttackablePredicate();
					if (t instanceof LivingEntity living) {
						result = check.test(living);
					} else if (t instanceof PartEntity<?> part) {
						if (part.getParent() instanceof LivingEntity living)
							result = check.test(living) && part.distanceToSqr(attacker) < (reach * reach);
					}
					return result;
				}).collect(Collectors.toList()));

		TargetingConditions predicate = getAreaAttackPredicate(reach);

		list1.addAll(world.getEntitiesOfClass(LivingEntity.class, aabb).stream()
				.flatMap(e -> (e.isMultipartEntity()) ? Stream.of(e.getParts()) : Stream.of(e)).filter(t -> {
					boolean result = false;
					if (t instanceof LivingEntity living) {
						result = predicate.test(attacker, living);
					} else if (t instanceof PartEntity<?> part) {
						if (part.getParent() instanceof LivingEntity living)
							result = predicate.test(attacker, living) && part.distanceToSqr(attacker) < (reach * reach);
					}
					return result;
				}).collect(Collectors.toList()));

		return list1;
	}

	static public <E extends Entity & IShootable> List<Entity> getTargettableEntitiesWithinAABB(Level world,
			double reach, E owner) {
		AABB aabb = owner.getBoundingBox().inflate(reach);

		List<Entity> list1 = Lists.newArrayList();

		list1.addAll(world.getEntitiesOfClass(EnderDragon.class, aabb.inflate(5)).stream()
				.flatMap(d -> Arrays.stream(d.getSubEntities())).filter(e -> (e.distanceToSqr(owner) < (reach * reach)))
				.collect(Collectors.toList()));

		LivingEntity user;
		if (owner.getShooter() instanceof LivingEntity)
			user = (LivingEntity) owner.getShooter();
		else
			user = null;

		list1.addAll(getReflectableEntitiesWithinAABB(world, reach, owner));

		TargetingConditions predicate = getAreaAttackPredicate(0); // reach check has already been completed

		list1.addAll(world.getEntitiesOfClass(LivingEntity.class, aabb, (e) -> true).stream()
				.filter(t -> predicate.test(user, t)).collect(Collectors.toList()));

		return list1;
	}

	static public <E extends Entity & IShootable> List<Entity> getReflectableEntitiesWithinAABB(Level world,
			double reach, E owner) {
		AABB aabb = owner.getBoundingBox().inflate(reach);

		return Stream
				.of(world.getEntitiesOfClass(Projectile.class, aabb).stream()
						.filter(e -> (e.getOwner()/* getThrower() */ == null
								|| e.getOwner()/* getThrower() */ != owner.getShooter())))
				/*
				 * world.getEntitiesWithinAABB(DamagingProjectileEntity.class, aabb).stream()
				 * .filter(e-> (e.shootingEntity == null || e.shootingEntity !=
				 * owner.getShooter())), world.getEntitiesWithinAABB(AbstractArrowEntity.class,
				 * aabb).stream() .filter(e->e.getShooter() == null || e.getShooter() !=
				 * owner.getShooter()))
				 */
				.flatMap(s -> s).filter(e -> (e.distanceToSqr(owner) < (reach * reach)) && e != owner)
				.collect(Collectors.toList());
	}

	static public AABB getResolvedAxisAligned(AABB bb, Vec3 dir, double reach) {
		final double padding = 1.0;

		if (dir == Vec3.ZERO) {
			bb = bb.inflate(reach * 2);
		} else {
			bb = bb.move(dir.scale(reach * 0.5)).inflate(reach);
		}

		bb = bb.inflate(padding);

		return bb;
	}

	static public double getResolvedReach(LivingEntity user) {
		double reach = 4.0D; /* 4 block */
		AttributeInstance attrib = user.getAttribute(ForgeMod.ENTITY_REACH.get());
		if (attrib != null) {
			reach = attrib.getValue() - 1;
		}
		return reach;
	}

	@SubscribeEvent
	public static void onInputChange(InputCommandEvent event) {

		EnumSet<InputCommand> old = event.getOld();
		EnumSet<InputCommand> current = event.getCurrent();
		ServerPlayer sender = event.getEntity();
		

		ItemStack stack = sender.getMainHandItem();
		if (stack.isEmpty())
			return;
		if (!(stack.getItem() instanceof ItemSlashBlade))
			return;

		// SneakHold & Middle Click
		if (!(!old.contains(InputCommand.M_DOWN) && current.contains(InputCommand.M_DOWN)
				&& current.contains(InputCommand.SNEAK)))
			return;

		stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
			Entity tmp = s.getTargetEntity(sender.level());
			if (tmp == null)
				return;
			if (!(tmp instanceof LivingEntity))
				return;

			LivingEntity target = (LivingEntity) tmp;

			if (target.getLastHurtByMob() == sender)
				return;

			target.setLastHurtByMob(sender);

			if (target.level() instanceof ServerLevel) {
				ServerLevel sw = (ServerLevel) target.level();

				sw.sendParticles(sender, ParticleTypes.ANGRY_VILLAGER, false, target.getX(),
						target.getY() + target.getEyeHeight(), target.getZ(), 5, target.getBbWidth() * 1.5,
						target.getBbHeight(), target.getBbWidth() * 1.5, 0.02D);
			}
		});
	}

	public static class SlashBladeTargetingConditions extends TargetingConditions {

		public SlashBladeTargetingConditions() {
			super(true);
		}

		@Override
		public boolean test(@Nullable LivingEntity attacker, LivingEntity target) {
			boolean isAttackable = false;

			isAttackable |= isAttackable(target.getLastHurtByMob(), attacker);

			if (!isAttackable && target instanceof Mob)
				isAttackable |= isAttackable(((Mob) target).getTarget(), attacker);

			if (isAttackable)
				target.addTag(AttackableTag);

			return super.test(attacker, target);
		}
	}

}
