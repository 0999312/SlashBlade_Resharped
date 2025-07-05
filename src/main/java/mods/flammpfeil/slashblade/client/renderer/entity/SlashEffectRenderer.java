package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank.ConcentrationRanks;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class SlashEffectRenderer<T extends EntitySlashEffect> extends EntityRenderer<T> {

    static private final ResourceLocation modelLocation = new ResourceLocation(SlashBlade.MODID,
            "model/util/slash.obj");
    static private final ResourceLocation textureLocation = new ResourceLocation(SlashBlade.MODID,
            "model/util/slash.png");

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return textureLocation;
    }

    public SlashEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStackIn,
                       @NotNull MultiBufferSource bufferIn, int packedLightIn) {

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStackIn)) {

            matrixStackIn
                    .mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(entity.getRotationRoll()));

            WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

            int lifetime = entity.getLifetime();

            float progress = Math.min(lifetime, (entity.tickCount + partialTicks)) / lifetime;

            double deathTime = lifetime;
            double baseAlpha = (Math.min(deathTime, Math.max(0, (lifetime - (entity.tickCount) - partialTicks)))
                    / deathTime);
            baseAlpha = -Math.pow(baseAlpha - 1, 4.0) + 1.0;

            // baseAlpha = Math.sin(-Math.PI + Math.PI * 2 * progress) * 0.5f + 0.5f;
            // baseAlpha = Math.sin(Math.PI * progress);

            // time
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(entity.getRotationOffset() - 135.0F * progress));

            matrixStackIn.scale(1, 0.25f, 1);

            float baseScale = 1.2f;
            matrixStackIn.scale(baseScale, baseScale, baseScale);

            float yscale = 0.03f;
            float scale = entity.getBaseSize() * Mth.lerp(progress, 0.03f, 0.035f);

            int color = entity.getColor() & 0xFFFFFF;

            ConcentrationRanks rank = entity.getRankCode();

            // rank color overwrite
            if (rank.level < ConcentrationRanks.C.level) {
                color = 0x555555;
            }

            ResourceLocation rl = getTextureLocation(entity);

            // baseAlpha = 1.0f;
            int alpha = ((0xFF & (int) (0xFF * baseAlpha)) << 24);

            // black alpha insidee
            if (ConcentrationRanks.S.level <= rank.level) {
                try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    float windscale = entity.getBaseSize() * Mth.lerp(progress, 0.035f, 0.03f);
                    matrixStackIn.scale(windscale, yscale, windscale);
                    Face.setAlphaOverride(Face.alphaOverrideYZZ);
                    Face.setUvOperator(1, 1, 0, -0.8f + progress * 0.3f);
                    BladeRenderState.setCol(0x222222 | alpha);
                    BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "base", rl, matrixStackIn,
                            bufferIn, packedLightIn);
                }
            }

            // color alpha base
            if (ConcentrationRanks.D.level <= rank.level) {
                try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    matrixStackIn.scale(scale, yscale, scale);
                    Face.setAlphaOverride(Face.alphaOverrideYZZ);
                    Face.setUvOperator(1, 1, 0, -0.35f + progress * -0.15f);
                    BladeRenderState.setCol(color | alpha);
                    BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "base", rl, matrixStackIn,
                            bufferIn, packedLightIn);
                }
            }

            // white add outside
            if (ConcentrationRanks.B.level <= rank.level) {
                try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    float windscale = entity.getBaseSize() * Mth.lerp(progress, 0.03f, 0.0375f);
                    matrixStackIn.scale(windscale, yscale, windscale);
                    Face.setAlphaOverride(Face.alphaOverrideYZZ);
                    Face.setUvOperator(1, 1, 0, -0.5f + progress * -0.2f);
                    BladeRenderState.setCol(0x404040 | alpha);
                    BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "base", rl, matrixStackIn,
                            bufferIn, packedLightIn);
                }
            }

            // color add base
            try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                matrixStackIn.scale(scale, yscale, scale);
                Face.setAlphaOverride(Face.alphaOverrideYZZ);
                Face.setUvOperator(1, 1, 0, -0.35f + progress * -0.15f);
                BladeRenderState.setCol(color | alpha);
                BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn,
                        packedLightIn);
            }
        }
    }
}
