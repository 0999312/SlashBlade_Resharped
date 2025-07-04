package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SneakingMotionCanceller {
    private static final class SingletonHolder {
        private static final SneakingMotionCanceller instance = new SneakingMotionCanceller();
    }

    public static SneakingMotionCanceller getInstance() {
        return SingletonHolder.instance;
    }

    private SneakingMotionCanceller() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderPlayerEventPre(RenderPlayerEvent.Pre event) {
        ItemStack stack = event.getEntity().getMainHandItem();

        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getCapability(ItemSlashBlade.BLADESTATE).isPresent())) {
            return;
        }

        if (!event.getRenderer().getModel().crouching) {
            return;
        }

        final Minecraft instance = Minecraft.getInstance();
        if (instance.options.getCameraType() == CameraType.FIRST_PERSON && instance.player == event.getEntity()) {
            return;
        }

        event.getRenderer().getModel().crouching = false;

        Vec3 offset = event.getRenderer()
                .getRenderOffset((AbstractClientPlayer) event.getEntity(), event.getPartialTick()).scale(-1);

        event.getPoseStack().translate(offset.x, offset.y, offset.z);
    }
}
