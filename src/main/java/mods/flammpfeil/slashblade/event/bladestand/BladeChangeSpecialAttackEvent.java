package mods.flammpfeil.slashblade.event.bladestand;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class BladeChangeSpecialAttackEvent extends SlashBladeEvent {
    private ResourceLocation SAKey;
    private int shrinkCount = 0;
    private final SlashBladeEvent.BladeStandAttackEvent originalEvent;

    public BladeChangeSpecialAttackEvent(ItemStack blade, ISlashBladeState state, ResourceLocation SAKey,
                                         SlashBladeEvent.BladeStandAttackEvent originalEvent) {
        super(blade, state);
        this.SAKey = SAKey;
        this.originalEvent = originalEvent;
    }

    public ResourceLocation getSAKey() {
        return SAKey;
    }

    public ResourceLocation setSAKey(ResourceLocation SAKey) {
        this.SAKey = SAKey;
        return SAKey;
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
