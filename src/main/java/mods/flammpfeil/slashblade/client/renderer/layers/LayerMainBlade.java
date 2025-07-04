package mods.flammpfeil.slashblade.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdMotionPlayerGL2;
import jp.nyatla.nymmd.MmdPmdModelMc;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.io.IOException;
import java.util.Objects;

public class LayerMainBlade<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public LayerMainBlade(RenderLayerParent<T, M> entityRendererIn) {
        super(entityRendererIn);
    }

    final LazyOptional<MmdPmdModelMc> bladeholder = LazyOptional.of(() -> {
        try {
            return new MmdPmdModelMc(new ResourceLocation(SlashBlade.MODID, "model/bladeholder.pmd"));
        } catch (IOException | MmdException e) {
            e.printStackTrace();
        }
        return null;
    });

    final LazyOptional<MmdMotionPlayerGL2> motionPlayer = LazyOptional.of(() -> {
        MmdMotionPlayerGL2 mmp = new MmdMotionPlayerGL2();

        bladeholder.ifPresent(pmd -> {
            try {
                mmp.setPmd(pmd);
            } catch (MmdException e) {
                e.printStackTrace();
            }
        });

        return mmp;
    });

    public float modifiedSpeed(float baseSpeed, LivingEntity entity) {
        float modif = 6.0f;
        if (MobEffectUtil.hasDigSpeed(entity)) {
            modif = 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            modif = 6 + (1 + Objects.requireNonNull(entity.getEffect(MobEffects.DIG_SLOWDOWN)).getAmplifier()) * 2;
        }

        modif /= 6.0f;

        return baseSpeed / modif;
    }

    public void renderOffhandItem(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity) {
        ItemStack offhandStack = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (offhandStack.isEmpty() || !offhandStack.getCapability(CapabilitySlashBlade.BLADESTATE).isPresent()) {
            renderHotbarItem(matrixStack, bufferIn, lightIn, entity);
            return;
        }

        renderStandbyBlade(matrixStack, bufferIn, lightIn, offhandStack, entity);
    }

    public void renderHotbarItem(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity) {
        if (entity instanceof Player player) {
            if (player.getInventory().selected == 0) {
                return;
            }

            ItemStack blade = player.getInventory().getItem(0);
            if (blade.isEmpty()) {
                return;
            }

            renderStandbyBlade(matrixStack, bufferIn, lightIn, blade, entity);
        }
    }

    public void renderStandbyBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack blade, T entity) {
        LazyOptional<ISlashBladeState> state = blade.getCapability(CapabilitySlashBlade.BLADESTATE);
        state.ifPresent(s -> {
            double modelScaleBase = 0.0078125F; // 0.5^7
            double motionScale = 1.5 / 12.0;
            ResourceLocation textureLocation = s.getTexture().orElse(DefaultResources.resourceDefaultTexture);

            WavefrontObject obj = BladeModelManager.getInstance()
                    .getModel(s.getModel().orElse(DefaultResources.resourceDefaultModel));
            String part;
            try (MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)) {
                // minecraft model neckPoint height = 1.5f
                // mmd model neckPoint height = 12.0f
                matrixStack.translate(0, 1.5f, 0);
                var carrytype = s.getCarryType();
                final Minecraft mcinstance = Minecraft.getInstance();
                switch (carrytype) {
                    case PSO2:
                        matrixStack.translate(1F, -1.125f, 0.20f);
                        matrixStack.mulPose(new Quaternionf().rotateZYX(-0.122173F, 0, 0));
                        if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                                && entity == mcinstance.player) {
                            return;
                        }
                        break;

                    case KATANA:
                        matrixStack.translate(0.25F, -0.875f, -0.55f);
                        matrixStack.mulPose(new Quaternionf().rotateZYX(3.1415927F, 1.570796f, 0.261799F));
                        break;

                    case DEFAULT:
                        matrixStack.translate(0.25F, -0.875f, -0.55f);
                        matrixStack.mulPose(new Quaternionf().rotateZYX(0F, 1.570796f, 0.261799F));
                        break;

                    case NINJA:
                        matrixStack.translate(-0.5F, -2f, 0.20f);
                        matrixStack.mulPose(new Quaternionf().rotateZYX(-2.094395F, 0f, 3.1415927F));
                        if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                                && entity == mcinstance.player) {
                            return;
                        }
                        break;

                    case RNINJA:
                        matrixStack.translate(0.5F, -2f, 0.20f);
                        matrixStack.mulPose(new Quaternionf().rotateZYX(-1.047198F, 0, 0));
                        if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                                && entity == mcinstance.player) {
                            return;
                        }
                        break;

                    default:
                        return;
                }

                float modelScale = (float) (modelScaleBase * (1.0f / motionScale));
                matrixStack.scale((float) motionScale, (float) motionScale, (float) motionScale);
                matrixStack.scale(modelScale, modelScale, modelScale);

                try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
                    if (s.isBroken()) {
                        part = "blade_damaged";
                    } else {
                        part = "blade";
                    }

                    BladeRenderState.renderOverrided(blade, obj, part, textureLocation, matrixStack, bufferIn,
                            lightIn);
                    BladeRenderState.renderOverridedLuminous(blade, obj, part + "_luminous", textureLocation,
                            matrixStack, bufferIn, lightIn);
                    BladeRenderState.renderOverrided(blade, obj, "sheath", textureLocation, matrixStack, bufferIn,
                            lightIn);
                    BladeRenderState.renderOverridedLuminous(blade, obj, "sheath_luminous", textureLocation,
                            matrixStack, bufferIn, lightIn);
                }
            }
        });
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int lightIn, @NotNull T entity, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        this.renderOffhandItem(matrixStack, bufferIn, lightIn, entity);

        float motionYOffset = 1.5f;
        double motionScale = 1.5 / 12.0;
        double modelScaleBase = 0.0078125F; // 0.5^7

        ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.isEmpty()) {
            return;
        }

        LazyOptional<ISlashBladeState> state = stack.getCapability(CapabilitySlashBlade.BLADESTATE);
        state.ifPresent(s -> motionPlayer.ifPresent(mmp -> {
            ComboState combo = ComboStateRegistry.REGISTRY.get().getValue(s.getComboSeq()) != null
                    ? ComboStateRegistry.REGISTRY.get().getValue(s.getComboSeq())
                    : ComboStateRegistry.NONE.get();
            // tick to msec
            double time = TimeValueHelper.getMSecFromTicks(
                    Math.max(0, entity.level().getGameTime() - s.getLastActionTime()) + partialTicks);

            while (combo != ComboStateRegistry.NONE.get() && combo.getTimeoutMS() < time) {
                time -= combo.getTimeoutMS();

                combo = ComboStateRegistry.REGISTRY.get().getValue(combo.getNextOfTimeout(entity)) != null
                        ? ComboStateRegistry.REGISTRY.get().getValue(combo.getNextOfTimeout(entity))
                        : ComboStateRegistry.NONE.get();
            }
            if (combo == ComboStateRegistry.NONE.get()) {
                combo = ComboStateRegistry.REGISTRY.get().getValue(s.getComboRoot()) != null
                        ? ComboStateRegistry.REGISTRY.get().getValue(s.getComboRoot())
                        : ComboStateRegistry.STANDBY.get();
            }

            MmdVmdMotionMc motion = BladeMotionManager.getInstance().getMotion(combo.getMotionLoc());

            double maxSeconds = 0;
            try {
                mmp.setVmd(motion);
                maxSeconds = TimeValueHelper.getMSecFromFrames(motion.getMaxFrame());
            } catch (Exception e) {
                e.printStackTrace();
            }

            double start = TimeValueHelper.getMSecFromFrames(combo.getStartFrame());
            double end = TimeValueHelper.getMSecFromFrames(combo.getEndFrame());
            double span = Math.abs(end - start);

            span = Math.min(maxSeconds, span);

            if (combo.getLoop()) {
                time = time % span;
            }
            time = Math.min(span, time);

            time = start + time;

            try {
                mmp.updateMotion((float) time);
            } catch (MmdException e) {
                e.printStackTrace();
            }

            try (MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)) {

                setUserPose(matrixStack, entity, partialTicks);

                // minecraft model neckPoint height = 1.5f
                // mmd model neckPoint height = 12.0f
                matrixStack.translate(0, motionYOffset, 0);

                matrixStack.scale((float) motionScale, (float) motionScale, (float) motionScale);

                // transpoze mmd to mc
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));

                ResourceLocation textureLocation = s.getTexture().orElse(DefaultResources.resourceDefaultTexture);

                WavefrontObject obj = BladeModelManager.getInstance()
                        .getModel(s.getModel().orElse(DefaultResources.resourceDefaultModel));

                try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
                    int idx = mmp.getBoneIndexByName("hardpointA");

                    if (0 <= idx) {
                        float[] buf = new float[16];
                        mmp._skinning_mat[idx].getValue(buf);

                        Matrix4f mat = VectorHelper.matrix4fFromArray(buf);

                        matrixStack.scale(-1, 1, 1);
                        PoseStack.Pose entry = matrixStack.last();
                        entry.pose().mul(mat);
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
                    BladeRenderState.renderOverridedLuminous(stack, obj, part + "_luminous", textureLocation,
                            matrixStack, bufferIn, lightIn);
                }

                try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
                    int idx = mmp.getBoneIndexByName("hardpointB");

                    if (0 <= idx) {
                        float[] buf = new float[16];
                        mmp._skinning_mat[idx].getValue(buf);

                        Matrix4f mat = VectorHelper.matrix4fFromArray(buf);

                        matrixStack.scale(-1, 1, 1);
                        PoseStack.Pose entry = matrixStack.last();
                        entry.pose().mul(mat);
                        matrixStack.scale(-1, 1, 1);
                    }

                    float modelScale = (float) (modelScaleBase * (1.0f / motionScale));
                    matrixStack.scale(modelScale, modelScale, modelScale);

                    BladeRenderState.renderOverrided(stack, obj, "sheath", textureLocation, matrixStack, bufferIn,
                            lightIn);
                    BladeRenderState.renderOverridedLuminous(stack, obj, "sheath_luminous", textureLocation,
                            matrixStack, bufferIn, lightIn);

                    if (s.isCharged(entity)) {
                        float f = (float) entity.tickCount + partialTicks;
                        BladeRenderState.renderChargeEffect(stack, f, obj, "effect",
                                new ResourceLocation("textures/entity/creeper/creeper_armor.png"), matrixStack,
                                bufferIn, lightIn);
                    }

                }

            }

        }));
    }

    public void setUserPose(PoseStack matrixStack, T entity, float partialTicks) {
        if (!UserPoseOverrider.UsePoseOverrider && entity instanceof AbstractClientPlayer) {
            var animationPlayer = ((IAnimatedPlayer) entity).playerAnimator_getAnimation();
            animationPlayer.setTickDelta(partialTicks);
            if (animationPlayer.isActive()) {
                Vec3f vec3d = animationPlayer.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
                matrixStack.translate(-vec3d.getX(), (vec3d.getY() + 0.7), -vec3d.getZ());
                // These are additive properties
                Vec3f vec3f = animationPlayer.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
                matrixStack.mulPose(Axis.ZP.rotation(vec3f.getZ())); // roll
                matrixStack.mulPose(Axis.YP.rotation(vec3f.getY())); // pitch
                matrixStack.mulPose(Axis.XP.rotation(vec3f.getX())); // yaw
                matrixStack.translate(0, -0.7d, 0);
            }
        } else {
            UserPoseOverrider.invertRot(matrixStack, entity, partialTicks);
        }
    }
}
