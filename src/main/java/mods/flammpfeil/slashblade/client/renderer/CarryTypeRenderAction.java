package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerSlashBlade;

import java.util.function.Consumer;

public record CarryTypeRenderAction(Consumer<PoseStack> standbyRenderAction, boolean cancelFirstPersonRender) {
    public CarryTypeRenderAction(Consumer<PoseStack> standbyRenderAction) {
        this(standbyRenderAction, false);
    }
    
    public CarryTypeRenderAction() {
        this(__ -> {
        }, false);
    }
    
    public static final CarryTypeRenderAction NONE_CARRY_TYPE_RENDER_ACTION = new CarryTypeRenderAction();
    public static final CarryTypeRenderAction DEFAULT_CARRY_TYPE_RENDER_ACTION = new CarryTypeRenderAction(poseStack -> {
        poseStack.translate(0.25F, -0.875f, -0.55f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_DEFAULT);
    });
    public static final CarryTypeRenderAction PSO2_CARRY_TYPE_RENDER_ACTION = new CarryTypeRenderAction(poseStack -> {
        poseStack.translate(1F, -1.125f, 0.20f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_PSO2);
    }, true);
    public static final CarryTypeRenderAction NINJA_CARRY_TYPE_RENDER_ACTION = new CarryTypeRenderAction(poseStack -> {
        poseStack.translate(-0.5F, -2f, 0.20f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_NINJA);
    }, true);
    public static final CarryTypeRenderAction KATANA_CARRY_TYPE_RENDER_ACTION = new CarryTypeRenderAction(poseStack -> {
        poseStack.translate(0.25F, -0.875f, -0.55f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_KATANA);
    });
    public static final CarryTypeRenderAction RNINJA_CARRY_TYPE_RENDER_ACTION = new CarryTypeRenderAction(poseStack -> {
        poseStack.translate(0.5F, -2f, 0.20f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_RNINJA);
    }, true);
}
