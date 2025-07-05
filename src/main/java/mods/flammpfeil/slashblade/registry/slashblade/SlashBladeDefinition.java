package mods.flammpfeil.slashblade.registry.slashblade;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.Util;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

public class SlashBladeDefinition {

	public static final Codec<SlashBladeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("item", SlashBlade.prefix("slashblade"))
					.forGetter(SlashBladeDefinition::getItemName),
			ResourceLocation.CODEC.fieldOf("name").forGetter(SlashBladeDefinition::getName),
			RenderDefinition.CODEC.fieldOf("render").forGetter(SlashBladeDefinition::getRenderDefinition),
			PropertiesDefinition.CODEC.fieldOf("properties").forGetter(SlashBladeDefinition::getStateDefinition),
			EnchantmentDefinition.CODEC.listOf().optionalFieldOf("enchantments", Lists.newArrayList())
					.forGetter(SlashBladeDefinition::getEnchantments))
			.apply(instance, SlashBladeDefinition::new));

	public static final ResourceKey<Registry<SlashBladeDefinition>> REGISTRY_KEY = ResourceKey
			.createRegistryKey(SlashBlade.prefix("named_blades"));

	private final ResourceLocation item;
	private final ResourceLocation name;
	private final RenderDefinition renderDefinition;
	private final PropertiesDefinition stateDefinition;
	private final List<EnchantmentDefinition> enchantments;

	public SlashBladeDefinition(ResourceLocation name, RenderDefinition renderDefinition,
			PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments) {
		this(SlashBlade.prefix("slashblade"), name, renderDefinition, stateDefinition, enchantments);
	}

	public SlashBladeDefinition(ResourceLocation item, ResourceLocation name, RenderDefinition renderDefinition,
			PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments) {
		this.item = item;
		this.name = name;
		this.renderDefinition = renderDefinition;
		this.stateDefinition = stateDefinition;
		this.enchantments = enchantments;
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
		return getBlade(getItem());
	}

	public ItemStack getBlade(Item bladeItem) {
		
		if(MinecraftForge.EVENT_BUS.post(new SlashBladeRegistryEvent.Pre(this)))
			return ItemStack.EMPTY;
		
		ItemStack result = new ItemStack(bladeItem);
		var state = result.getCapability(ItemSlashBlade.BLADESTATE).orElse(new SlashBladeState(result));
		state.setNonEmpty();
		state.setBaseAttackModifier(this.stateDefinition.getBaseAttackModifier());
		state.setMaxDamage(this.stateDefinition.getMaxDamage());
		state.setComboRoot(this.stateDefinition.getComboRoot());
		state.setSlashArtsKey(this.stateDefinition.getSpecialAttackType());

		this.stateDefinition.getSpecialEffects().forEach(state::addSpecialEffect);

		this.stateDefinition.getDefaultType().forEach(type -> {
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

		state.setModel(this.renderDefinition.getModelName());
		state.setTexture(this.renderDefinition.getTextureName());
		state.setColorCode(this.renderDefinition.getSummonedSwordColor());
		state.setEffectColorInverse(this.renderDefinition.isSummonedSwordColorInverse());
		state.setCarryType(this.renderDefinition.getStandbyRenderType());
		if (!this.getName().equals(SlashBlade.prefix("none")))
			state.setTranslationKey(this.getTranslationKey());

		result.getOrCreateTag().put("bladeState", state.serializeNBT());

		for (var instance : this.enchantments) {
			var enchantment = ForgeRegistries.ENCHANTMENTS.getValue(instance.getEnchantmentID());
			result.enchant(enchantment, instance.getEnchantmentLevel());

		}
		var postRegistry = new SlashBladeRegistryEvent.Post(this, result);
		MinecraftForge.EVENT_BUS.post(postRegistry);
		return postRegistry.getBlade();
	}

	public Item getItem() {
		@Nullable
		Item value = ForgeRegistries.ITEMS.getValue(this.item);
		if(value == null)
			return SBItems.slashblade;
		return value;
	}
	
	

	public static final BladeComparator COMPARATOR = new BladeComparator();

	private static class BladeComparator implements Comparator<Reference<SlashBladeDefinition>> {
		@Override
		public int compare(Reference<SlashBladeDefinition> left, Reference<SlashBladeDefinition> right) {

			ResourceLocation leftKey = left.key().location();
			ResourceLocation rightKey = right.key().location();
			boolean checkSame = leftKey.getNamespace().equalsIgnoreCase(rightKey.getNamespace());
			if (!checkSame) {
				if (leftKey.getNamespace().equalsIgnoreCase(SlashBlade.MODID))
					return -1;

				if (rightKey.getNamespace().equalsIgnoreCase(SlashBlade.MODID))
					return 1;
			}
			String leftName = leftKey.toString();
			String rightName = rightKey.toString();

			return leftName.compareToIgnoreCase(rightName);
		}
	}
}
