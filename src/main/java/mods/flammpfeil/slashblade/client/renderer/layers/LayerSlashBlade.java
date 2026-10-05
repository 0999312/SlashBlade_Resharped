package mods.flammpfeil.slashblade.client.renderer.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.data.tag.SlashBladeEntityTypeTagProvider.EntityTypeTags;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * SlashBlade 的实体渲染层抽象基类。
 * <p>
 * 负责与动画来源无关的公共流程：
 * <ul>
 *     <li>副手 / 快捷栏 / 待机携带的刀渲染；</li>
 *     <li>当前 ComboState 与播放时间的解析（帧 → 毫秒换算、循环与截断）；</li>
 *     <li>按刀的 ComboRoot 自动区分调用：若存在附属 mod 通过
 *     {@link mods.flammpfeil.slashblade.event.client.LayerSlashBladeInitEvent}
 *     注册的绑定层，则主刀渲染委托给该层，否则由本层完成。</li>
 * </ul>
 * 主刀的实际渲染由子类实现，本类不依赖任何 MMD 类型。
 *
 * @param <T> 实体类型
 * @param <M> 实体模型类型
 */
public abstract class LayerSlashBlade<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public static final Quaternionf CARRY_ROTATION_PSO2 =
        new Quaternionf().rotateZYX(-0.122173F, 0, 0);
    public static final Quaternionf CARRY_ROTATION_KATANA =
        new Quaternionf().rotateZYX(3.1415927F, 1.570796f, 0.261799F);
    public static final Quaternionf CARRY_ROTATION_DEFAULT =
        new Quaternionf().rotateZYX(0F, 1.570796f, 0.261799F);
    public static final Quaternionf CARRY_ROTATION_NINJA =
        new Quaternionf().rotateZYX(-2.094395F, 0f, 3.1415927F);
    public static final Quaternionf CARRY_ROTATION_RNINJA =
        new Quaternionf().rotateZYX(-1.047198F, 0, 0);
    
    protected static final String BLADE_LUMINOUS = "blade_luminous";
    protected static final String BLADE_DAMAGED_LUMINOUS = "blade_damaged_luminous";
    protected static final String SHEATH_LUMINOUS = "sheath_luminous";
    
    /**
     * 本层绑定的 ComboRoot；{@code null} 表示默认层（负责副手等公共渲染与主刀分发）。
     * 非默认层仅在被默认层委托时渲染主刀。
     */
    @Nullable
    protected ResourceLocation comboRoot;
    
    /**
     * 同渲染器下按 ComboRoot 索引的绑定层实例，由装配方（ClientHandler）填充。
     */
    protected final Map<ResourceLocation, LayerSlashBlade<T, M>> boundLayers = Maps.newHashMap();
    
    public LayerSlashBlade(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }
    
    /**
     * 设置本层绑定的 ComboRoot；{@code null} 表示默认层。
     *
     * @param comboRoot 绑定目标 ComboRoot
     */
    public void setComboRoot(@Nullable ResourceLocation comboRoot) {
        this.comboRoot = comboRoot;
    }
    
    /**
     * 将同渲染器下的绑定层实例登记到默认层，供渲染时按 ComboRoot 自动区分调用。
     *
     * @param comboRoot 该绑定层对应的 ComboRoot
     * @param layer     绑定层实例
     */
    public void registerBoundLayer(ResourceLocation comboRoot, LayerSlashBlade<T, M> layer) {
        this.boundLayers.put(comboRoot, layer);
    }
    
    /**
     * 依据刀上配置的 ComboRoot 解析对应的绑定层实例；未命中返回 {@code null}。
     *
     * @param comboRoot 刀上配置的 ComboRoot
     * @return 匹配的绑定层实例，可能为 {@code null}
     */
    @Nullable
    protected LayerSlashBlade<T, M> resolveBoundLayer(@Nullable ResourceLocation comboRoot) {
        if (comboRoot == null) {
            return null;
        }
        return this.boundLayers.get(comboRoot);
    }
    
    public float modifiedSpeed(float baseSpeed, LivingEntity entity) {
        float modif = 6.0f;
        if (MobEffectUtil.hasDigSpeed(entity)) {
            modif = 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else {
            MobEffectInstance effect = entity.getEffect(MobEffects.DIG_SLOWDOWN);
            if (effect != null) {
                modif = 6 + (1 + effect.getAmplifier()) * 2;
            }
        }
        
        modif /= 6.0f;
        
        return baseSpeed / modif;
    }
    
    public void renderOffhandItem(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity) {
        
        ItemStack offhandStack = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (offhandStack.isEmpty() || BladeStateAccess.of(offhandStack).isEmpty()) {
            this.renderHotbarItem(matrixStack, bufferIn, lightIn, entity);
            return;
        }
        
        this.renderStandbyBlade(matrixStack, bufferIn, lightIn, offhandStack, entity);
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
            
            this.renderStandbyBlade(matrixStack, bufferIn, lightIn, blade, entity);
        }
    }
    
    public void renderStandbyBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack blade, T entity) {
        Optional<ISlashBladeState> state = BladeStateAccess.of(blade);
        state.ifPresent(s -> {
            double modelScaleBase = 0.0078125F; // 0.5^7
            double motionScale = 1.5 / 12.0;
            ResourceLocation textureLocation = s.getTexture().orElse(DefaultResources.resourceDefaultTexture);
            
            WavefrontObject obj = BladeModelManager.getInstance()
                .getModel(s.getModel().orElse(DefaultResources.resourceDefaultModel));
            String part;
            try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(matrixStack)) {
                // minecraft model neckPoint height = 1.5f
                // mmd model neckPoint height = 12.0f
                matrixStack.translate(0, 1.5f, 0);
                CarryType carrytype = s.getCarryType();
                final Minecraft mcinstance = Minecraft.getInstance();
                if (carrytype.cancelFirstPersonRender
                    && mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                    && entity.equals(mcinstance.player)) {
                    return;
                }
                carrytype.standbyRenderAction.accept(matrixStack);
                
                float modelScale = (float) (modelScaleBase * (1.0f / motionScale));
                matrixStack.scale((float) motionScale, (float) motionScale, (float) motionScale);
                matrixStack.scale(modelScale, modelScale, modelScale);
                
                try (MSAutoCloser ignored1 = MSAutoCloser.pushMatrix(matrixStack)) {
                    if (s.isBroken()) {
                        part = "blade_damaged";
                    } else {
                        part = "blade";
                    }
                    
                    BladeRenderState.renderOverrided(blade, obj, part, textureLocation, matrixStack, bufferIn,
                        lightIn);
                    BladeRenderState.renderOverridedLuminous(blade, obj,
                        s.isBroken() ? BLADE_DAMAGED_LUMINOUS : BLADE_LUMINOUS, textureLocation,
                        matrixStack, bufferIn, lightIn);
                    BladeRenderState.renderOverrided(blade, obj, "sheath", textureLocation, matrixStack, bufferIn,
                        lightIn);
                    BladeRenderState.renderOverridedLuminous(blade, obj, SHEATH_LUMINOUS, textureLocation,
                        matrixStack, bufferIn, lightIn);
                }
            }
        });
    }
    
    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderOffhandItem(matrixStack, bufferIn, lightIn, entity);
        
        ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
        
        if (stack.isEmpty()) {
            return;
        }
        
        if (entity.getType().is(EntityTypeTags.RENDER_LAYER_BLACKLIST)) {
            return;
        }
        
        Optional<ISlashBladeState> state = BladeStateAccess.of(stack);
        state.ifPresent(s -> {
            Map.Entry<Integer, ResourceLocation> comboStateTicks = s.peekCurrentComboStateTicks(entity);
            ComboState combo = Objects.requireNonNullElse(
                ComboStateRegistry.REGISTRY.get(comboStateTicks.getValue()),
                ComboStateRegistry.NONE.get());
            double time = TimeValueHelper.getMSecFromTicks(comboStateTicks.getKey() + partialTicks);
            if (combo == ComboStateRegistry.NONE.get()) {
                combo = ComboStateRegistry.REGISTRY.get(s.getComboRoot()) != null
                    ? ComboStateRegistry.REGISTRY.get(s.getComboRoot())
                    : ComboStateRegistry.STANDBY.get();
            }
            
            // 动画总时长由子类按自身动画来源提供，用于对 span 截断。
            if (combo != null) {
                double maxSeconds = this.getMaxSeconds(combo);
                
                double start = TimeValueHelper.getMSecFromFrames(combo.getStartFrame());
                double end = TimeValueHelper.getMSecFromFrames(combo.getEndFrame());
                double span = Math.abs(end - start);
                float speed = combo.getSpeed();
                
                span = Math.min(maxSeconds, span) / speed;
                
                if (combo.getLoop()) {
                    time = time % span;
                }
                time = Math.min(span, time);
                
                time = start + time * speed;
                
                // 按刀的 ComboRoot 自动区分调用：命中绑定层则委托，否则由默认层渲染主刀。
                LayerSlashBlade<T, M> boundLayer = this.resolveBoundLayer(s.getComboRoot());
                if (boundLayer != null) {
                    boundLayer.renderMainBlade(matrixStack, bufferIn, lightIn, entity, partialTicks, stack, s, combo,
                        time);
                } else {
                    this.renderMainBlade(matrixStack, bufferIn, lightIn, entity, partialTicks, stack, s, combo, time);
                }
            }
        });
    }
    
    protected abstract void renderMainBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn,
                                            T entity, float partialTicks, ItemStack stack,
                                            ISlashBladeState state, ComboState combo, double timeMSec);
    
    protected abstract double getMaxSeconds(ComboState combo);
    
    public void setUserPose(PoseStack matrixStack, T entity, float partialTicks) {
        this.setUserPose(matrixStack, entity, partialTicks, null);
    }
    
    public void setUserPose(PoseStack matrixStack, T entity, float partialTicks, @Nullable ISlashBladeState state) {
        if (ModList.get().isLoaded("playeranimator") && entity instanceof AbstractClientPlayer) {
            AnimationApplier animationPlayer = ((IAnimatedPlayer) entity).playerAnimator_getAnimation();
            animationPlayer.setTickDelta(partialTicks);
            if (animationPlayer.isActive()) {
                Vec3f vec3d = animationPlayer.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
                matrixStack.translate(-vec3d.getX(), (vec3d.getY() + 0.7), -vec3d.getZ());
                Vec3f vec3f = animationPlayer.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
                matrixStack.mulPose(Axis.ZP.rotation(vec3f.getZ()));
                matrixStack.mulPose(Axis.YP.rotation(vec3f.getY()));
                matrixStack.mulPose(Axis.XP.rotation(vec3f.getX()));
                matrixStack.translate(0, -0.7d, 0);
                return;
            }
        }
        
        float comboRot = state != null
            ? UserPoseOverrider.getInterpolatedComboRotation(state, entity, partialTicks)
            : UserPoseOverrider.getInterpolatedComboRotation(entity, partialTicks);
        if (comboRot != 0f) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(comboRot));
        }
    }
}
