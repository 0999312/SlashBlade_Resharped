package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class RefineSettlementEvent extends SlashBladeEvent {
    private final AnvilUpdateEvent originalEvent;
    private int materialCost;
    private int costResult;
    private int refineResult;

    public RefineSettlementEvent(ItemStack blade, ISlashBladeState state, int materialCost, int costResult,
                                 int refineResult, AnvilUpdateEvent originalEvent) {
        super(blade, state);
        this.materialCost = materialCost;
        this.costResult = costResult;
        this.refineResult = refineResult;
        this.originalEvent = originalEvent;
    }

    public @Nullable AnvilUpdateEvent getOriginalEvent() {
        return originalEvent;
    }

    public int getMaterialCost() {
        return materialCost;
    }

    public int setMaterialCost(int materialCost) {
        this.materialCost = materialCost;
        return this.materialCost;
    }

    public int getCostResult() {
        return costResult;
    }

    public int setCostResult(int costResult) {
        this.costResult = costResult;
        return this.costResult;
    }

    public int getRefineResult() {
        return refineResult;
    }

    public int setRefineResult(int refineResult) {
        this.refineResult = refineResult;
        return this.refineResult;
    }
}
