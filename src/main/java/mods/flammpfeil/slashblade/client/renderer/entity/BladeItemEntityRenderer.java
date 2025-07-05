package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class BladeItemEntityRenderer extends ItemEntityRenderer {
    public BladeItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldSpreadItems() {
        return false;
    }

    @Override
    public boolean shouldBob() {
        return false;
    }

    @Override
    public void render(ItemEntity itemIn, float entityYaw, float partialTicks, @NotNull PoseStack matrixStackIn,
                       @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        this.shadowRadius = 0;

        if (!itemIn.getItem().isEmpty()) {
            renderBlade(itemIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        } else {
            partialTicks = (float) (itemIn.bobOffs * 20.0 - (double) itemIn.getAge());
            super.render(itemIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
    }

    private void renderBlade(ItemEntity itemIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
                             MultiBufferSource bufferIn, int packedLightIn) {
        if (itemIn instanceof BladeItemEntity bladeItem) {
            try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStackIn)) {
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityYaw));

                ItemStack current = itemIn.getItem();

                EnumSet<SwordType> types = SwordType.from(current);
                itemIn.getPersistentData();
                ResourceLocation modelLocation = current.getCapability(ItemSlashBlade.BLADESTATE)
                        .map((state) -> state.getModel().orElseGet(bladeItem::getModel)).orElseGet(bladeItem::getModel);
                ResourceLocation textureLocation = current.getCapability(ItemSlashBlade.BLADESTATE)
                        .map((state) -> state.getTexture().orElseGet(bladeItem::getTexture))
                        .orElseGet(bladeItem::getTexture);

                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

                float scale = 0.00625f;

                try (MSAutoCloser msac2 = MSAutoCloser.pushMatrix(matrixStackIn)) {

                    float heightOffset;
                    float xOffset;
                    String renderTarget;
                    if (types.contains(SwordType.EDGEFRAGMENT)) {
                        heightOffset = 225;
                        xOffset = 200;
                        renderTarget = "blade_fragment";
                    } else if (types.contains(SwordType.BROKEN)) {
                        heightOffset = 100;
                        xOffset = 30;
                        renderTarget = "blade_damaged";
                    } else {
                        heightOffset = 225;
                        xOffset = 120;
                        renderTarget = "blade";
                    }

                    if (itemIn.isInWater()) {

                        matrixStackIn.translate(0, 0.025f, 0);
                        matrixStackIn.mulPose(Axis.YP.rotationDegrees(itemIn.bobOffs));

                        matrixStackIn.scale(scale, scale, scale);

                        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));

                    } else if (!itemIn.onGround()) {
                        matrixStackIn.scale(scale, scale, scale);

                        float speed = -81f;
                        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(speed * (itemIn.tickCount + partialTicks)));
                        matrixStackIn.translate(xOffset, 0, 0);
                    } else {
                        matrixStackIn.scale(scale, scale, scale);

                        matrixStackIn
                                .mulPose(Axis.ZP.rotationDegrees(60 + (float) Math.toDegrees(itemIn.bobOffs / 6.0)));
                        matrixStackIn.translate(heightOffset, 0, 0);
                    }

                    BladeRenderState.renderOverrided(current, model, renderTarget, textureLocation, matrixStackIn,
                            bufferIn, packedLightIn);
                    BladeRenderState.renderOverridedLuminous(current, model, renderTarget + "_luminous",
                            textureLocation, matrixStackIn, bufferIn, packedLightIn);
                }

                if (itemIn.isInWater() || itemIn.onGround() && !types.contains(SwordType.NOSCABBARD)) {

                    try (MSAutoCloser msac2 = MSAutoCloser.pushMatrix(matrixStackIn)) {

                        matrixStackIn.translate(0, 0.025f, 0);

                        matrixStackIn.mulPose(Axis.YP.rotationDegrees(itemIn.bobOffs));

                        if (!itemIn.isInWater()) {
                            matrixStackIn.translate(0.75, 0, -0.4);
                        }

                        matrixStackIn.scale(scale, scale, scale);

                        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));

                        String renderTarget = "sheath";

                        BladeRenderState.renderOverrided(current, model, renderTarget, textureLocation, matrixStackIn,
                                bufferIn, packedLightIn);
                        BladeRenderState.renderOverridedLuminous(current, model, renderTarget + "_luminous",
                                textureLocation, matrixStackIn, bufferIn, packedLightIn);
                    }
                }

            }
        }
        // todo: fire render override?
    }

    /*
     * @Override public void doRenderShadowAndFire(Entity entityIn, double x, double
     * y, double z, float yaw, float partialTicks) {
     *
     * matrixStackIn.enableBlend(); matrixStackIn.blendFuncSeparate(
     * matrixStackIn.SourceFactor.SRC_COLOR, matrixStackIn.DestFactor.ONE ,
     * matrixStackIn.SourceFactor.ONE, matrixStackIn.DestFactor.ZERO);
     *
     * matrixStackIn.pushMatrix(); matrixStackIn.translatef((float)x, (float)y,
     * (float)z); matrixStackIn.scaled(1.4,1.8, 1.4);
     * matrixStackIn.translatef((float)-x, (float)-y, (float)-z);
     *
     * //core super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
     *
     *
     * //dark fire matrixStackIn.blendFuncSeparate(
     * matrixStackIn.SourceFactor.SRC_ALPHA, matrixStackIn.DestFactor.ONE ,
     * matrixStackIn.SourceFactor.ONE, matrixStackIn.DestFactor.ZERO);
     * matrixStackIn.translatef((float)x, (float)y, (float)z);
     * matrixStackIn.scaled(1.5,1.6,1.5); matrixStackIn.translatef((float)-x,
     * (float)-y, (float)-z); super.doRenderShadowAndFire(entityIn, x, y, z, yaw,
     * partialTicks); matrixStackIn.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
     * super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
     *
     *
     * matrixStackIn.popMatrix(); matrixStackIn.blendEquation(GL14.GL_FUNC_ADD);
     * matrixStackIn.blendFuncSeparate(GL11.GL_SRC_ALPHA,
     * GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
     * matrixStackIn.disableBlend(); }
     */
}
