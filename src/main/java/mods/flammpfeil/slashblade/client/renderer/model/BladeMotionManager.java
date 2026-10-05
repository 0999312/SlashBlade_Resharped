package mods.flammpfeil.slashblade.client.renderer.model;

import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static mods.flammpfeil.slashblade.init.DefaultResources.ExMotionLocation;

/**
 * MMD（VMD）动画数据的缓存管理器，{@link AbstractBladeMotionManager} 的 MMD 实现。
 * <p>
 * 保留单例与 {@link #getInstance()} 入口，供 MMD 渲染链路（LayerMainBlade、VmdAnimation）使用；
 * 其他动画格式可自行实现 {@link AbstractBladeMotionManager} 子类。
 */
public class BladeMotionManager extends AbstractBladeMotionManager<MmdVmdMotionMc> {
    private static final class SingletonHolder {
        private static final BladeMotionManager instance = new BladeMotionManager();
    }
    
    public static BladeMotionManager getInstance() {
        return SingletonHolder.instance;
    }
    
    private BladeMotionManager() {
        super();
    }
    
    @Override
    protected MmdVmdMotionMc loadMotion(ResourceLocation key) throws IOException, MmdException {
        return new MmdVmdMotionMc(key);
    }
    
    @Override
    @Nullable
    protected MmdVmdMotionMc loadDefaultMotion() {
        try {
            return new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException | MmdException e) {
            SlashBlade.LOGGER.warn(e);
            return null;
        }
    }
}
