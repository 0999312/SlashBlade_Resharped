package mods.flammpfeil.slashblade.data.tag;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SlashBladeEntityTypeTagProvider extends EntityTypeTagsProvider {

    public SlashBladeEntityTypeTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, String modId,
                                           @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull Provider lookupProvider) {
        this.tag(EntityTypeTags.ATTACKABLE_BLACKLIST)
                .add(EntityType.VILLAGER)
                .addOptional(new ResourceLocation("touhou_little_maid", "maid"));
    }

    public static class EntityTypeTags {
        public static final TagKey<EntityType<?>> ATTACKABLE_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE,
                SlashBlade.prefix("blacklist/attackable"));
    }
}
