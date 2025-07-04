package mods.flammpfeil.slashblade.capability.concentrationrank;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.network.RankSyncMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public interface IConcentrationRank {

    enum ConcentrationRanks {
        NONE(0, Range.lessThan(1.0f)), D(1, Range.closedOpen(1.0f, 2.0f)), C(2, Range.closedOpen(2.0f, 3.0f)),
        B(3, Range.closedOpen(3.0f, 4.0f)), A(4, Range.closedOpen(4.0f, 5.0f)), S(5, Range.closedOpen(5.0f, 5.25f)),
        SS(6, Range.closedOpen(5.25f, 5.5f)), SSS(7, Range.atLeast(5.5f)),
        ;

        static public float MAX_LEVEL = 6.0f;

        final Range<Float> pointRange;
        public final int level;

        ConcentrationRanks(int level, Range<Float> pointRange) {
            this.pointRange = pointRange;
            this.level = level;
        }

        public static ConcentrationRanks getRankFromLevel(float point) {
            return concentrationRanksMap.get(point);
        }

        private static final RangeMap<Float, ConcentrationRanks> concentrationRanksMap = ImmutableRangeMap
                .<Float, ConcentrationRanks>builder().put(ConcentrationRanks.NONE.pointRange, ConcentrationRanks.NONE)
                .put(ConcentrationRanks.D.pointRange, ConcentrationRanks.D)
                .put(ConcentrationRanks.C.pointRange, ConcentrationRanks.C)
                .put(ConcentrationRanks.B.pointRange, ConcentrationRanks.B)
                .put(ConcentrationRanks.A.pointRange, ConcentrationRanks.A)
                .put(ConcentrationRanks.S.pointRange, ConcentrationRanks.S)
                .put(ConcentrationRanks.SS.pointRange, ConcentrationRanks.SS)
                .put(ConcentrationRanks.SSS.pointRange, ConcentrationRanks.SSS).build();
    }

    long getRawRankPoint();

    void setRawRankPoint(long point);

    long getLastUpdate();

    void setLastUpdte(long time);

    long getLastRankRise();

    void setLastRankRise(long time);

    long getUnitCapacity();

    default long getMaxCapacity() {
        return (long) (ConcentrationRanks.MAX_LEVEL * getUnitCapacity()) - 1;
    }

    default ConcentrationRanks getRank(long time) {
        return ConcentrationRanks.getRankFromLevel(getRankLevel(time));
    }

    default long reductionLimitter(long reduction) {
        long limit = getRawRankPoint() % getUnitCapacity();

        return Math.min(reduction, limit);
    }

    default float getRankLevel(long currentTime) {
        return getRankPoint(currentTime) / (float) getUnitCapacity();
    }

    default float getRankProgress(long currentTime) {
        float level = getRankLevel(currentTime);

        Range<Float> range = getRank(currentTime).pointRange;

        double bottom = range.hasLowerBound() ? range.lowerEndpoint() : 0;

        double top = range.hasUpperBound() ? range.upperEndpoint() : Math.floor(bottom + 1.0f);

        double len = top - bottom;

        return (float) ((level - bottom) / len);
    }

    default long getRankPoint(long time) {
        long reduction = time - getLastUpdate();
        return getRawRankPoint() - reductionLimitter(reduction);
    }

    default void addRankPoint(LivingEntity user, long point) {
        long time = user.level().getGameTime();

        ConcentrationRanks oldRank = getRank(time);

        this.setRawRankPoint(Math.min(Math.max(0, point + getRankPoint(time)), getMaxCapacity()));
        this.setLastUpdte(time);

        if (oldRank.level < getRank(time).level) {
            this.setLastRankRise(time);
        }

        if (user instanceof ServerPlayer && !user.level().isClientSide()) {

            RankSyncMessage msg = new RankSyncMessage();
            msg.rawPoint = this.getRawRankPoint();
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) user), msg);
        }
    }

    default void addRankPoint(DamageSource src) {
        if (!(src.getEntity() instanceof LivingEntity user)) {
            return;
        }

        ItemStack stack = user.getMainHandItem();

        Optional<ResourceLocation> combo = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .map(s -> s.resolvCurrentComboState(user));

        float modifier = combo.map(this::getRankPointModifier).orElse(getRankPointModifier(src));

        addRankPoint(user, (long) (modifier * getUnitCapacity()));
    }

    float getRankPointModifier(DamageSource ds);

    float getRankPointModifier(ResourceLocation combo);
}
