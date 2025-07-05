package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class JudgementCutRenderer<T extends EntityJudgementCut> extends EntityRenderer<T> {

    static private final ResourceLocation modelLocation = new ResourceLocation(SlashBlade.MODID,
            "model/util/slashdim.obj");
    static private final ResourceLocation textureLocation = new ResourceLocation(SlashBlade.MODID,
            "model/util/slashdim.png");

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return textureLocation;
    }

    public JudgementCutRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStackIn,
                       @NotNull MultiBufferSource bufferIn, int packedLightIn) {

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStackIn)) {

            matrixStackIn
                    .mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

            WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

            int lifetime = entity.getLifetime();

            double deathTime = lifetime;
            double baseAlpha = (Math.min(deathTime, Math.max(0, (lifetime - (entity.tickCount) - partialTicks)))
                    / deathTime);
            baseAlpha = -Math.pow(baseAlpha - 1, 4.0) + 1.0;

            int seed = entity.getSeed();

            matrixStackIn.mulPose(Axis.YP.rotationDegrees(seed));

            float scale = 0.01f;
            matrixStackIn.scale(scale, scale, scale);

            int color = entity.getColor() & 0xFFFFFF;
            Color col = new Color(color);
            float[] hsb = Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
            int baseColor = Color.HSBtoRGB(0.5f + hsb[0], hsb[1], 0.2f/* hsb[2] */) & 0xFFFFFF;

            try (MSAutoCloser msacB = MSAutoCloser.pushMatrix(matrixStackIn)) {
                for (int l = 0; l < 5; l++) {
                    matrixStackIn.scale(0.95f, 0.95f, 0.95f);

                    BladeRenderState.setCol(baseColor | ((0xFF & (int) (0x66 * baseAlpha)) << 24));
                    BladeRenderState.renderOverridedReverseLuminous(ItemStack.EMPTY, model, "base",
                            this.getTextureLocation(entity), matrixStackIn, bufferIn, packedLightIn);
                }
            }

            int loop = 3;
            for (int l = 0; l < loop; l++) {
                try (MSAutoCloser msacB = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    float cycleTicks = 15;
                    float wave = (entity.tickCount + (cycleTicks / (float) loop * l) + partialTicks) % cycleTicks;
                    float waveScale = 1.0f + 0.03f * wave;
                    matrixStackIn.scale(waveScale, waveScale, waveScale);

                    BladeRenderState
                            .setCol(baseColor | ((int) (0x88 * ((cycleTicks - wave) / cycleTicks) * baseAlpha) << 24));
                    BladeRenderState.renderOverridedReverseLuminous(ItemStack.EMPTY, model, "base",
                            this.getTextureLocation(entity), matrixStackIn, bufferIn, packedLightIn);
                }
            }

            int windCount = 5;
            for (int l = 0; l < windCount; l++) {
                try (MSAutoCloser msacB = MSAutoCloser.pushMatrix(matrixStackIn)) {

                    matrixStackIn.mulPose(Axis.XP.rotationDegrees((360.0f / windCount) * l));
                    matrixStackIn.mulPose(Axis.YP.rotationDegrees(30.0f));

                    double rotWind = 360.0 / 20.0;

                    double offsetBase = 7;

                    double offset = l * offsetBase;

                    double motionLen = offsetBase * (windCount - 1);

                    double ticks = entity.tickCount + partialTicks + seed;
                    double offsetTicks = ticks + offset;
                    double progress = (offsetTicks % motionLen) / motionLen;

                    double rad = (Math.PI) * 2.0;
                    rad *= progress;

                    float windScale = (float) (0.4 + progress);
                    matrixStackIn.scale(windScale, windScale, windScale);

                    matrixStackIn.mulPose(Axis.ZP.rotationDegrees((float) (rotWind * offsetTicks)));

                    Color cc = new Color(col.getRed(), col.getGreen(), col.getBlue(),
                            0xff & (int) (Math.min(0, 0xFF * Math.sin(rad) * baseAlpha)));
                    BladeRenderState.setCol(cc);
                    BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "wind",
                            this.getTextureLocation(entity), matrixStackIn, bufferIn, BladeRenderState.MAX_LIGHT);
                }
            }
        }
    }
}
