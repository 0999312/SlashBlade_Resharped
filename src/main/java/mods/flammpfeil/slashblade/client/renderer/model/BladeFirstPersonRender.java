package mods.flammpfeil.slashblade.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerSlashBlade;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import net.irisshaders.iris.Iris;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

public class BladeFirstPersonRender {
    @Nullable
    private LayerSlashBlade<LocalPlayer, ?> layer = null;
    
    private final boolean isIrisLoaded = ModList.get().isLoaded("iris");
    
    private BladeFirstPersonRender() {
        initLayer();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean initLayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        
        EntityRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(mc.player);
        if (renderer instanceof RenderLayerParent) {
            layer = new LayerMainBlade((RenderLayerParent) renderer);
        }
        
        return layer != null;
    }
    
    private static final class SingletonHolder {
        private static final BladeFirstPersonRender instance = new BladeFirstPersonRender();
    }
    
    public static BladeFirstPersonRender getInstance() {
        return SingletonHolder.instance;
    }
    
    public void render(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (layer == null && !initLayer()) {
            return;
        }
        
        boolean flag = mc.getCameraEntity() instanceof LivingEntity
            && ((LivingEntity) mc.getCameraEntity()).isSleeping();
        if (mc.gameMode == null || !(mc.options.getCameraType() == CameraType.FIRST_PERSON && !flag && !mc.options.hideGui
            && !mc.gameMode.isAlwaysFlying())) {
            return;
        }
        
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.isEmpty()) {
            return;
        }
        if (BladeStateAccess.of(stack).isEmpty()) {
            return;
        }
        
        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(matrixStack)) {
            PoseStack.Pose me = matrixStack.last();
            me.pose().identity();
            me.normal().identity();
            
            float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(false);
            float yaw = Mth.lerp(partialTicks, player.yRotO, player.getYRot());
            
            // 这里顺序不要乱动，Java的“短路与”检测机制在不加载iris时isIrisLoaded为false，后续的Iris.isPackInUseQuick()直接不会被调用，从而避免了NoClassDefFoundError
            if (isIrisLoaded && Iris.isPackInUseQuick()) {
                matrixStack.mulPose(Axis.XP.rotationDegrees(player.getXRot()));
                matrixStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
            }
            
            matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            
            matrixStack.translate(0.0f, 0.0f, -0.5f);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            matrixStack.scale(1.2F, 1.0F, 1.0F);
            
            // Keep the blade aligned with the camera without swinging through extreme pitch angles.
            matrixStack.mulPose(Axis.XP.rotationDegrees(-Mth.clamp(player.getXRot(), -60.0F, 10.0F)));
            
            // layer.disableOffhandRendering();
            layer.render(matrixStack, bufferIn, combinedLightIn, player, 0, 0, partialTicks, 0, 0, 0);
        }
    }
}
