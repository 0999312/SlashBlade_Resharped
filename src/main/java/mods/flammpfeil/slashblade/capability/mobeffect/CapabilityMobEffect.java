package mods.flammpfeil.slashblade.capability.mobeffect;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CapabilityMobEffect {

    public static final Capability<IMobEffectState> MOB_EFFECT = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IMobEffectState.class);
    }
}
