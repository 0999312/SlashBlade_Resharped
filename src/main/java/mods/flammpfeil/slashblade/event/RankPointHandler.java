package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RankPointHandler {
    private static final class SingletonHolder {
        private static final RankPointHandler instance = new RankPointHandler();
    }

    public static RankPointHandler getInstance() {
        return SingletonHolder.instance;
    }

    private RankPointHandler() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Not reached if canceled.
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeathEvent(LivingHurtEvent event) {

        LivingEntity victim = event.getEntity();
        if (victim != null) {
            victim.getCapability(CapabilityConcentrationRank.RANK_POINT)
                    .ifPresent(cr -> cr.addRankPoint(victim, -cr.getUnitCapacity()));
        }

        Entity trueSource = event.getSource().getEntity();
        if (!(trueSource instanceof LivingEntity sourceEntity)) {
            return;
        }

        if (!sourceEntity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return;
        }

        trueSource.getCapability(CapabilityConcentrationRank.RANK_POINT)
                .ifPresent(cr -> cr.addRankPoint(event.getSource()));
    }
}
