package mods.flammpfeil.slashblade.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdMotionPlayerGL2;
import jp.nyatla.nymmd.MmdPmdModelMc;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.io.IOException;

public class LayerMainBlade<T extends LivingEntity, M extends EntityModel<T>> extends LayerSlashBlade<T, M> {
    @Nullable
    private MmdPmdModelMc cachedBladeholder;
    @Nullable
    private MmdMotionPlayerGL2 cachedMotionPlayer;
    
    private final float[] boneMatrixBuf = new float[16];
    private final Matrix3f normalMatrixTmp = new Matrix3f();
    
    public LayerMainBlade(RenderLayerParent<T, M> entityRendererIn) {
        super(entityRendererIn);
    }
    
    @Nullable
    private MmdPmdModelMc getBladeholder() {
        if (this.cachedBladeholder == null) {
            try {
                this.cachedBladeholder = new MmdPmdModelMc(
                    ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "model/bladeholder.pmd"));
            } catch (IOException | MmdException e) {
                SlashBlade.LOGGER.warn(e);
            }
        }
        return this.cachedBladeholder;
    }
    
    private MmdMotionPlayerGL2 getMotionPlayer() {
        if (this.cachedMotionPlayer == null) {
            this.cachedMotionPlayer = new MmdMotionPlayerGL2();
            MmdPmdModelMc pmd = this.getBladeholder();
            if (pmd != null) {
                try {
                    this.cachedMotionPlayer.setPmd(pmd);
                } catch (MmdException e) {
                    SlashBlade.LOGGER.warn(e);
                }
            }
        }
        return this.cachedMotionPlayer;
    }
    
    @Override
    protected double getMaxSeconds(ComboState combo) {
        MmdVmdMotionMc motion = BladeMotionManager.getInstance().getMotion(combo.getMotionLoc());
        if (motion == null) {
            return 0;
        }
        return TimeValueHelper.getMSecFromFrames(motion.getMaxFrame());
    }
    
    @Override
    protected void renderMainBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn,
                                   T entity, float partialTicks, ItemStack stack,
                                   ISlashBladeState s, ComboState combo, double timeMSec) {
        MmdMotionPlayerGL2 mmp = this.getMotionPlayer();
        MmdVmdMotionMc motion = BladeMotionManager.getInstance().getMotion(combo.getMotionLoc());
        
        // 无可用动画且播放器尚未持有 VMD 时，保持现状的提前返回语义。
        if (motion == null && !mmp.hasVmdMotion()) {
            return;
        }
        
        if (motion != null) {
            try {
                mmp.setVmd(motion);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn(e);
            }
        }
        
        try {
            mmp.updateMotionBonesAndSkinning((float) timeMSec);
        } catch (MmdException e) {
            SlashBlade.LOGGER.warn(e);
        }
        
        double motionYOffset = 1.5f;
        double motionScale = 1.5 / 12.0;
        double modelScaleBase = 0.0078125F; // 0.5^7
        
        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(matrixStack)) {
            
            this.setUserPose(matrixStack, entity, partialTicks * combo.getSpeed(), s);
            
            // minecraft model neckPoint height = 1.5f
            // mmd model neckPoint height = 12.0f
            matrixStack.translate(0, motionYOffset, 0);
            
            matrixStack.scale((float) motionScale, (float) motionScale, (float) motionScale);
            
            // transpoze mmd to mc
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
            
            ResourceLocation textureLocation = s.getTexture().orElse(DefaultResources.resourceDefaultTexture);
            
            WavefrontObject obj = BladeModelManager.getInstance()
                .getModel(s.getModel().orElse(DefaultResources.resourceDefaultModel));
            
            try (MSAutoCloser ignored1 = MSAutoCloser.pushMatrix(matrixStack)) {
                int idx = mmp.getBoneIndexByName("hardpointA");
                
                if (0 <= idx) {
                    mmp._skinning_mat[idx].getValue(this.boneMatrixBuf);
                    
                    Matrix4f mat = VectorHelper.matrix4fFromArray(this.boneMatrixBuf);
                    
                    matrixStack.scale(-1, 1, 1);
                    PoseStack.Pose entry = matrixStack.last();
                    entry.pose().mul(mat);
                    entry.normal().mul(this.normalMatrixTmp.set(mat).invert().transpose());
                    matrixStack.scale(-1, 1, 1);
                }
                
                float modelScale = (float) (modelScaleBase * (1.0f / motionScale));
                matrixStack.scale(modelScale, modelScale, modelScale);
                
                String part;
                if (s.isBroken()) {
                    part = "blade_damaged";
                } else {
                    part = "blade";
                }
                
                BladeRenderState.renderOverrided(stack, obj, part, textureLocation, matrixStack, bufferIn,
                    lightIn);
                BladeRenderState.renderOverridedLuminous(stack, obj,
                    s.isBroken() ? BLADE_DAMAGED_LUMINOUS : BLADE_LUMINOUS, textureLocation,
                    matrixStack, bufferIn, lightIn);
            }
            
            try (MSAutoCloser ignored1 = MSAutoCloser.pushMatrix(matrixStack)) {
                int idx = mmp.getBoneIndexByName("hardpointB");
                
                if (0 <= idx) {
                    mmp._skinning_mat[idx].getValue(this.boneMatrixBuf);
                    
                    Matrix4f mat = VectorHelper.matrix4fFromArray(this.boneMatrixBuf);
                    
                    matrixStack.scale(-1, 1, 1);
                    PoseStack.Pose entry = matrixStack.last();
                    entry.pose().mul(mat);
                    entry.normal().mul(this.normalMatrixTmp.set(mat).invert().transpose());
                    matrixStack.scale(-1, 1, 1);
                }
                
                float modelScale = (float) (modelScaleBase * (1.0f / motionScale));
                matrixStack.scale(modelScale, modelScale, modelScale);
                BladeRenderState.renderOverrided(stack, obj, "sheath", textureLocation, matrixStack, bufferIn,
                    lightIn);
                BladeRenderState.renderOverridedLuminous(stack, obj, SHEATH_LUMINOUS, textureLocation,
                    matrixStack, bufferIn, lightIn);
                
                if (s.isCharged(entity)) {
                    float f = (float) entity.tickCount + partialTicks;
                    BladeRenderState.renderChargeEffect(stack, f, obj, "effect",
                        ResourceLocation.parse("textures/entity/creeper/creeper_armor.png"), matrixStack,
                        bufferIn, lightIn);
                }
            }
        }
    }
}
