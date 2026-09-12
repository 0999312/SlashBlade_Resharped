package mods.flammpfeil.slashblade.event.client;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerSlashBlade;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Function;

/**
 * ComboRoot → Layer 实例工厂的静态注册表。
 * <p>
 * 由 {@link LayerSlashBladeInitEvent} 在初始化期填充，被 ClientHandler 在
 * {@code EntityRenderersEvent.AddLayers} 时消费：为每个实体渲染器实例化默认层与各绑定层，
 * 渲染时按刀上配置的 ComboRoot 自动区分调用。
 */
public class LayerSlashBladeRegistry {
    private static final Map<ResourceLocation, Function<RenderLayerParent<?, ?>, LayerSlashBlade<?, ?>>> LAYERS = Maps
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
                                Function<RenderLayerParent<?, ?>, LayerSlashBlade<?, ?>> factory) {
        LAYERS.put(comboRoot, factory);
    }
    
    public static Map<ResourceLocation, Function<RenderLayerParent<?, ?>, LayerSlashBlade<?, ?>>> getEntries() {
        return LAYERS;
    }
    
    public static boolean isEmpty() {
        return LAYERS.isEmpty();
    }
}
