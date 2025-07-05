package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.inputstate.InputStateCapabilityProvider;
import mods.flammpfeil.slashblade.capability.mobeffect.MobEffectCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityAttachHandler {

    static public final ResourceLocation MOBEFFECT_KEY = new ResourceLocation(SlashBlade.MODID, "mobeffect");
    static public final ResourceLocation INPUTSTATE_KEY = new ResourceLocation(SlashBlade.MODID, "inputstate");
    static public final ResourceLocation RANK_KEY = new ResourceLocation(SlashBlade.MODID, "concentration");

    @SubscribeEvent
    public void AttachCapabilities_Entity(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof LivingEntity)) {
            return;
        }

        event.addCapability(INPUTSTATE_KEY, new InputStateCapabilityProvider());
        event.addCapability(MOBEFFECT_KEY, new MobEffectCapabilityProvider());
        event.addCapability(RANK_KEY, new ConcentrationRankCapabilityProvider());
    }

}
