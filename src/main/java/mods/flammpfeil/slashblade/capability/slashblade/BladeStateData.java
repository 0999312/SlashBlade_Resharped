package mods.flammpfeil.slashblade.capability.slashblade;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record BladeStateData(
    String translationKey,
    float baseAttackModifier,
    int proudSoul,
    int killCount,
    int refine,
    boolean broken,
    boolean sealed,
    ResourceLocation slashArtsKey,
    boolean defaultBewitched,
    Optional<List<SwordType>> defaultSwordTypes,
    boolean destructable,
    ResourceLocation comboRoot,
    CarryType carryType,
    int effectColor,
    boolean effectColorInverse,
    Vec3 adjust,
    Optional<ResourceLocation> texture,
    Optional<ResourceLocation> model,
    List<ResourceLocation> specialEffects
) {
    public BladeStateData {
        specialEffects = List.copyOf(specialEffects);
    }
    
    static final ResourceLocation DEFAULT_SLASH_ARTS = ResourceLocation.fromNamespaceAndPath("slashblade", "judgement_cut");
    static final ResourceLocation DEFAULT_COMBO_ROOT = ResourceLocation.fromNamespaceAndPath("slashblade", "standby");
    
    public static final BladeStateData DEFAULT = new BladeStateData(
        "", 4.0F, 0, 0, 0,
        false, false,
        DEFAULT_SLASH_ARTS,
        false,
        Optional.empty(),
        false,
        DEFAULT_COMBO_ROOT,
        CarryType.PSO2,
        0xFF3333FF,
        false,
        Vec3.ZERO,
        Optional.empty(),
        Optional.empty(),
        Collections.emptyList()
    );
    
    private static final Codec<Vec3> VEC3_CODEC = Codec.DOUBLE.listOf().comapFlatMap(
        list -> {
            if (list.size() < 3) {
                return com.mojang.serialization.DataResult.error(() -> "Need 3 doubles");
            }
            return com.mojang.serialization.DataResult.success(new Vec3(list.get(0), list.get(1), list.get(2)));
        },
        vec -> List.of(vec.x, vec.y, vec.z)
    );
    
    private record CoreFields(
        String translationKey,
        float baseAttackModifier,
        int proudSoul,
        int killCount,
        int refine,
        boolean broken,
        boolean sealed,
        ResourceLocation slashArtsKey,
        boolean defaultBewitched,
        Optional<List<SwordType>> defaultSwordTypes,
        boolean destructable
    ) {
    }
    
    private record RenderFields(
        ResourceLocation comboRoot,
        CarryType carryType,
        int effectColor,
        boolean effectColorInverse,
        Vec3 adjust,
        Optional<ResourceLocation> texture,
        Optional<ResourceLocation> model,
        List<ResourceLocation> specialEffects
    ) {
    }
    
    private static final MapCodec<CoreFields> CORE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("translationKey").forGetter(CoreFields::translationKey),
        Codec.FLOAT.fieldOf("baseAttackModifier").forGetter(CoreFields::baseAttackModifier),
        Codec.INT.fieldOf("proudSoul").forGetter(CoreFields::proudSoul),
        Codec.INT.fieldOf("killCount").forGetter(CoreFields::killCount),
        Codec.INT.fieldOf("refine").forGetter(CoreFields::refine),
        Codec.BOOL.fieldOf("broken").forGetter(CoreFields::broken),
        Codec.BOOL.fieldOf("sealed").forGetter(CoreFields::sealed),
        ResourceLocation.CODEC.fieldOf("slashArtsKey").forGetter(CoreFields::slashArtsKey),
        Codec.BOOL.fieldOf("defaultBewitched").forGetter(CoreFields::defaultBewitched),
        SwordType.CODEC.listOf().optionalFieldOf("defaultSwordTypes").forGetter(CoreFields::defaultSwordTypes),
        Codec.BOOL.optionalFieldOf("destructable", false).forGetter(CoreFields::destructable)
    ).apply(instance, CoreFields::new));
    
    private static final MapCodec<RenderFields> RENDER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("comboRoot").forGetter(RenderFields::comboRoot),
        CarryType.CODEC.fieldOf("carryType").forGetter(RenderFields::carryType),
        Codec.INT.fieldOf("effectColor").forGetter(RenderFields::effectColor),
        Codec.BOOL.fieldOf("effectColorInverse").forGetter(RenderFields::effectColorInverse),
        VEC3_CODEC.fieldOf("adjust").forGetter(RenderFields::adjust),
        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(RenderFields::texture),
        ResourceLocation.CODEC.optionalFieldOf("model").forGetter(RenderFields::model),
        ResourceLocation.CODEC.listOf().fieldOf("specialEffects").forGetter(RenderFields::specialEffects)
    ).apply(instance, RenderFields::new));
    
    public static final Codec<BladeStateData> CODEC = Codec.mapPair(CORE_CODEC, RENDER_CODEC)
        .xmap(
            pair -> new BladeStateData(
                pair.getFirst().translationKey(),
                pair.getFirst().baseAttackModifier(),
                pair.getFirst().proudSoul(),
                pair.getFirst().killCount(),
                pair.getFirst().refine(),
                pair.getFirst().broken(),
                pair.getFirst().sealed(),
                pair.getFirst().slashArtsKey(),
                pair.getFirst().defaultBewitched(),
                pair.getFirst().defaultSwordTypes(),
                pair.getFirst().destructable(),
                pair.getSecond().comboRoot(),
                pair.getSecond().carryType(),
                pair.getSecond().effectColor(),
                pair.getSecond().effectColorInverse(),
                pair.getSecond().adjust(),
                pair.getSecond().texture(),
                pair.getSecond().model(),
                pair.getSecond().specialEffects()
            ),
            data -> Pair.of(
                new CoreFields(
                    data.translationKey(),
                    data.baseAttackModifier(),
                    data.proudSoul(),
                    data.killCount(),
                    data.refine(),
                    data.broken(),
                    data.sealed(),
                    data.slashArtsKey(),
                    data.defaultBewitched(),
                    data.defaultSwordTypes(),
                    data.destructable()
                ),
                new RenderFields(
                    data.comboRoot(),
                    data.carryType(),
                    data.effectColor(),
                    data.effectColorInverse(),
                    data.adjust(),
                    data.texture(),
                    data.model(),
                    data.specialEffects()
                )
            )
        )
        .codec();
    
    public static final StreamCodec<RegistryFriendlyByteBuf, BladeStateData> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);
    
    public BladeStateData withSpecialEffects(@Nullable List<ResourceLocation> effects) {
        return new BladeStateData(translationKey, baseAttackModifier, proudSoul, killCount, refine,
            broken, sealed, slashArtsKey, defaultBewitched, defaultSwordTypes, destructable, comboRoot, carryType,
            effectColor, effectColorInverse, adjust, texture, model,
            effects != null ? effects : Collections.emptyList());
    }
}
