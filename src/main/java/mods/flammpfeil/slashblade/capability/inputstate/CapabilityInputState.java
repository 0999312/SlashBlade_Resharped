package mods.flammpfeil.slashblade.capability.inputstate;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CapabilityInputState {

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IInputState.class);
    }
}
