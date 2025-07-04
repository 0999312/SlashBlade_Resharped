package mods.flammpfeil.slashblade.capability.mobeffect;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MobEffectCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<IMobEffectState> MOB_EFFECT = CapabilityManager.get(new CapabilityToken<>() {
    });

    protected LazyOptional<IMobEffectState> state = LazyOptional.of(MobEffectState::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return MOB_EFFECT.orEmpty(cap, state);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag baseTag = new CompoundTag();

        state.ifPresent(instance -> NBTHelper.getNBTCoupler(baseTag).put("StunTimeout", instance.getStunTimeOut()));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        state.ifPresent(instance -> NBTHelper.getNBTCoupler(nbt).get("StunTimeout", instance::setStunTimeOut));
    }
}
