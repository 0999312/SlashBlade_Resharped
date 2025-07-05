package mods.flammpfeil.slashblade;

import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SlashBladeCreativeGroup {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, SlashBlade.MODID);

	private static final CreativeModeTab SLASHBLADE = CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.slashblade")).icon(() -> {
				ItemStack stack = new ItemStack(SBItems.slashblade);
				stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
					s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
					s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
				});
				return stack;
			}).displayItems((features, output) -> {

				output.accept(SBItems.proudsoul);
				output.accept(SBItems.proudsoul_tiny);
				output.accept(SBItems.proudsoul_ingot);
				output.accept(SBItems.proudsoul_sphere);

				output.accept(SBItems.proudsoul_crystal);
				output.accept(SBItems.proudsoul_trapezohedron);
				fillEnchantmentsSouls(output);
				fillSASpheres(output);
				output.accept(SBItems.bladestand_1);
				output.accept(SBItems.bladestand_1w);
				output.accept(SBItems.bladestand_2);
				output.accept(SBItems.bladestand_2w);
				output.accept(SBItems.bladestand_s);
				output.accept(SBItems.bladestand_v);

				output.accept(SBItems.slashblade_wood);
				output.accept(SBItems.slashblade_bamboo);
				output.accept(SBItems.slashblade_silverbamboo);
				output.accept(SBItems.slashblade_white);
				output.accept(SBItems.slashblade);

				fillBlades(features, output);
			}).build();

	public static final RegistryObject<CreativeModeTab> SLASHBLADE_GROUP = CREATIVE_MODE_TABS.register("slashblade",
			() -> SLASHBLADE);

	private static void fillBlades(CreativeModeTab.ItemDisplayParameters features, CreativeModeTab.Output output) {
		SlashBlade.getSlashBladeDefinitionRegistry(features.holders()).listElements()
				.sorted(SlashBladeDefinition.COMPARATOR).forEach(entry -> {
					if(!entry.value().getBlade().isEmpty())
						output.accept(entry.value().getBlade());
				});
	}
	
	private static void fillEnchantmentsSouls(CreativeModeTab.Output output) {
		ForgeRegistries.ENCHANTMENTS.forEach(enchantment->{
			ItemStack blade = new ItemStack(SBItems.slashblade);
			if(blade.canApplyAtEnchantingTable(enchantment)) {
				ItemStack soul = new ItemStack(SBItems.proudsoul_tiny);
				soul.enchant(enchantment, 1);
				output.accept(soul);
			}
				
		});
	}

	private static void fillSASpheres(CreativeModeTab.Output output) {
		SlashArtsRegistry.REGISTRY.get().forEach(slashArts -> {
			ResourceLocation key = SlashArtsRegistry.REGISTRY.get().getKey(slashArts);
			if (slashArts.equals(SlashArtsRegistry.NONE.get()) || key == null)
				return;
			ItemStack sphere = new ItemStack(SBItems.proudsoul_sphere);
			CompoundTag tag = new CompoundTag();
			tag.putString("SpecialAttackType", key.toString());
			sphere.setTag(tag);
			output.accept(sphere);
		});
	}
}
