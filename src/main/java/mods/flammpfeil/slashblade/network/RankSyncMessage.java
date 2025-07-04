package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RankSyncMessage {
    public long rawPoint;

    public RankSyncMessage() {
    }

    static public RankSyncMessage decode(FriendlyByteBuf buf) {
        RankSyncMessage msg = new RankSyncMessage();
        msg.rawPoint = buf.readLong();
        return msg;
    }

    static public void encode(RankSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.rawPoint);
    }

    static public void handle(RankSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);

        if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }

        Consumer<Long> handler = DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> RankSyncMessage::setPoint);

        if (handler != null) {
            ctx.get().enqueueWork(() -> handler.accept(msg.rawPoint));
        }

    }

    @OnlyIn(Dist.CLIENT)
    static public void setPoint(long point) {
        Player pl = Minecraft.getInstance().player;
        Objects.requireNonNull(pl).getCapability(CapabilityConcentrationRank.RANK_POINT).ifPresent(cr -> {

            long time = pl.level().getGameTime();

            IConcentrationRank.ConcentrationRanks oldRank = cr.getRank(time);

            cr.setRawRankPoint(point);
            cr.setLastUpdte(time);

            if (oldRank.level < cr.getRank(time).level) {
                cr.setLastRankRise(time);
            }
        });
    }
}
