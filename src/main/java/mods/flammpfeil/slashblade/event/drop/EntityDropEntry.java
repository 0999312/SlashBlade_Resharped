package mods.flammpfeil.slashblade.event.drop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class EntityDropEntry {
    public static final Codec<EntityDropEntry> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf("entity_type").forGetter(EntityDropEntry::getEntityType),
                    ResourceLocation.CODEC.fieldOf("blade").forGetter(EntityDropEntry::getBladeName),
                    Codec.FLOAT.optionalFieldOf("drop_rate", 1.0F).forGetter(EntityDropEntry::getDropRate),
                    Codec.BOOL.optionalFieldOf("request_slashblade", false)
                            .forGetter(EntityDropEntry::isRequestSlashBladeKill),
                    Codec.BOOL.optionalFieldOf("drop_fixed", false).forGetter(EntityDropEntry::isDropFixedPoint),
                    Vec3.CODEC.optionalFieldOf("drop_point", new Vec3(0, 0, 0)).forGetter(EntityDropEntry::getDropPoint)

            ).apply(instance, EntityDropEntry::new));

    public static final ResourceKey<Registry<EntityDropEntry>> REGISTRY_KEY = ResourceKey
            .createRegistryKey(SlashBlade.prefix("entity_drop"));

    private final ResourceLocation entityType;
    private final ResourceLocation bladeName;
    private final float dropRate;
    private final boolean requestSlashBladeKill;
    private final boolean dropFixedPoint;
    private final Vec3 dropPoint;

    public EntityDropEntry(ResourceLocation entityType, ResourceLocation bladeName, float dropRate) {
        this(entityType, bladeName, dropRate, true, false, new Vec3(0, 0, 0));
    }

    public EntityDropEntry(ResourceLocation entityType, ResourceLocation bladeName, float dropRate, boolean request) {
        this(entityType, bladeName, dropRate, request, false, new Vec3(0, 0, 0));
    }

    public EntityDropEntry(ResourceLocation entityType, ResourceLocation bladeName, float dropRate, boolean request,
                           boolean pointFixed, Vec3 point) {
        this.entityType = entityType;
        this.bladeName = bladeName;
        this.dropRate = dropRate;
        this.requestSlashBladeKill = request;
        this.dropFixedPoint = pointFixed;
        this.dropPoint = point;
    }

    public ResourceLocation getBladeName() {
        return bladeName;
    }

    public ResourceLocation getEntityType() {
        return entityType;
    }

    public float getDropRate() {
        return dropRate;
    }

    public boolean isRequestSlashBladeKill() {
        return requestSlashBladeKill;
    }

    public boolean isDropFixedPoint() {
        return dropFixedPoint;
    }

    public Vec3 getDropPoint() {
        return dropPoint;
    }
}
