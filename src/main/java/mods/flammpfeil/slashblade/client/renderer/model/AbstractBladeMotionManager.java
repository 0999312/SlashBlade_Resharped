package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;

/**
 * 动画数据（motion）缓存的抽象基类，与具体动画格式解耦。
 * <p>
 * 负责通用的缓存骨架：按 {@link ResourceLocation} 异步加载、失败回退默认动画、贴图重载时整体失效。
 * 具体动画格式（如 MMD 的 {@code MmdVmdMotionMc}、其他骨骼动画）由子类实现加载逻辑。
 *
 * @param <T> 动画数据的具体类型
 */
public abstract class AbstractBladeMotionManager<T> {
    
    /**
     * 默认动画；加载失败或未设置时为 {@code null}。
     */
    @Nullable
    protected T defaultMotion;
    
    /**
     * 动画缓存，容量 64，异步加载。
     */
    protected LoadingCache<ResourceLocation, T> cache;
    
    /**
     * 构造缓存骨架并加载默认动画。
     * <p>
     * 注意：构造期间会调用子类覆写的 {@link #loadDefaultMotion()}（动态分派），
     * 因此子类实现中不得访问自身尚未初始化的实例字段。
     */
    protected AbstractBladeMotionManager() {
        this.defaultMotion = this.loadDefaultMotion();
        
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(64)
            .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                @SuppressWarnings("DataFlowIssue")
                @Override
                public T load(ResourceLocation key) {
                    try {
                        return AbstractBladeMotionManager.this.loadMotion(key);
                    } catch (Exception e) {
                        SlashBlade.LOGGER.warn(e);
                        return AbstractBladeMotionManager.this.defaultMotion;
                    }
                }
                
            }, Executors.newFixedThreadPool(2)));
    }
    
    /**
     * 按位置加载指定动画数据。
     *
     * @param key 动画资源位置
     * @return 加载成功的动画数据
     * @throws Exception 加载失败时抛出，由缓存骨架回退到默认动画
     */
    protected abstract T loadMotion(ResourceLocation key) throws Exception;
    
    /**
     * 加载默认动画；失败时返回 {@code null}（缓存骨架据此回退）。
     * <p>
     * 构造期与 {@link #reload()} 均会调用；构造期调用时不得访问子类实例字段。
     *
     * @return 默认动画数据，可能为 {@code null}
     */
    @Nullable
    protected abstract T loadDefaultMotion();
    
    /**
     * 失效全部缓存并重新加载默认动画（贴图重载时调用）。
     */
    public void reload() {
        this.cache.invalidateAll();
        
        T newDefault = this.loadDefaultMotion();
        if (newDefault != null) {
            this.defaultMotion = newDefault;
        }
    }
    
    /**
     * 获取指定位置的动画数据；位置为空或加载失败时返回默认动画。
     *
     * @param loc 动画资源位置，可为 {@code null}
     * @return 动画数据，可能为 {@code null}
     */
    @Nullable
    public T getMotion(@Nullable ResourceLocation loc) {
        if (loc != null) {
            try {
                return this.cache.get(loc);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn(e);
            }
        }
        return this.defaultMotion;
    }
}
