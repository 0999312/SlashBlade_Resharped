package mods.flammpfeil.slashblade.data.builtin;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class SlashBladeEntityDropBuiltInRegistry {
    public static final ResourceKey<EntityDropEntry> ENDER_DRAGON_YAMATO = register("ender_dragon_yamato");
    public static final ResourceKey<EntityDropEntry> WITHER_SANGE = register("wither_sange");

    public static final ResourceKey<EntityDropEntry> MINOTAUR_YASHA = register("minotaur_yasha");
    public static final ResourceKey<EntityDropEntry> MINOSHROOM_YASHA_TRUE = register("minoshroom_yasha_true");

    public static final ResourceKey<EntityDropEntry> NAGA_AGITO = register("naga_agito");
    public static final ResourceKey<EntityDropEntry> HYDRA_OROTIAGITO = register("hydra_orotiagito");

    public static void registerAll(BootstapContext<EntityDropEntry> bootstrap) {
        bootstrap.register(ENDER_DRAGON_YAMATO, new EntityDropEntry(new ResourceLocation("minecraft", "ender_dragon"),
                SlashBlade.prefix("yamato_broken"), 1.0F, false, true, new Vec3(0F, 60F, 0F)));

        bootstrap.register(WITHER_SANGE, new EntityDropEntry(new ResourceLocation("minecraft", "wither"),
                SlashBlade.prefix("sange"), 0.3F, true));

        bootstrap.register(MINOTAUR_YASHA, new EntityDropEntry(new ResourceLocation("twilightforest", "minotaur"),
                SlashBlade.prefix("yasha"), 0.05F, true));

        bootstrap.register(MINOSHROOM_YASHA_TRUE, new EntityDropEntry(
                new ResourceLocation("twilightforest", "minoshroom"), SlashBlade.prefix("yasha_true"), 0.2F, true));

        bootstrap.register(NAGA_AGITO, new EntityDropEntry(new ResourceLocation("twilightforest", "naga"),
                SlashBlade.prefix("agito_rust"), 0.3F, false));

        bootstrap.register(HYDRA_OROTIAGITO, new EntityDropEntry(new ResourceLocation("twilightforest", "hydra"),
                SlashBlade.prefix("orotiagito_rust"), 0.3F, false));
    }

    private static ResourceKey<EntityDropEntry> register(String id) {
        return ResourceKey.create(EntityDropEntry.REGISTRY_KEY, SlashBlade.prefix(id));
    }
}
