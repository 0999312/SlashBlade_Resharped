package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.Executors;

import static mods.flammpfeil.slashblade.init.DefaultResources.ExMotionLocation;

/**
 * Created by Furia on 2016/02/06.
 */
public class BladeMotionManager {

    private static final class SingletonHolder {
        private static final BladeMotionManager instance = new BladeMotionManager();
    }

    public static BladeMotionManager getInstance() {
        return SingletonHolder.instance;
    }

    MmdVmdMotionMc defaultMotion;

    LoadingCache<ResourceLocation, MmdVmdMotionMc> cache;

    private BladeMotionManager() {
        try {
            defaultMotion = new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException | MmdException e) {
            SlashBlade.LOGGER.error(e);
        }

        cache = CacheBuilder.newBuilder()
                .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                    @Override
                    public @NotNull MmdVmdMotionMc load(@NotNull ResourceLocation key) {
                        try {
                            return new MmdVmdMotionMc(key);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return defaultMotion;
                        }
                    }

                }, Executors.newCachedThreadPool()));
    }

    @SubscribeEvent
    public void reload(TextureStitchEvent.Post event) {
        cache.invalidateAll();

        try {
            defaultMotion = new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException | MmdException e) {
            SlashBlade.LOGGER.error(e);
        }
    }

    public MmdVmdMotionMc getMotion(ResourceLocation loc) {
        if (loc != null) {
            try {
                return cache.get(loc);
            } catch (Exception e) {
                SlashBlade.LOGGER.error(e);
            }
        }
        return defaultMotion;
    }

}
