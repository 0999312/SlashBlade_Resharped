package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Created by Furia on 2016/02/06.
 */
@OnlyIn(Dist.CLIENT)
public class BladeModelManager {

    private static final class SingletonHolder {
        private static final BladeModelManager instance = new BladeModelManager();
    }

    public static BladeModelManager getInstance() {
        return SingletonHolder.instance;
    }

    public static Registry<SlashBladeDefinition> getClientSlashBladeRegistry() {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess()
                .registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }

    WavefrontObject defaultModel;

    LoadingCache<ResourceLocation, WavefrontObject> cache;

    private BladeModelManager() {
        defaultModel = new WavefrontObject(DefaultResources.resourceDefaultModel);

        cache = CacheBuilder.newBuilder()
                .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                    @Override
                    public @NotNull WavefrontObject load(@NotNull ResourceLocation key) throws Exception {
                        try {
                            return new WavefrontObject(key);
                        } catch (Exception e) {
                            return defaultModel;
                        }
                    }

                }, Executors.newCachedThreadPool()));
    }

    @SubscribeEvent
    public void reload(TextureStitchEvent.Post event) {
        cache.invalidateAll();

        defaultModel = new WavefrontObject(DefaultResources.resourceDefaultModel);
    }

    public WavefrontObject getModel(ResourceLocation loc) {
        if (loc != null) {
            try {
                return cache.get(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultModel;
    }

}
