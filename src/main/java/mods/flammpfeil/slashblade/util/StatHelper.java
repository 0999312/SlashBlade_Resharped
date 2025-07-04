package mods.flammpfeil.slashblade.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

public class StatHelper {

    static final long MAX_VALUE = Integer.MAX_VALUE;

    static public int increase(ServerPlayer player, ResourceLocation loc, int amount) {
        Stat<?> stat = Stats.CUSTOM.get(loc);
        ServerStatsCounter stats = player.getStats();

        int oldValue = stats.getValue(stat);
        int newValue = (int) Math.min((long) oldValue + (long) amount, MAX_VALUE);
        if (oldValue == newValue) {
            newValue--;
        }

        stats.setValue(player, stat, newValue);

        return newValue;
    }
}
