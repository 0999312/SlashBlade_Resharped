package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.EnumSet;

public class InputCommandEvent extends Event {

    public InputCommandEvent(ServerPlayer player, IInputState state, EnumSet<InputCommand> old,
                             EnumSet<InputCommand> current) {
        this.player = player;
        this.state = state;
        this.old = old;
        this.current = current;
    }

    public ServerPlayer getEntity() {
        return player;
    }

    public IInputState getState() {
        return state;
    }

    public EnumSet<InputCommand> getOld() {
        return old;
    }

    public EnumSet<InputCommand> getCurrent() {
        return current;
    }

    ServerPlayer player;
    IInputState state;
    EnumSet<InputCommand> old;
    EnumSet<InputCommand> current;

    public static InputCommandEvent onInputChange(ServerPlayer player, IInputState state, EnumSet<InputCommand> old,
                                                  EnumSet<InputCommand> current) {
        InputCommandEvent event = new InputCommandEvent(player, state, old, current);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
