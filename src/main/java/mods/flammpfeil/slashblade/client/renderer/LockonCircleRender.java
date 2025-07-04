package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.Optional;

public class LockonCircleRender {
    private static final class SingletonHolder {
        private static final LockonCircleRender instance = new LockonCircleRender();
    }

    public static LockonCircleRender getInstance() {
        return SingletonHolder.instance;
    }

    private LockonCircleRender() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    static final ResourceLocation modelLoc = new ResourceLocation("slashblade", "model/util/lockon.obj");
    static final ResourceLocation textureLoc = new ResourceLocation("slashblade", "model/util/lockon.png");

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent<?, ?> event) {
        final Minecraft minecraftInstance = Minecraft.getInstance();
        Player player = minecraftInstance.player;
        if (player == null) {
            return;
        }
        if (player.getCapability(CapabilityInputState.INPUT_STATE)
                .filter(input -> input.getCommands().contains(InputCommand.SNEAK)).isEmpty()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        Level level = player.level();
        Optional<Color> effectColor = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s -> event.getEntity().equals(s.getTargetEntity(level))).map(ISlashBladeState::getEffectColor);

        if (effectColor.isEmpty()) {
            return;
        }

        LivingEntityRenderer<?, ?> renderer = event.getRenderer();
        LivingEntity livingEntity = event.getEntity();

        if (!livingEntity.isAlive()) {
            return;
        }

        float health = livingEntity.getHealth() / livingEntity.getMaxHealth();

        Color col = new Color(effectColor.get().getRGB() & 0xFFFFFF | 0xAA000000, true);

        PoseStack poseStack = event.getPoseStack();

        float f = livingEntity.getBbHeight() * 0.5f;
        float partialTicks = event.getPartialTick();

        poseStack.pushPose();
        poseStack.translate(0.0D, f, 0.0D);

        Vec3 offset = renderer.entityRenderDispatcher.camera.getPosition()
                .subtract(livingEntity.getPosition(partialTicks).add(0, f, 0));
        offset = offset.scale(0.5f);
        poseStack.translate(offset.x(), offset.y(), offset.z());

        poseStack.mulPose(renderer.entityRenderDispatcher.cameraOrientation());
        // poseStack.scale(-0.025F, -0.025F, 0.025F);

        float scale = 0.0025f;
        poseStack.scale(scale, -scale, scale);

        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLoc);
        ResourceLocation resourceTexture = textureLoc;

        MultiBufferSource buffer = event.getMultiBufferSource();

        final String base = "lockonBase";
        final String mask = "lockonHealthMask";
        final String value = "lockonHealth";

        BladeRenderState.setCol(col);
        BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, base, resourceTexture, poseStack, buffer,
                BladeRenderState.MAX_LIGHT);
        {
            poseStack.pushPose();
            poseStack.translate(0, 0, health * 10.0f);
            BladeRenderState.setCol(new Color(0x20000000, true));
            BladeRenderState.renderOverridedLuminousDepthWrite(ItemStack.EMPTY, model, mask, resourceTexture, poseStack,
                    buffer, BladeRenderState.MAX_LIGHT);
            poseStack.popPose();
        }
        BladeRenderState.setCol(col);
        BladeRenderState.renderOverridedLuminousDepthWrite(ItemStack.EMPTY, model, value, resourceTexture, poseStack,
                buffer, BladeRenderState.MAX_LIGHT);

        poseStack.popPose();
    }
}
