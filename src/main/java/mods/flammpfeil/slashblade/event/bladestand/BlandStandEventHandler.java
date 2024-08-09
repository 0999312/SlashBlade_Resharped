package mods.flammpfeil.slashblade.event.bladestand;

import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber()
public class BlandStandEventHandler {
	@SubscribeEvent
	public static void eventChangeSE(SlashBladeEvent.BladeStandAttackEvent event) {
		if(!(event.getDamageSource().getEntity() instanceof ServerPlayer))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);


	}
	
	@SubscribeEvent
	public static void eventChangeSA(SlashBladeEvent.BladeStandAttackEvent event) {
		if(!(event.getDamageSource().getEntity() instanceof ServerPlayer))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		CompoundTag tag = stack.getTag();

		if (!stack.is(SBItems.proudsoul_sphere) || tag == null || !tag.contains("SpecialAttackType"))
			return;

		ResourceLocation SAKey = new ResourceLocation(tag.getString("SpecialAttackType"));
		if (!SlashArtsRegistry.REGISTRY.get().containsKey(SAKey))
			return;

		ItemStack blade = event.getBlade();
		blade.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
			if (!SAKey.equals(state.getSlashArtsKey())) {
				state.setSlashArtsKey(SAKey);
				player.level().playSound(event.getBladeStand(), event.getBladeStand().getPos(),
						SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
				Minecraft.getInstance().particleEngine.createTrackingEmitter(event.getBladeStand(),
						ParticleTypes.PORTAL);

				if (!player.isCreative()){
					stack.shrink(1);
				}
			}
		});
		event.setCanceled(true);//防止掉落拔刀
		
	}
	
	@SubscribeEvent
	public static void eventCopySE(SlashBladeEvent.BladeStandAttackEvent event) {
		if(!(event.getDamageSource().getEntity() instanceof ServerPlayer))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		
		
	}
	
	@SubscribeEvent
	public static void eventCopySA(SlashBladeEvent.BladeStandAttackEvent event) {
		if(!(event.getDamageSource().getEntity() instanceof ServerPlayer))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		
		
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void eventProudSoulEnchantment(SlashBladeEvent.BladeStandAttackEvent event) {
		if(!(event.getDamageSource().getEntity() instanceof ServerPlayer))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		
		ItemStack blade = event.getBlade();

		if (!stack.is(SBItems.proudsoul_tiny) && !stack.is(SBItems.proudsoul) && !stack.is(SBItems.proudsoul_ingot))
			return;

		if(!stack.isEnchanted())
			return ;

		RandomSource random = player.getRandom();
		Map<Enchantment, Integer> currentBladeEnchantments = EnchantmentHelper.getEnchantments(blade);
		AtomicBoolean canEnchanted = new AtomicBoolean(false);
		stack.getAllEnchantments().forEach((enchantment,level)->{
			if(!blade.canApplyAtEnchantingTable(enchantment))
				return;

			if (currentBladeEnchantments.containsKey(enchantment)){
				if (currentBladeEnchantments.get(enchantment) >= enchantment.getMaxLevel())//已达该附魔的最大等级则不添加
					return;
				currentBladeEnchantments.put(enchantment, currentBladeEnchantments.get(enchantment) + level);
				canEnchanted.set(true);
			}else {
				currentBladeEnchantments.put(enchantment, level);
				canEnchanted.set(true);
			}

		});
		if (canEnchanted.get()){
			EnchantmentHelper.setEnchantments(currentBladeEnchantments, blade);

			player.level().playSound(event.getBladeStand(), event.getBladeStand().getPos(),
					SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, random.nextFloat() * 0.1F + 0.9F);
			Minecraft.getInstance().particleEngine.createTrackingEmitter(event.getBladeStand(),
					ParticleTypes.ENCHANTED_HIT);

			if (!player.isCreative()){
				stack.shrink(1);
			}
		}
		event.setCanceled(true);//防止掉落拔刀
	}
}
