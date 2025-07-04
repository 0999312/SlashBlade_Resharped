package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.registry.specialeffects.WitherEdge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class SpecialEffectsRegistry {
    public static final DeferredRegister<SpecialEffect> SPECIAL_EFFECT = DeferredRegister.create(SpecialEffect.REGISTRY_KEY,
            SlashBlade.MODID);

    public static final Supplier<IForgeRegistry<SpecialEffect>> REGISTRY = SPECIAL_EFFECT.makeRegistry(RegistryBuilder::new);
    public static final RegistryObject<SpecialEffect> WITHER_EDGE = SPECIAL_EFFECT.register("wither_edge",
            WitherEdge::new);
}
