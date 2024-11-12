package mods.flammpfeil.slashblade.client.renderer.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.function.Function;

import net.minecraft.client.renderer.entity.ItemRenderer;

public class BladeRenderState extends RenderStateShard {

    private static final Color defaultColor = Color.white;
    private static Color col = defaultColor;

    public static void setCol(int rgba) {
        setCol(rgba, true);
    }

    public static void setCol(int rgb, boolean hasAlpha) {
        setCol(new Color(rgb, hasAlpha));
    }

    public static void setCol(Color value) {
        col = value;
    }

    public static final int MAX_LIGHT = 15728864;

    public static void resetCol() {
        col = defaultColor;
    }

    public BladeRenderState(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture,
            PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

//        Face.forceQuad = true;
//        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
//                Util.memoize(RenderType::entitySmoothCutout), true);
//        Face.forceQuad = false;

        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn,
                packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlend), true);
    }

    static public void renderOverridedColorWrite(ItemStack stack, WavefrontObject model, String target,
            ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendColorWrite), true);
    }

    static public void renderChargeEffect(ItemStack stack, float f, WavefrontObject model, String target,
            ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                (loc) -> BladeRenderState.getChargeEffect(loc, f * 0.1F % 1.0F, f * 0.01F % 1.0F), false);
    }

    static public void renderOverridedLuminous(ItemStack stack, WavefrontObject model, String target,
            ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendLuminous), false);
    }

    static public void renderOverridedLuminousDepthWrite(ItemStack stack, WavefrontObject model, String target,
            ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendLuminousDepthWrite), false);
    }

    static public void renderOverridedReverseLuminous(ItemStack stack, WavefrontObject model, String target,
            ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn,
                Util.memoize(BladeRenderState::getSlashBladeBlendReverseLuminous), false);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture,
            PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
            Function<ResourceLocation, RenderType> getRenderType, boolean enableEffect) {
        RenderOverrideEvent event = RenderOverrideEvent.onRenderOverride(stack, model, target, texture, matrixStackIn,
                bufferIn);

        if (event.isCanceled())
            return;

        ResourceLocation loc = event.getTexture();

        RenderType rt = getRenderType.apply(loc);// getSlashBladeBlendLuminous(event.getTexture());
        VertexConsumer vb = bufferIn.getBuffer(rt);

        Face.setCol(col);
        Face.setLightMap(packedLightIn);
        Face.setMatrix(matrixStackIn);
        event.getModel().tessellateOnly(vb, event.getTarget());

        if (stack.hasFoil() && enableEffect) {
            vb = bufferIn.getBuffer(BladeRenderState.getSlashBladeGlint());
            event.getModel().tessellateOnly(vb, event.getTarget());
        }

        Face.resetMatrix();
        Face.resetLightMap();
        Face.resetCol();

        Face.resetAlphaOverride();
        Face.resetUvOperator();

        resetCol();
    }

    public static VertexConsumer getBuffer(MultiBufferSource bufferIn, RenderType renderTypeIn, boolean glintIn) {
        return null;
    }

    public static final VertexFormat POSITION_TEX = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION).put("UV0", DefaultVertexFormat.ELEMENT_UV0).build());
    public static final RenderType BLADE_GLINT = RenderType.create("blade_glint", POSITION_TEX,
            VertexFormat.Mode.TRIANGLES, 256, false, false,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
                    .setTextureState(new TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
                    .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING)
                    .createCompositeState(false));

    public static RenderType getSlashBladeBlend(ResourceLocation p_228638_0_) {

        /*
         * RenderType.CompositeState rendertype$compositestate =
         * RenderType.CompositeState.builder()
         * .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER) .setTextureState(new
         * RenderStateShard.TextureStateShard(p_173200_, false, false))
         * .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         * .setOutputState(ITEM_ENTITY_TARGET) .setLightmapState(LIGHTMAP)
         * .setOverlayState(OVERLAY)
         * .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
         * .createCompositeState(true);
         */

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                //.setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                //该着色器无法正确处理lightmap
                //.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                //该渲染写入半透明渲染帧缓冲，鉴于帧缓冲主要用于后处理管线，渲染物品使用可能会使部分光影出现问题
                //.setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                //不透明渲染
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                //该着色器支持处理lightmap,overlaymap，详见https://zh.minecraft.wiki/w/着色器#entity_cutout
                //注：该页面中介绍的为渲染类型，但其信息基本与着色器一致，若真感兴趣，可查看开发环境依赖库中client-extra.jar中assets/shaders/core/目录中rendertype_entity_cutout前缀的相关文件。
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)//该渲染写入游戏物品渲染帧缓冲
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, true))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)//半透明渲染
                .setCullState(NO_CULL)//不剔除背面(刀鞘)
                .setLightmapState(LIGHTMAP)//使用光照图
                .setOverlayState(RenderStateShard.OVERLAY)//使用叠加层纹理，被攻击时变红
                // .overlay(OVERLAY_ENABLED)
                .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)//使用深度偏移叠加，避免Z-fighting
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).createCompositeState(true);

        return RenderType.create("slashblade_blend", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }
    
    public static RenderType getSlashBladeGlint() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
        	    .setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER)
        	    .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false))
        	    .setWriteMaskState(COLOR_WRITE)
        	    .setCullState(NO_CULL)
        	    .setDepthTestState(EQUAL_DEPTH_TEST)
        	    .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
        	    .setOutputState(ITEM_ENTITY_TARGET)
        	    .setTexturingState(ENTITY_GLINT_TEXTURING)
        	    .createCompositeState(false);
        return RenderType.create("slashblade_glint", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeBlendColorWrite(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER).setOutputState(TRANSLUCENT_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE).createCompositeState(true);
        return RenderType.create("slashblade_blend_write_color", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    public static RenderType getSlashBladeBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER).setOutputState(PARTICLES_TARGET)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE).createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getChargeEffect(ResourceLocation p_228638_0_, float x, float y) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER).setOutputState(PARTICLES_TARGET)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, false))
                .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(x, y))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .setOverlayState(OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(false);
        return RenderType.create("slashblade_charge_effect", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeBlendLuminousDepthWrite(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER).setOutputState(RenderStateShard.PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous_depth_write", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_REVERSE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                RenderSystem.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
            }, () -> {
                RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    public static RenderType getSlashBladeBlendReverseLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER).setOutputState(PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_REVERSE_TRANSPARENCY)
                // .setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                // .overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE).createCompositeState(false);
        return RenderType.create("slashblade_blend_reverse_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

}
