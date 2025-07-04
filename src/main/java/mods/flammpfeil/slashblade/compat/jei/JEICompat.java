package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEICompat implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SlashBlade.prefix(SlashBlade.MODID);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(SBItems.slashblade,
                (stack, context) -> {
                    //同步nbt到Cap
                    stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(cap -> cap.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"))
                    );
                    return stack.getCapability(ItemSlashBlade.BLADESTATE)
                            .map(ISlashBladeState::getTranslationKey)
                            .orElse("");
                });
    }

}
