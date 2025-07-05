package mods.flammpfeil.slashblade.data.tag;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class SlashBladeItemTags {
    public static final TagKey<Item> PROUD_SOULS = ItemTags.create(new ResourceLocation("slashblade", "proudsouls"));
    public static final TagKey<Item> BAMBOO = ItemTags.create(new ResourceLocation("forge", "bamboo"));
    
    public static final TagKey<Item> CAN_COPY_SA = ItemTags.create(SlashBlade.prefix("can_copy_sa"));
    public static final TagKey<Item> CAN_COPY_SE = ItemTags.create(SlashBlade.prefix("can_copy_se"));
    public static final TagKey<Item> CAN_CHANGE_SA = ItemTags.create(SlashBlade.prefix("can_change_sa"));
    public static final TagKey<Item> CAN_CHANGE_SE = ItemTags.create(SlashBlade.prefix("can_change_se"));
}
