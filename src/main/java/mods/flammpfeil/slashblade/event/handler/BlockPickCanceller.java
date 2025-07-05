package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.client.SlashBladeKeyMappings;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockPickCanceller {
    private static final class SingletonHolder {
        private static final BlockPickCanceller instance = new BlockPickCanceller();
    }

    public static BlockPickCanceller getInstance() {
        return BlockPickCanceller.SingletonHolder.instance;
    }

    private BlockPickCanceller() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBlockPick(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock()) {
            return;
        }

        final Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null) {
            return;
        }
        if (SlashBladeKeyMappings.KEY_SUMMON_BLADE.getKey() != SlashBladeKeyMappings.KEY_SUMMON_BLADE.getDefaultKey()) {
            return;
        }
        if (player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            event.setCanceled(true);
        }
    }
}
