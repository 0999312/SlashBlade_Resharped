package mods.flammpfeil.slashblade.event.bladestand;

import java.util.Map;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber()
public class BlandStandEventHandler {
	@SubscribeEvent
	public static void eventKoseki(SlashBladeEvent.BladeStandAttackEvent event) {
		var slashBladeDefinitionRegistry = SlashBlade.getSlashBladeDefinitionRegistry(event.getBladeStand().level());
		if(!slashBladeDefinitionRegistry.containsKey(SlashBladeBuiltInRegistry.KOSEKI.location()))
			return;
		if (!(event.getDamageSource().getEntity() instanceof WitherBoss))
			return;
		if(!event.getDamageSource().is(DamageTypeTags.IS_EXPLOSION))
			return;
		var in = SlashBladeIngredient.of(RequestDefinition.Builder.newInstance().build());
		if(!in.test(event.getBlade()))
			return;
		event.getBladeStand().setItem(slashBladeDefinitionRegistry.get(SlashBladeBuiltInRegistry.KOSEKI).getBlade());
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void eventChangeSE(SlashBladeEvent.BladeStandAttackEvent event) {
		if (!(event.getDamageSource().getEntity() instanceof ServerPlayer))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack blade = event.getBlade();
		if (blade.isEmpty())
			return;
		if (!stack.is(SBItems.proudsoul_crystal))
			return;
		var world = player.level();
		var state = event.getSlashBladeState();
		
		if (stack.getTag() == null)
			return;

		CompoundTag tag = stack.getTag();
		if (tag.contains("SpecialEffectType")) {
			var bladeStand = event.getBladeStand();
			ResourceLocation SEKey = new ResourceLocation(tag.getString("SpecialEffectType"));
			if (!(SpecialEffectsRegistry.REGISTRY.get().containsKey(SEKey)))
				return;
			if (state.hasSpecialEffect(SEKey))
				return;
			state.addSpecialEffect(SEKey);
			RandomSource random = player.getRandom();
			world.playSound(bladeStand, bladeStand.getPos(),
					SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
			for(int i = 0; i < 32; ++i) {
				if(player.level().isClientSide())
					break;
				double xDist = (random.nextFloat() * 2.0F - 1.0F);
				double yDist = (random.nextFloat() * 2.0F - 1.0F);
				double zDist = (random.nextFloat() * 2.0F - 1.0F);
				if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
					double x = bladeStand.getX(xDist / 4.0D);
					double y = bladeStand.getY(0.5D + yDist / 4.0D);
					double z = bladeStand.getZ(zDist / 4.0D);
					((ServerLevel)world).sendParticles(ParticleTypes.PORTAL, x, y, z,0, xDist, yDist + 0.2D, zDist,1);
				}
			}
			if (!player.isCreative())
				stack.shrink(1);
			event.setCanceled(true);
		}
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

				RandomSource random = player.getRandom();
				BladeStandEntity bladeStand = event.getBladeStand();
				player.level().playSound(bladeStand, bladeStand.getPos(),
						SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
				for(int i = 0; i < 32; ++i) {
					if(player.level().isClientSide())
						break;
					double xDist = (random.nextFloat() * 2.0F - 1.0F);
					double yDist = (random.nextFloat() * 2.0F - 1.0F);
					double zDist = (random.nextFloat() * 2.0F - 1.0F);
					if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
						double x = bladeStand.getX(xDist / 4.0D);
						double y = bladeStand.getY(0.5D + yDist / 4.0D);
						double z = bladeStand.getZ(zDist / 4.0D);
						((ServerLevel)player.level()).sendParticles(ParticleTypes.PORTAL, x, y, z,0, xDist, yDist + 0.2D, zDist,1);
					}
				}

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
		ItemStack blade = event.getBlade();
		if (blade.isEmpty())
			return;
		if (!stack.is(SBItems.proudsoul_crystal))
			return;
		var world = player.level();
		var state = event.getSlashBladeState();
		var bladeStand = event.getBladeStand();
		var specialEffects = state.getSpecialEffects();

		for (var se : specialEffects) {
			if (!SpecialEffectsRegistry.REGISTRY.get().containsKey(se))
				continue;
			if (!SpecialEffectsRegistry.REGISTRY.get().getValue(se).isCopiable())
				continue;
			ItemStack orb = new ItemStack(SBItems.proudsoul_crystal);
			CompoundTag tag = new CompoundTag();
			tag.putString("SpecialEffectType", se.toString());
			orb.setTag(tag);
			if (!player.isCreative())
				stack.shrink(1);
			RandomSource random = player.getRandom();
			world.playSound(bladeStand, bladeStand.getPos(),
					SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
			for(int i = 0; i < 32; ++i) {
				if(world.isClientSide())
					break;
				double xDist = (random.nextFloat() * 2.0F - 1.0F);
				double yDist = (random.nextFloat() * 2.0F - 1.0F);
				double zDist = (random.nextFloat() * 2.0F - 1.0F);
				if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
					double x = bladeStand.getX(xDist / 4.0D);
					double y = bladeStand.getY(0.5D + yDist / 4.0D);
					double z = bladeStand.getZ(zDist / 4.0D);
					((ServerLevel)world).sendParticles(ParticleTypes.PORTAL, x, y, z,0, xDist, yDist + 0.2D, zDist,1);
				}
			}
			player.drop(orb, true);
			if(SpecialEffectsRegistry.REGISTRY.get().getValue(se).isRemovable())
				state.removeSpecialEffect(se);
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public static void eventCopySA(SlashBladeEvent.BladeStandAttackEvent event) {
		if (!(event.getDamageSource().getEntity() instanceof Player))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack blade = event.getBlade();
		if (blade.isEmpty())
			return;
		if (!stack.is(SBItems.proudsoul_ingot) || !stack.isEnchanted())
			return;
		var world = player.level();
		var state = event.getSlashBladeState();
		var bladeStand = event.getBladeStand();
		var enchantments = EnchantmentHelper.getEnchantments(stack).keySet();
		ResourceLocation SA = state.getSlashArtsKey();
		if (SA != null && !SA.equals(SlashArtsRegistry.NONE.getId())) {
			ItemStack orb = new ItemStack(SBItems.proudsoul_sphere);
			for (Enchantment e : enchantments)
            {
			if (EnchantmentHelper.getTagEnchantmentLevel(e, blade) < e.getMaxLevel())
				return;
            }
			CompoundTag tag = new CompoundTag();
			tag.putString("SpecialAttackType", state.getSlashArtsKey().toString());
			orb.setTag(tag);

			if (!player.isCreative())
				stack.shrink(1);
			world.playSound(bladeStand, bladeStand.getPos(), SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
			RandomSource random = player.getRandom();
			for(int i = 0; i < 32; ++i) {
				if(world.isClientSide())
					break;
				double xDist = (random.nextFloat() * 2.0F - 1.0F);
				double yDist = (random.nextFloat() * 2.0F - 1.0F);
				double zDist = (random.nextFloat() * 2.0F - 1.0F);
				if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
					double x = bladeStand.getX(xDist / 4.0D);
					double y = bladeStand.getY(0.5D + yDist / 4.0D);
					double z = bladeStand.getZ(zDist / 4.0D);
					((ServerLevel)world).sendParticles(ParticleTypes.PORTAL, x, y, z,0, xDist, yDist + 0.2D, zDist,1);
				}
			}
			player.drop(orb, true);
			event.setCanceled(true);
		}
	
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void eventProudSoulEnchantment(SlashBladeEvent.BladeStandAttackEvent event) {
		if (!(event.getDamageSource().getEntity() instanceof Player))
			return;
		Player player = (Player) event.getDamageSource().getEntity();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack blade = event.getBlade();

		if (blade.isEmpty())
			return;

		if (!stack.isEnchanted())
			return;
		
		var world = player.level();
		var random = world.getRandom();
		var bladeStand = event.getBladeStand();
		Map<Enchantment, Integer> currentBladeEnchantments = blade.getAllEnchantments();
		stack.getAllEnchantments().forEach((enchantment, level) -> {
			if(event.isCanceled()) 
				return;
			if (!blade.canApplyAtEnchantingTable(enchantment))
				return;

			var probability = 1.0F;
			if (stack.is(SBItems.proudsoul_tiny))
				probability = 0.25F;
			if (stack.is(SBItems.proudsoul))
				probability = 0.5F;
			if (stack.is(SBItems.proudsoul_ingot))
				probability = 0.75F;
			if (random.nextFloat() <= probability) {
				int enchantLevel = Math.min(enchantment.getMaxLevel(), EnchantmentHelper.getTagEnchantmentLevel(enchantment, blade) + 1);
				currentBladeEnchantments.put(enchantment, enchantLevel);
				EnchantmentHelper.setEnchantments(currentBladeEnchantments, blade);
				world.playSound(bladeStand, bladeStand.getPos(), SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
				for(int i = 0; i < 32; ++i) {
					if(player.level().isClientSide())
						break;
					double xDist = (random.nextFloat() * 2.0F - 1.0F);
					double yDist = (random.nextFloat() * 2.0F - 1.0F);
					double zDist = (random.nextFloat() * 2.0F - 1.0F);
					if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
						double x = bladeStand.getX(xDist / 4.0D);
						double y = bladeStand.getY(0.5D + yDist / 4.0D);
						double z = bladeStand.getZ(zDist / 4.0D);
						((ServerLevel)world).sendParticles(ParticleTypes.PORTAL, x, y, z,0, xDist, yDist + 0.2D, zDist,1);
					}
				}
			}
			if (!player.isCreative())
				stack.shrink(1);
			event.setCanceled(true);
		});
	}
}
