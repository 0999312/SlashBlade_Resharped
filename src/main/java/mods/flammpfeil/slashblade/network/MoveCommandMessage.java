package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Supplier;

public class MoveCommandMessage {
    public int command;

    public MoveCommandMessage() {
    }

    static public MoveCommandMessage decode(FriendlyByteBuf buf) {
        MoveCommandMessage msg = new MoveCommandMessage();
        msg.command = buf.readInt();
        return msg;
    }

    static public void encode(MoveCommandMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.command);
    }

    static public void handle(MoveCommandMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Work that needs to be threadsafe (most work)
            ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
            // do stuff
            ItemStack stack = Objects.requireNonNull(sender).getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.isEmpty()) {
                return;
            }
            if (!(stack.getCapability(ItemSlashBlade.BLADESTATE).isPresent())) {
                return;
            }

            sender.getCapability(CapabilityInputState.INPUT_STATE).ifPresent((state) -> {
                EnumSet<InputCommand> old = state.getCommands().clone();

                state.getCommands().clear();
                state.getCommands().addAll(EnumSetConverter.convertToEnumSet(InputCommand.class, msg.command));

                EnumSet<InputCommand> current = state.getCommands().clone();

                long currentTime = sender.level().getGameTime();
                current.forEach(c -> {
                    if (!old.contains(c)) {
                        state.getLastPressTimes().put(c, currentTime);
                    }
                });

                InputCommandEvent.onInputChange(sender, state, old, current);

            });
        });
        ctx.get().setPacketHandled(true);
    }
}
