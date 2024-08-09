package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Map;

public class BladeStandEntity extends ItemFrame implements IEntityAdditionalSpawnData {

	public Item currentType = null;
	public ItemStack currentTypeStack = ItemStack.EMPTY;

	public BladeStandEntity(EntityType<? extends BladeStandEntity> p_i50224_1_, Level p_i50224_2_) {
		super(p_i50224_1_, p_i50224_2_);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		String standTypeStr;
		if (this.currentType != null) {
			standTypeStr = ForgeRegistries.ITEMS.getKey(this.currentType).toString();
		} else {
			standTypeStr = "";
		}
		compound.putString("StandType", standTypeStr);

		compound.putByte("Pose", (byte) this.getPose().ordinal());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.currentType = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("StandType")));

		this.setPose(Pose.values()[compound.getByte("Pose") % Pose.values().length]);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		CompoundTag tag = new CompoundTag();
		this.addAdditionalSaveData(tag);
		buffer.writeNbt(tag);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		CompoundTag tag = additionalData.readNbt();
		this.readAdditionalSaveData(tag);
	}

	public static BladeStandEntity createInstanceFromPos(Level worldIn, BlockPos placePos, Direction dir, Item type) {
		BladeStandEntity e = new BladeStandEntity(SlashBlade.RegistryEvents.BladeStand, worldIn);

		e.pos = placePos;
		e.setDirection(dir);
		e.currentType = type;

		return e;
	}

	public static BladeStandEntity createInstance(PlayMessages.SpawnEntity spawnEntity, Level world) {
		return new BladeStandEntity(SlashBlade.RegistryEvents.BladeStand, world);
	}

	@Nullable
	@Override
	public ItemEntity spawnAtLocation(ItemLike iip) {
		if (iip == Items.ITEM_FRAME) {
			if (this.currentType == null || this.currentType == Items.AIR)
				return null;

			iip = this.currentType;
		}
		return super.spawnAtLocation(iip);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float cat) {
		ItemStack blade = this.getItem();
		
		if (blade.isEmpty())
			return super.hurt(damageSource, cat);
		
		if(!blade.getCapability(ItemSlashBlade.BLADESTATE).isPresent())
			return super.hurt(damageSource, cat);
		
		ISlashBladeState state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
		
		if(MinecraftForge.EVENT_BUS.post(new SlashBladeEvent.BladeStandAttackEvent(blade, state, this, damageSource)))
			return true;//事件取消则不掉落拔刀

		return super.hurt(damageSource, cat);
		
//		if (entity instanceof Player player) {	
//			
//			Level level = player.level();
//			BlockPos pos = this.pos;
//			ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
//			float probability = 0f; 
//			boolean canEnchant = false;
//			RandomSource random = player.getRandom();
//			Enchantment enchantment;
//			var specialActionStat = new Object() {
//				boolean SA_duplicated = false;
//				boolean SA_changed = false;
//				boolean enchanted = false;
//			};
//			// SA realization
//			if (stack.is(SBItems.proudsoul_sphere) && stack.getTag() != null) {
//				CompoundTag tag = stack.getTag();
//				if (tag.contains("SpecialAttackType")) {
//					ResourceLocation SAKey = new ResourceLocation(tag.getString("SpecialAttackType"));
//					if (SlashArtsRegistry.REGISTRY.get().containsKey(SAKey)) {
//						blade.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
//							ResourceLocation currentSA = state.getSlashArtsKey();
//							if (!SAKey.equals(currentSA)) {
//								state.setSlashArtsKey(SAKey);
//								level.playSound(this, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
//								Minecraft.getInstance().particleEngine.createTrackingEmitter(this,
//										ParticleTypes.PORTAL);
//								if (!player.isCreative())
//									stack.shrink(1);
//								specialActionStat.SA_changed = true;
//							}
//						});
//					}
//				}
//			}
//			// Enchanting via left click holding proudsoul-typed items and SA duplicating
//			if (stack.isEnchanted()) {
//				ArrayList<Enchantment> enchantments = new ArrayList<>(
//						EnchantmentHelper.getEnchantments(stack).keySet());
//				Map<Enchantment, Integer> currentBladeEnchantments = blade.getAllEnchantments();
//				if (stack.is(SBItems.proudsoul_tiny)) {
//					canEnchant = true;
//					probability = 0.25f;
//				} else if (stack.is(SBItems.proudsoul)) {
//					canEnchant = true;
//					probability = 0.5f;
//				} else if (stack.is(SBItems.proudsoul_ingot)) {
//					blade.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
//						ResourceLocation SA = state.getSlashArtsKey();
//						if (random.nextFloat() <= 0.8f && SA != null && !SA.equals(SlashArtsRegistry.NONE.getId())) {
//							if (!enchantments.isEmpty()) {
//								ItemStack orb = new ItemStack(SBItems.proudsoul_sphere);
//								CompoundTag tag = new CompoundTag();
//								tag.putString("SpecialAttackType", state.getSlashArtsKey().toString());
//								orb.setTag(tag);
//								if (!player.isCreative())
//									stack.shrink(1);
//								level.playSound(this, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1f, 1f);
//								Minecraft.getInstance().particleEngine.createTrackingEmitter(this,
//										ParticleTypes.PORTAL);
//								player.drop(orb, true);
//								specialActionStat.SA_duplicated = true;
//							}
//						}
//					});
//					if (specialActionStat.SA_duplicated)
//						return true;
//					canEnchant = true;
//					probability = 0.75f;
//				} else if (stack.is(SBItems.proudsoul_sphere) || stack.is(SBItems.proudsoul_crystal)
//						|| stack.is(SBItems.proudsoul_trapezohedron)) {
//					canEnchant = true;
//					probability = 1f;
//				}
//
//				if (!canEnchant)
//					return super.hurt(damageSource, cat);
//				enchantment = enchantments.get(random.nextInt(0, enchantments.size()));
//				if (random.nextFloat() <= probability) {
//					int enchantLevel = EnchantmentHelper.getTagEnchantmentLevel(enchantment, blade) + 1;
//					currentBladeEnchantments.put(enchantment, enchantLevel);
//					EnchantmentHelper.setEnchantments(currentBladeEnchantments, blade);
//					level.playSound(this, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F,
//							random.nextFloat() * 0.1F + 0.9F);
//					Minecraft.getInstance().particleEngine.createTrackingEmitter(this, ParticleTypes.ENCHANTED_HIT);
//				}
//				if (!player.isCreative())
//					stack.shrink(1);
//
//				return true;
//			}
//			if (specialActionStat.SA_duplicated || specialActionStat.SA_changed || specialActionStat.enchanted)
//				return true;
//		}


	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		InteractionResult result = InteractionResult.PASS;
		if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
			ItemStack itemstack = player.getItemInHand(hand);
			if (player.isShiftKeyDown() && !this.getItem().isEmpty()) {
				Pose current = this.getPose();
				int newIndex = (current.ordinal() + 1) % Pose.values().length;
				this.setPose(Pose.values()[newIndex]);
				result = InteractionResult.SUCCESS;
			} else if ((!itemstack.isEmpty() && itemstack.getItem() instanceof ItemSlashBlade)
					|| (itemstack.isEmpty() && !this.getItem().isEmpty())) {

				if (this.getItem().isEmpty()) {
					if (!this.isRemoved()) {
						this.setItem(itemstack);
						if (!player.getAbilities().instabuild) {
							itemstack.shrink(1);
						}
						this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
						result = InteractionResult.SUCCESS;
					}
				} else {
					ItemStack displayed = this.getItem().copy();

					this.setItem(itemstack);
					player.setItemInHand(hand, displayed);

					this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
					result = InteractionResult.SUCCESS;

				}

			} else {
				this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
				this.setRotation(this.getRotation() + 1);
				result = InteractionResult.SUCCESS;
			}
		}
		return result;
	}

	protected ItemStack getFrameItemStack() {
		return new ItemStack(currentType);
	}

}
