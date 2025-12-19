package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BladeStandEntity extends ItemFrame implements IEntityAdditionalSpawnData {

    public Item currentType = null;
    public ItemStack currentTypeStack = ItemStack.EMPTY;

    public BladeStandEntity(EntityType<? extends BladeStandEntity> p_i50224_1_, Level p_i50224_2_) {
        super(p_i50224_1_, p_i50224_2_);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        String standTypeStr;
        ResourceLocation itemsKey = ForgeRegistries.ITEMS.getKey(this.currentType);
        if (this.currentType != null && itemsKey != null) {
            standTypeStr = itemsKey.toString();
        } else {
            standTypeStr = "";
        }
        compound.putString("StandType", standTypeStr);

        compound.putByte("Pose", (byte) this.getPose().ordinal());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
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
        if (tag != null) {
            this.readAdditionalSaveData(tag);
        }
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
    public ItemEntity spawnAtLocation(@NotNull ItemLike iip) {
        if (iip == Items.ITEM_FRAME) {
            if (this.currentType == null || this.currentType == Items.AIR) {
                return null;
            }

            iip = this.currentType;
        }
        return super.spawnAtLocation(iip);
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float cat) {
        ItemStack blade = this.getItem();

        if (blade.isEmpty()) {
            return super.hurt(damageSource, cat);
        }

        if (!blade.getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return super.hurt(damageSource, cat);
        }

        ISlashBladeState state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        if (MinecraftForge.EVENT_BUS.post(new SlashBladeEvent.BladeStandAttackEvent(blade, state, this, damageSource))) {
            return true;
        }

        return super.hurt(damageSource, cat);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = InteractionResult.PASS;
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && !this.getItem().isEmpty()) {
                Pose current = this.getPose();
                int newIndex = (current.ordinal() + 1) % Pose.values().length;
                this.setPose(Pose.values()[newIndex]);
                result = InteractionResult.SUCCESS;
            } else if ((!itemstack.isEmpty() && itemstack.getCapability(ItemSlashBlade.BLADESTATE).isPresent())
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

    @Override
    protected @NotNull ItemStack getFrameItemStack() {
        return new ItemStack(currentType);
    }

    @Override
    public boolean survives() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        ItemStack blade = this.getItem();
        if (blade.isEmpty()) return;
        ISlashBladeState state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        MinecraftForge.EVENT_BUS.post(new SlashBladeEvent.BladeStandTickEvent(blade, state, this));
    }

    @Override
    protected void recalculateBoundingBox() {
        if (this.direction != null) {
            double d0 = 2D / 16D;
            double d1 = (double)this.pos.getX() + 0.5D - (double)this.direction.getStepX() * d0;
            double d2 = (double)this.pos.getY() + 0.5D - (double)this.direction.getStepY() * d0;
            double d3 = (double)this.pos.getZ() + 0.5D - (double)this.direction.getStepZ() * d0;
            this.setPosRaw(d1, d2, d3);
            double d4 = this.getWidth();
            double d5 = this.getHeight();
            double d6 = this.getWidth();

            d4 /= 32.0D;
            d5 /= 32.0D;
            d6 /= 32.0D;
            this.setBoundingBox(new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
        }
    }
}
