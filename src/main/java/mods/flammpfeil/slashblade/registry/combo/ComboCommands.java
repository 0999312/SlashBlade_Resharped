package mods.flammpfeil.slashblade.registry.combo;

import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class ComboCommands {
    public static final EnumSet<InputCommand> COMBO_B1_ALT = EnumSet.of(InputCommand.BACK, InputCommand.R_DOWN);
    private static final Map<EnumSet<InputCommand>, ResourceLocation> DEAFULT_STANDBY = new HashMap<>();

    public static ResourceLocation initStandByCommand(LivingEntity a) {
        return initStandByCommand(a, DEAFULT_STANDBY);
    }

    public static ResourceLocation initStandByCommand(LivingEntity a,
                                                      Map<EnumSet<InputCommand>, ResourceLocation> map) {
        EnumSet<InputCommand> commands = a.getCapability(CapabilityInputState.INPUT_STATE)
                .map((state) -> state.getCommands(a)).orElseGet(() -> EnumSet.noneOf(InputCommand.class));

        return map.entrySet().stream().filter((entry) -> commands.containsAll(entry.getKey()))
                // .findFirst()
                .min(Comparator.comparingInt(
                        (entry) -> Objects.requireNonNull(ComboStateRegistry.REGISTRY.get().getValue(entry.getValue())).getPriority()))
                .map(Map.Entry::getValue).orElseGet(ComboStateRegistry.NONE::getId);
    }

    public static void initDefaultStandByCommands() {
        DEAFULT_STANDBY.put(
                EnumSet.of(InputCommand.ON_GROUND, InputCommand.SNEAK, InputCommand.FORWARD, InputCommand.R_CLICK),
                ComboStateRegistry.RAPID_SLASH.getId());
        DEAFULT_STANDBY.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.L_CLICK),
                ComboStateRegistry.COMBO_A1.getId());
        DEAFULT_STANDBY.put(
                EnumSet.of(InputCommand.ON_GROUND, InputCommand.BACK, InputCommand.SNEAK, InputCommand.R_CLICK),
                ComboStateRegistry.UPPERSLASH.getId());

        DEAFULT_STANDBY.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.R_CLICK),
                ComboStateRegistry.COMBO_A1.getId());

        DEAFULT_STANDBY.put(
                EnumSet.of(InputCommand.ON_AIR, InputCommand.SNEAK, InputCommand.BACK, InputCommand.R_CLICK),
                ComboStateRegistry.AERIAL_CLEAVE.getId());
        DEAFULT_STANDBY.put(EnumSet.of(InputCommand.ON_AIR), ComboStateRegistry.AERIAL_RAVE_A1.getId());
    }
}
