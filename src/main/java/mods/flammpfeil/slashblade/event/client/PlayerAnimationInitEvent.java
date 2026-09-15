package mods.flammpfeil.slashblade.event.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;
import java.util.function.Supplier;

/**
 * PlayerAnimationOverrider 初始化事件（IModBusEvent，由 mod 总线发布）。
 * <p>
 * 在 {@link mods.flammpfeil.slashblade.compat.playerAnim.PlayerAnimationOverrider} 初始化时发布，
 * 供附属 mod 将 ComboState 绑定至 {@link IAnimation} 工厂：每次动画开始（BladeMotionEvent）时
 * 调用工厂 {@link Supplier#get()} 创建新的动画实例，已存在的绑定将被覆盖。
 */
public class PlayerAnimationInitEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, Supplier<IAnimation>> animationMap;
    
    public PlayerAnimationInitEvent(Map<ResourceLocation, Supplier<IAnimation>> animationMap) {
        this.animationMap = animationMap;
    }
    
    /**
     * 将 ComboState 绑定至 IAnimation 工厂。
     * <p>
     * 工厂应返回全新的动画实例（每次动画开始都会调用），不要在工厂间共享可变播放状态；
     * 若工厂返回 {@link mods.flammpfeil.slashblade.compat.playerAnim.PlayerAnimationBase}，
     * Overrider 会额外调用其 {@code play(elapsedTicks)} 以对齐动画起点。
     *
     * @param comboStateId ComboState 的注册 id（{@code ComboStateRegistry.REGISTRY} 的 key）
     * @param factory      动画工厂
     */
    public void register(ResourceLocation comboStateId, Supplier<IAnimation> factory) {
        this.animationMap.put(comboStateId, factory);
    }
}
