package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JEICompat implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return SlashBlade.prefix(SlashBlade.MODID);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(SBItems.slashblade,
				(stack, context) -> {
					//同步nbt到Cap
					stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(cap -> {
								cap.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"));
							}
					);
					return stack.getCapability(ItemSlashBlade.BLADESTATE)
							.map(cap -> cap.getTranslationKey())
							.orElse("");
				});
	}

}
