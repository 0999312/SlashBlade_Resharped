package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
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

public class InputStateCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>() {
    });

    protected LazyOptional<IInputState> state = LazyOptional.of(InputState::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return INPUT_STATE.orEmpty(cap, state);
    }

    static final String KEY = "Command";

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag baseTag = new CompoundTag();

        state.ifPresent(instance -> baseTag.putInt(KEY, EnumSetConverter.convertToInt(instance.getCommands())));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundTag baseTag) {
        state.ifPresent(instance -> instance.getCommands().addAll(EnumSetConverter.convertToEnumSet(InputCommand.class, baseTag.getInt(KEY))));
    }
}
