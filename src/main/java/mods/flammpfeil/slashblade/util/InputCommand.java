package mods.flammpfeil.slashblade.util;

import java.util.EnumSet;

//32bit limit
public enum InputCommand {
    FORWARD, BACK, LEFT, RIGHT, SNEAK, R_DOWN, L_DOWN, M_DOWN, R_CLICK, L_CLICK, ON_GROUND, ON_AIR, SPRINT, JUMP;

    public final static EnumSet<InputCommand> move = EnumSet.of(InputCommand.FORWARD, InputCommand.BACK,
            InputCommand.LEFT, InputCommand.RIGHT);

    public static boolean anyMatch(EnumSet<InputCommand> a, EnumSet<InputCommand> b) {
        return a.stream().anyMatch(b::contains);
    }
}
