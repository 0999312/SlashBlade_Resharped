package mods.flammpfeil.slashblade.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;

public class TNTExtinguisher {
    public static void doExtinguishing(Entity target, LivingEntity attacker) {
        if (!(target instanceof PrimedTnt)) {
            return;
        }

        if (attacker.level().isClientSide()) {
            return;
        }

        target.remove(Entity.RemovalReason.KILLED);

        ServerLevel world = (ServerLevel) attacker.level();

        world.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + target.getBbHeight() * 0.5,
                target.getZ(), 5, target.getBbWidth() * 1.5, target.getBbHeight(), target.getBbWidth() * 1.5, 0.02D);

        if (target.getType() == EntityType.TNT) {
            if (world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                ItemEntity itementity = new ItemEntity(world, target.getX(), target.getY() + target.getBbHeight(),
                        target.getZ(), new ItemStack(Items.TNT));
                itementity.setDefaultPickUpDelay();

                world.addFreshEntity(itementity);
            }
        }
    }
}
