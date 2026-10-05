package mods.flammpfeil.slashblade.registry.slashblade;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeCreativeGroup;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import net.minecraft.Util;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class SlashBladeDefinition {
    
    public static final Codec<SlashBladeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("item", SlashBlade.prefix("slashblade"))
                .forGetter(SlashBladeDefinition::getItemName),
            ResourceLocation.CODEC.fieldOf("name").forGetter(SlashBladeDefinition::getName),
            RenderDefinition.CODEC.fieldOf("render").forGetter(SlashBladeDefinition::getRenderDefinition),
            PropertiesDefinition.CODEC.fieldOf("properties").forGetter(SlashBladeDefinition::getStateDefinition),
            EnchantmentDefinition.CODEC.listOf().optionalFieldOf("enchantments", Lists.newArrayList())
                .forGetter(SlashBladeDefinition::getEnchantments),
            ResourceLocation.CODEC.optionalFieldOf("creativeGroup", SlashBladeCreativeGroup.SLASHBLADE_GROUP.getKey().location())
                .forGetter(SlashBladeDefinition::getCreativeGroup))
        .apply(instance, SlashBladeDefinition::new));
    
    public static final ResourceKey<Registry<SlashBladeDefinition>> REGISTRY_KEY = ResourceKey
        .createRegistryKey(SlashBlade.prefix("named_blades"));
    
    private final ResourceLocation item;
    private final ResourceLocation name;
    private final RenderDefinition renderDefinition;
    private final PropertiesDefinition stateDefinition;
    private final List<EnchantmentDefinition> enchantments;
    
    private final ResourceLocation creativeGroup;
    
    public SlashBladeDefinition(ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments) {
        this(SlashBlade.prefix("slashblade"), name, renderDefinition, stateDefinition, enchantments,
            SlashBladeCreativeGroup.SLASHBLADE_GROUP.getKey().location());
    }
    
    public SlashBladeDefinition(ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments,
                                ResourceLocation creativeGroup) {
        this(SlashBlade.prefix("slashblade"), name, renderDefinition, stateDefinition, enchantments, creativeGroup);
    }
    
    public SlashBladeDefinition(ResourceLocation item, ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments) {
        this(item, name, renderDefinition, stateDefinition, enchantments,
            SlashBladeCreativeGroup.SLASHBLADE_GROUP.getKey().location());
    }
    
    
    public SlashBladeDefinition(ResourceLocation item, ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments,
                                ResourceLocation creativeGroup) {
        this.item = item;
        this.name = name;
        this.renderDefinition = renderDefinition;
        this.stateDefinition = stateDefinition;
        this.enchantments = enchantments;
        this.creativeGroup = creativeGroup;
    }
    
    public ResourceLocation getItemName() {
        return item;
    }
    
    public ResourceLocation getName() {
        return name;
    }
    
    public String getTranslationKey() {
        return Util.makeDescriptionId("item", this.getName());
    }
    
    public RenderDefinition getRenderDefinition() {
        return renderDefinition;
    }
    
    public PropertiesDefinition getStateDefinition() {
        return stateDefinition;
    }
    
    public List<EnchantmentDefinition> getEnchantments() {
        return enchantments;
    }
    
    public ItemStack getBlade() {
        return getBlade(getItem(), null);
    }
    
    public ItemStack getBlade(Item bladeItem) {
        return getBlade(bladeItem, null);
    }
    
    public ItemStack getBlade(@Nullable HolderLookup.Provider registries) {
        return getBlade(getItem(), registries);
    }
    
    public ItemStack getBlade(Item bladeItem, @Nullable HolderLookup.Provider registries) {
        if (NeoForge.EVENT_BUS.post(new SlashBladeRegistryEvent.Pre(this)).isCanceled()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack result = new ItemStack(bladeItem);
        var state = BladeStateAccess.of(result).orElseThrow();
        state.setNonEmpty();
        state.setBaseAttackModifier(this.stateDefinition.getBaseAttackModifier());
        state.setMaxDamage(this.stateDefinition.getMaxDamage());
        state.setComboRoot(this.stateDefinition.getComboRoot());
        state.setSlashArtsKey(this.stateDefinition.getSpecialAttackType());
        state.setDestructable(this.stateDefinition.isDestructable());
        
        this.stateDefinition.getSpecialEffects().forEach(state::addSpecialEffect);
        
        List<SwordType> defaultType = this.stateDefinition.getDefaultType();
        defaultType.forEach(type -> {
            switch (type) {
                case BEWITCHED -> state.setDefaultBewitched(true);
                case BROKEN -> {
                    result.setDamageValue(result.getMaxDamage() - 1);
                    state.setBroken(true);
                }
                case SEALED -> state.setSealed(true);
                default -> {
                }
            }
        });
        state.setDefaultSwordTypes(defaultType);
        
        state.setModel(this.renderDefinition.getModelName());
        state.setTexture(this.renderDefinition.getTextureName());
        state.setColorCode(this.renderDefinition.getSummonedSwordColor());
        state.setEffectColorInverse(this.renderDefinition.isSummonedSwordColorInverse());
        state.setCarryType(this.renderDefinition.getStandbyRenderType());
        if (!this.getName().equals(RequestDefinition.EMPTY_NAME)) {
            state.setTranslationKey(this.getTranslationKey());
        }
        
        for (var instance : this.enchantments) {
            result.enchant(instance.getEnchantment(), instance.getEnchantmentLevel());
        }
        if (this.stateDefinition.isUnbreakable()) {
            result.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        }
        var postRegistry = new SlashBladeRegistryEvent.Post(this, result);
        NeoForge.EVENT_BUS.post(postRegistry);
        ItemStack out = postRegistry.getBlade();
        ItemSlashBlade.updateRarity(out);
        return out;
    }
    
    public Item getItem() {
        return BuiltInRegistries.ITEM.get(this.item);
    }
    
    public ResourceLocation getCreativeGroup() {
        return creativeGroup;
    }
    
    public static final BladeComparator COMPARATOR = new BladeComparator();
    
    public static class BladeComparator implements Comparator<Reference<SlashBladeDefinition>> {
        @Override
        public int compare(Reference<SlashBladeDefinition> left, Reference<SlashBladeDefinition> right) {
            
            ResourceLocation leftKey = left.key().location();
            ResourceLocation rightKey = right.key().location();
            boolean checkSame = leftKey.getNamespace().equalsIgnoreCase(rightKey.getNamespace());
            if (!checkSame) {
                if (leftKey.getNamespace().equalsIgnoreCase(SlashBlade.MODID)) {
                    return -1;
                }
                
                if (rightKey.getNamespace().equalsIgnoreCase(SlashBlade.MODID)) {
                    return 1;
                }
            }
            String leftName = leftKey.toString();
            String rightName = rightKey.toString();
            
            return leftName.compareToIgnoreCase(rightName);
        }
    }
}
