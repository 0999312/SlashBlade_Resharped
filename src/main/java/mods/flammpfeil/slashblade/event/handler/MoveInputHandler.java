package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.client.SlashBladeKeyMappings;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.network.MoveCommandMessage;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MoveInputHandler {

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final String LAST_CHANGE_TIME = "SB_LAST_CHANGE_TIME";

    public static boolean checkFlag(int data, int flags) {
        return (data & flags) == flags;
    }

    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onPlayerPostTick(ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (player.getMainHandItem().isEmpty() || !player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return;
        }

        EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);

        if (player.input.up) {
            commands.add(InputCommand.FORWARD);
        }
        if (player.input.down) {
            commands.add(InputCommand.BACK);
        }
        if (player.input.left) {
            commands.add(InputCommand.LEFT);
        }
        if (player.input.right) {
            commands.add(InputCommand.RIGHT);
        }

        if (player.input.shiftKeyDown) {
            commands.add(InputCommand.SNEAK);
        }

        if (player.input.jumping) {
            commands.add(InputCommand.JUMP);
        }

        final Minecraft minecraftInstance = Minecraft.getInstance();

        if (SlashBladeKeyMappings.KEY_SPECIAL_MOVE.isDown()) {
            commands.add(InputCommand.SPRINT);
        }

        if (minecraftInstance.options.keyUse.isDown()) {
            commands.add(InputCommand.R_DOWN);
        }
        if (minecraftInstance.options.keyAttack.isDown()) {
            commands.add(InputCommand.L_DOWN);
        }

        if (SlashBladeKeyMappings.KEY_SUMMON_BLADE.isDown()) {
            commands.add(InputCommand.M_DOWN);
        }

        EnumSet<InputCommand> old = player.getCapability(INPUT_STATE).map(IInputState::getCommands)
                .orElseGet(() -> EnumSet.noneOf(InputCommand.class));

        Level worldIn = player.getCommandSenderWorld();

        long currentTime = worldIn.getGameTime();
        boolean doSend = !old.equals(commands);

        if (doSend) {
            player.getCapability(INPUT_STATE).ifPresent((state) -> {
                commands.forEach(c -> {
                    if (!old.contains(c)) {
                        state.getLastPressTimes().put(c, currentTime);
                    }
                });

                state.getCommands().clear();
                state.getCommands().addAll(commands);
            });
            MoveCommandMessage msg = new MoveCommandMessage();
            msg.command = EnumSetConverter.convertToInt(commands);
            NetworkManager.INSTANCE.sendToServer(msg);
        }
    }
}
