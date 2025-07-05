package mods.flammpfeil.slashblade.event.handler;

import java.util.concurrent.atomic.AtomicInteger;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.event.RefineProgressEvent;
import mods.flammpfeil.slashblade.event.RefineSettlementEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RefineHandler {
    private static final class SingletonHolder {
        private static final RefineHandler instance = new RefineHandler();
    }

    public static RefineHandler getInstance() {
        return SingletonHolder.instance;
    }

    private RefineHandler() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        if (!event.getOutput().isEmpty())
            return;

        ItemStack base = event.getLeft();
        ItemStack material = event.getRight();

        if (base.isEmpty()) {
            return;
        }
        if (!(base.getCapability(ItemSlashBlade.BLADESTATE).isPresent())) {
            return;
        }

        if (material.isEmpty()) {
            return;
        }

        boolean isRepairable = base.getItem().isValidRepairItem(base, material);
        if (!isRepairable) {
            return;
        }

        int level = material.getEnchantmentValue();

        if (level < 0) {
            return;
        }

        ItemStack result = base.copy();

        int refineLimit = Math.max(10, level);

        int materialCost = 0;
        int levelCostBase = SlashBladeConfig.REFINE_LEVEL_COST.get();
        int costResult = 0;
        AtomicInteger refineResult = new AtomicInteger(0);
        result.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> refineResult.set(s.getRefine()));

        while (materialCost < material.getCount()) {

            RefineProgressEvent e = new RefineProgressEvent(result,
                    result.getCapability(ItemSlashBlade.BLADESTATE).orElse(new SlashBladeState(result)), materialCost + 1, levelCostBase,
                    costResult, refineResult.get(), event);

            MinecraftForge.EVENT_BUS.post(e);
            if (e.isCanceled()) {
                break;
            }

            refineResult.set(e.getRefineResult());

            materialCost = e.getMaterialCost();
            costResult = e.getCostResult() + e.getLevelCost();

            if (!event.getPlayer().getAbilities().instabuild && event.getPlayer().experienceLevel < costResult) {
                break;
            }
        }

        if (result.getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            ISlashBladeState state = result.getCapability(ItemSlashBlade.BLADESTATE).resolve().orElse(new SlashBladeState(result));
            RefineSettlementEvent e2 = new RefineSettlementEvent(result,
                    state, materialCost, costResult, refineResult.get(), event);

            MinecraftForge.EVENT_BUS.post(e2);
            if (e2.isCanceled()) {
                return;
            }

            state.setProudSoulCount(state.getProudSoulCount() + Math.min(5000, level * 10));
            if (state.getRefine() < refineLimit) {
                if (state.getRefine() + e2.getRefineResult() < 200) {
                    state.setMaxDamage(state.getMaxDamage() + e2.getRefineResult());
                } else if (state.getRefine() < 200) {
                    state.setMaxDamage(state.getMaxDamage() + Math.min(state.getRefine() + e2.getRefineResult(), 200)
                            - state.getRefine());
                }
                state.setRefine(e2.getRefineResult());
            }

            result.setDamageValue(result.getDamageValue() - Math.max(1, level / 2));
            result.getOrCreateTag().put("bladeState", state.serializeNBT());

            materialCost = e2.getMaterialCost();
            costResult = e2.getCostResult();
        }

        event.setMaterialCost(materialCost);
        event.setCost(costResult);
        event.setOutput(result);
    }

    static private final ResourceLocation REFINE = new ResourceLocation(SlashBlade.MODID, "tips/refine");

    @SubscribeEvent
    public void onAnvilRepairEvent(AnvilRepairEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer))
            return;

        ItemStack material = event.getRight();// .getIngredientInput();
        ItemStack base = event.getLeft();// .getItemInput();
        ItemStack output = event.getOutput();

        if (base.isEmpty())
            return;
        if (!(base.getItem() instanceof ItemSlashBlade))
            return;
        if (material.isEmpty())
            return;

        boolean isRepairable = base.getItem().isValidRepairItem(base, material);

        if (!isRepairable)
            return;

        int before = base.getCapability(ItemSlashBlade.BLADESTATE).map(s -> s.getRefine()).orElse(0);
        int after = output.getCapability(ItemSlashBlade.BLADESTATE).map(s -> s.getRefine()).orElse(0);

        if (before < after)
            AdvancementHelper.grantCriterion((ServerPlayer) event.getEntity(), REFINE);

    }

}
