package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerSlashBlade;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.function.Consumer;

public enum CarryType implements IExtensibleEnum {
    NONE,
    DEFAULT(poseStack -> {
        poseStack.translate(0.25F, -0.875f, -0.55f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_DEFAULT);
    }),
    PSO2(poseStack -> {
        poseStack.translate(1F, -1.125f, 0.20f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_PSO2);
    }, true),
    NINJA(poseStack -> {
        poseStack.translate(-0.5F, -2f, 0.20f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_NINJA);
    }, true),
    KATANA(poseStack -> {
        poseStack.translate(0.25F, -0.875f, -0.55f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_KATANA);
    }),
    RNINJA(poseStack -> {
        poseStack.translate(0.5F, -2f, 0.20f);
        poseStack.mulPose(LayerSlashBlade.CARRY_ROTATION_RNINJA);
    }, true),
    ;
    
    public final Consumer<PoseStack> standbyRenderAction;
    public final boolean cancelFirstPersonRender;
    
    CarryType() {
        this(poseStack -> {
        }, false);
    }
    
    CarryType(Consumer<PoseStack> standbyRenderAction) {
        this(standbyRenderAction, false);
    }
    
    CarryType(Consumer<PoseStack> standbyRenderAction, boolean cancelFirstPersonRender) {
        this.standbyRenderAction = standbyRenderAction;
        this.cancelFirstPersonRender = cancelFirstPersonRender;
    }
    
    public static final Codec<CarryType> CODEC = Codec.STRING.xmap(string -> CarryType.valueOf(string.toUpperCase()),
        instance -> instance.name().toLowerCase());
    
    public static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(CarryType.class);
    }
}
