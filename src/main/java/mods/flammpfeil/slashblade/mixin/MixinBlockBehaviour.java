package mods.flammpfeil.slashblade.mixin;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class MixinBlockBehaviour {

    @SuppressWarnings("deprecation")
    @Inject(at = @At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", cancellable = true)
    public void getCollisionShape(BlockGetter p_60743_, BlockPos p_60744_, CollisionContext p_60745_,
                                  CallbackInfoReturnable<VoxelShape> callback) {
        if (!(asState().is(BlockTags.LEAVES))) {
            return;
        }
        if (p_60745_.isDescending()) {
            return;
        }

        if (!(p_60745_ instanceof EntityCollisionContext)) {
            return;
        }
        if (!(((EntityCollisionContext) p_60745_).getEntity() instanceof Player)) {
            return;
        }

        ItemStack itemStack = ((Player) ((EntityCollisionContext) p_60745_).getEntity()).getMainHandItem();
        if (!(itemStack.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        callback.setReturnValue(Blocks.SCAFFOLDING.getCollisionShape(Blocks.SCAFFOLDING.defaultBlockState(), p_60743_,
                p_60744_, p_60745_));
        callback.cancel();
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At("HEAD"), method = "getVisualShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", cancellable = true)
    public void getVisualShape(BlockGetter p_60743_, BlockPos p_60744_, CollisionContext p_60745_,
                               CallbackInfoReturnable<VoxelShape> callback) {
        if (!(asState().is(BlockTags.LEAVES))) {
            return;
        }

        callback.setReturnValue(Blocks.SCAFFOLDING.getVisualShape(Blocks.SCAFFOLDING.defaultBlockState(), p_60743_,
                p_60744_, p_60745_));
        callback.cancel();
    }

    @Shadow
    protected BlockState asState() {
        throw new IllegalStateException("Mixin failed to shadow asState()");
    }

}
