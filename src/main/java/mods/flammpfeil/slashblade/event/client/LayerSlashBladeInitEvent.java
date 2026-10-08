package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.client.renderer.layers.LayerSlashBlade;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Function;

/**
 * LayerSlashBlade 相关注册表初始化事件（IModBusEvent，由 mod 总线发布）。
 * <p>
 * 在客户端初始化期（{@code FMLClientSetupEvent}，先于实体渲染器装配）发布，
 * 供附属 mod 将 ComboRoot（{@code ISlashBladeState#getComboRoot()} 的返回值）绑定至
 * 特定 Layer 实例：渲染时依据刀上配置的 ComboRoot 自动区分调用绑定层或默认层。
 */
public class LayerSlashBladeInitEvent extends Event implements IModBusEvent {
    /**
     * 将 ComboRoot 绑定至 LayerSlashBlade 工厂。
     * <p>
     * 工厂入参为实体渲染器（每个渲染器各实例化一份 Layer），渲染时若刀的 ComboRoot
     * 命中该绑定，主刀渲染将委托给对应 Layer 实例。
     *
     * @param comboRoot 目标 ComboRoot
     * @param factory   层工厂
     */
    public void register(ResourceLocation comboRoot,
                         Function<RenderLayerParent<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>, LayerSlashBlade<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>>> factory) {
        LayerSlashBladeRegistry.register(comboRoot, factory);
    }
}
