package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.network.MotionBroadcastMessage;
import mods.flammpfeil.slashblade.network.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class BladeMotionEventBroadcaster {

    private static final class SingletonHolder {
        private static final BladeMotionEventBroadcaster instance = new BladeMotionEventBroadcaster();
    }

    public static BladeMotionEventBroadcaster getInstance() {
        return BladeMotionEventBroadcaster.SingletonHolder.instance;
    }

    private BladeMotionEventBroadcaster() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBladeMotion(BladeMotionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }

        MotionBroadcastMessage msg = new MotionBroadcastMessage();
        msg.playerId = sp.getUUID();
        msg.combo = event.getCombo().toString();

        // if(msg.combo == Extra.EX_JUDGEMENT_CUT.getName())
        {
            NetworkManager.INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(sp.getX(),
                    sp.getY(), sp.getZ(), 20, sp.serverLevel().dimension())), msg);
        }

    }
}
