package mods.flammpfeil.slashblade.event.bladestand;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class BladeChangeSpecialEffectEvent extends SlashBladeEvent {
    private ResourceLocation SEKey;
    private int shrinkCount = 0;
    private final SlashBladeEvent.BladeStandAttackEvent originalEvent;

    public BladeChangeSpecialEffectEvent(ItemStack blade, ISlashBladeState state, ResourceLocation SEKey,
                                         SlashBladeEvent.BladeStandAttackEvent originalEvent) {
        super(blade, state);
        this.SEKey = SEKey;
        this.originalEvent = originalEvent;
    }

    public ResourceLocation getSEKey() {
        return SEKey;
    }

    public ResourceLocation setSEKey(ResourceLocation SEKey) {
        this.SEKey = SEKey;
        return SEKey;
    }

    public int getShrinkCount() {
        return shrinkCount;
    }

    public int setShrinkCount(int shrinkCount) {
        this.shrinkCount = shrinkCount;
        return this.shrinkCount;
    }

    public @Nullable SlashBladeEvent.BladeStandAttackEvent getOriginalEvent() {
        return originalEvent;
    }
}
