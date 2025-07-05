package mods.flammpfeil.slashblade.event.handler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AllowFlightOverrwrite {

    private static final class SingletonHolder {
        private static final AllowFlightOverrwrite instance = new AllowFlightOverrwrite();
    }

    public static AllowFlightOverrwrite getInstance() {
        return AllowFlightOverrwrite.SingletonHolder.instance;
    }

    private AllowFlightOverrwrite() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onFMLServerAboutToStartEvent(ServerAboutToStartEvent event) {
        event.getServer().setFlightAllowed(true);
    }
}
