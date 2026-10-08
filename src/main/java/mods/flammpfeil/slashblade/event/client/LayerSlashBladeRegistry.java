package mods.flammpfeil.slashblade.event.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerSlashBlade;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.function.Function;

public class LayerSlashBladeRegistry {
    private static final Map<ResourceLocation, Function<RenderLayerParent<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>, LayerSlashBlade<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>>> LAYERS = Maps
        .newHashMap();
    
    private LayerSlashBladeRegistry() {
    }
    
    /**
     * 将 ComboRoot 绑定至 Layer 工厂。
     *
     * @param comboRoot 目标 ComboRoot（对应 {@code ISlashBladeState#getComboRoot()} 的返回值）
     * @param factory   层工厂，每个实体渲染器各实例化一份
     */
    public static void register(ResourceLocation comboRoot,
                                Function<RenderLayerParent<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>, LayerSlashBlade<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>> factory) {
        LAYERS.put(comboRoot, factory);
    }
    
    public static Map<ResourceLocation, Function<RenderLayerParent<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>, LayerSlashBlade<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>>> getEntries() {
        return ImmutableMap.copyOf(LAYERS);
    }
    
    public static boolean isEmpty() {
        return LAYERS.isEmpty();
    }
}
