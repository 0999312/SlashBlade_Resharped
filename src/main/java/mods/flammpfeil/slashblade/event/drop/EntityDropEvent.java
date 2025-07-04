package mods.flammpfeil.slashblade.event.drop;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber
public class EntityDropEvent {
    @SubscribeEvent
    public static void dropBlade(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        var bladeRegistry = SlashBlade.getSlashBladeDefinitionRegistry(entity.level());
        entity.level().registryAccess().registryOrThrow(EntityDropEntry.REGISTRY_KEY).forEach(entry -> {
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(entry.getEntityType())) {
                return;
            }
            if (!bladeRegistry.containsKey(entry.getBladeName())) {
                return;
            }

            if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
                return;
            }

            if (entry.isRequestSlashBladeKill() && !(attacker.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
                return;
            }

            float resultRate = Math.min(1F, entry.getDropRate() + event.getLootingLevel() * 0.1F);

            if (entry.isDropFixedPoint()) {
                dropBlade(entity, ForgeRegistries.ENTITY_TYPES.getValue(entry.getEntityType()),
                        Objects.requireNonNull(bladeRegistry.get(entry.getBladeName())).getBlade(), resultRate, entry.getDropPoint().x,
                        entry.getDropPoint().y, entry.getDropPoint().z);
            } else {
                dropBlade(entity, ForgeRegistries.ENTITY_TYPES.getValue(entry.getEntityType()),
                        Objects.requireNonNull(bladeRegistry.get(entry.getBladeName())).getBlade(), resultRate, entity.getX(), entity.getY(),
                        entity.getZ());
            }
        });

    }

    public static void dropBlade(LivingEntity entity, EntityType<?> type, ItemStack blade, float percent, double x,
                                 double y, double z) {
        if (entity.getType().equals(type)) {
            var rand = entity.level().getRandom();

            if (rand.nextFloat() > percent) {
                return;
            }
            ItemEntity itementity = new ItemEntity(entity.level(), x, y, z, blade);
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, entity.level());

            e.restoreFrom(itementity);
            e.init();
            e.push(0, 0.4, 0);

            e.setPickUpDelay(20 * 2);
            e.setGlowingTag(true);

            e.setAirSupply(-1);

            e.setThrower(entity.getUUID());

            entity.level().addFreshEntity(e);
        }
    }
}
