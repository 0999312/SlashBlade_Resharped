package mods.flammpfeil.slashblade.util;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.IntStream;

public class EnumSetConverter {
    public static <T extends Enum<T>> int convertToInt(EnumSet<T> enumSet) {
        return enumSet.stream().mapToInt(e -> 1 << e.ordinal()).sum();
    }

    public static <T extends Enum<T>> EnumSet<T> convertToEnumSet(Class<T> tclass, int ivalues) {
        T[] values = tclass.getEnumConstants();
        EnumSet<T> set = EnumSet.noneOf(tclass);
        IntStream.range(0, Math.min(values.length, 32)).filter(i -> (ivalues & (1 << i)) != 0)
                .forEach(i -> set.add(values[i]));
        return set;
    }

    public static <T extends Enum<?>> T fromOrdinal(T[] values, int ordinal, T def) {
        return Arrays.stream(values).skip(ordinal).findFirst().orElse(def);
    }
}
