package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NamedBladeStateCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    protected LazyOptional<ISlashBladeState> state;
    private final ItemStack blade;

    public NamedBladeStateCapabilityProvider(ItemStack blade) {
        if (!blade.isEmpty()) {
            state = LazyOptional.of(() -> new SlashBladeState(blade));
            this.blade = blade;
        } else {
            state = LazyOptional.empty();
            this.blade = ItemStack.EMPTY;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ItemSlashBlade.BLADESTATE.orEmpty(cap, state);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.state.orElseGet(() -> new SlashBladeState(blade)).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag inTag) {
        state.ifPresent(instance -> instance.deserializeNBT(inTag));
    }
}
