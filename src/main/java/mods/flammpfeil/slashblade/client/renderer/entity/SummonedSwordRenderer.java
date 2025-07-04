package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class SummonedSwordRenderer<T extends EntityAbstractSummonedSword> extends EntityRenderer<T> {

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getTextureLoc();
    }

    public SummonedSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn,
                       int packedLightIn) {

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
            Entity hits = entity.getHitEntity();
            boolean hasHitEntity = hits != null;

            if (hasHitEntity) {
                matrixStack
                        .mulPose(Axis.YN.rotationDegrees(Mth.rotLerp(partialTicks, hits.yRotO, hits.getYRot()) - 90));
                matrixStack.mulPose(Axis.YN.rotationDegrees(entity.getOffsetYaw()));
            } else {
                matrixStack.mulPose(
                        Axis.YP.rotationDegrees(Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            }

            matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));

            matrixStack.mulPose(Axis.XP.rotationDegrees(entity.getRoll()));

            float scale = 0.0075f;
            matrixStack.scale(scale, scale, scale);
            matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));

            if (hasHitEntity) {
                matrixStack.translate(0, 0, -100);
            }

            // matrixStack.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
            WavefrontObject model = BladeModelManager.getInstance().getModel(entity.getModelLoc());
            BladeRenderState.setCol(entity.getColor(), false);
            BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "ss", getTextureLocation(entity),
                    matrixStack, bufferIn, packedLightIn);
        }
    }
}
