package mods.flammpfeil.slashblade.compat.playerAnim;

import com.google.common.collect.Maps;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.BladeMotionEvent;
import mods.flammpfeil.slashblade.event.client.PlayerAnimationInitEvent;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 玩家动画覆盖器：监听 {@link BladeMotionEvent}，将 ComboState 对应的动画注入
 * playeranimator 的 {@link AnimationStack} 第 0 层。
 */
public class PlayerAnimationOverrider {
    private final Map<ResourceLocation, Supplier<IAnimation>> animation = initAnimations();
    
    private static final class SingletonHolder {
        private static final PlayerAnimationOverrider instance = new PlayerAnimationOverrider();
    }
    
    public static PlayerAnimationOverrider getInstance() {
        return PlayerAnimationOverrider.SingletonHolder.instance;
    }
    
    private PlayerAnimationOverrider() {
    }
    
    /**
     * 注册到游戏总线，并在初始化完成前发布 {@link PlayerAnimationInitEvent}，
     * 允许附属 mod 在默认 VMD 绑定之上注册/覆盖 ComboState → IAnimation 绑定。
     */
    public void register() {
        SlashBlade.MOD_EVENT_BUS.post(new PlayerAnimationInitEvent(this.animation));
        NeoForge.EVENT_BUS.register(this);
    }
    
    private static final ResourceLocation MotionLocation = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "model/pa/player_motion.vmd");
    
    public Map<ResourceLocation, Supplier<IAnimation>> getAnimation() {
        return this.animation;
    }
    
    @SubscribeEvent
    public void onBladeAnimationStart(BladeMotionEvent event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        
        AnimationStack animationStack = PlayerAnimationAccess.getPlayerAnimLayer(player);
        
        Supplier<IAnimation> supplier = this.getAnimation().get(event.getCombo());
        
        if (supplier != null) {
            IAnimation animation = supplier.get();
            int elapsedTicks = Mth.floor(Math.max(0L, player.level().getGameTime() - event.getActionTime()) + 1L);
            animationStack.removeLayer(0);
            if (animation instanceof PlayerAnimationBase base) {
                base.play(elapsedTicks);
            }
            animationStack.addAnimLayer(0, animation);
        }
        
    }
    
    private Map<ResourceLocation, Supplier<IAnimation>> initAnimations() {
        Map<ResourceLocation, Supplier<IAnimation>> map = Maps.newHashMap();
        
        map.put(ComboStateRegistry.PIERCING.getId(), () -> new VmdAnimation(DefaultResources.testPLLocation, 1, 90, false));
        map.put(ComboStateRegistry.PIERCING_JUST.getId(), () -> new VmdAnimation(DefaultResources.testPLLocation, 34, 90, false));
        
        // guard
        map.put(ComboStateRegistry.COMBO_A1_END2.getId(), () -> new VmdAnimation(MotionLocation, 21, 41, false));
        
        map.put(ComboStateRegistry.COMBO_A1.getId(), () -> new VmdAnimation(MotionLocation, 1, 41, false));
        map.put(ComboStateRegistry.COMBO_A2.getId(), () -> new VmdAnimation(MotionLocation, 100, 151, false));
        map.put(ComboStateRegistry.COMBO_C.getId(), () -> new VmdAnimation(MotionLocation, 400, 488, false));
        map.put(ComboStateRegistry.COMBO_A3.getId(), () -> new VmdAnimation(MotionLocation, 200, 306, false));
        map.put(ComboStateRegistry.COMBO_A4.getId(), () -> new VmdAnimation(MotionLocation, 500, 608, false));
        
        map.put(ComboStateRegistry.COMBO_A4_EX.getId(), () -> new VmdAnimation(MotionLocation, 800, 894, false));
        map.put(ComboStateRegistry.COMBO_A5.getId(), () -> new VmdAnimation(MotionLocation, 900, 1061, false));
        
        map.put(ComboStateRegistry.COMBO_B1.getId(), () -> new VmdAnimation(MotionLocation, 700, 787, false));
        map.put(ComboStateRegistry.COMBO_B2.getId(), () -> new VmdAnimation(MotionLocation, 710, 787, false));
        map.put(ComboStateRegistry.COMBO_B3.getId(), () -> new VmdAnimation(MotionLocation, 710, 787, false));
        map.put(ComboStateRegistry.COMBO_B4.getId(), () -> new VmdAnimation(MotionLocation, 710, 787, false));
        map.put(ComboStateRegistry.COMBO_B5.getId(), () -> new VmdAnimation(MotionLocation, 710, 787, false));
        map.put(ComboStateRegistry.COMBO_B6.getId(), () -> new VmdAnimation(MotionLocation, 710, 787, false));
        map.put(ComboStateRegistry.COMBO_B7.getId(), () -> new VmdAnimation(MotionLocation, 710, 787, false));
        
        map.put(ComboStateRegistry.CIRCLE_SLASH.getId(), () -> new VmdAnimation(MotionLocation, 725, 787, false));
        
        map.put(ComboStateRegistry.AERIAL_RAVE_A1.getId(), () -> new VmdAnimation(MotionLocation, 1100, 1132, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.AERIAL_RAVE_A2.getId(), () -> new VmdAnimation(MotionLocation, 1200, 1241, false).setBlendLegs(false));
        map.put(ComboStateRegistry.AERIAL_RAVE_A3.getId(), () -> new VmdAnimation(MotionLocation, 1300, 1338, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.AERIAL_RAVE_B3.getId(), () -> new VmdAnimation(MotionLocation, 1400, 1443, false).setBlendLegs(false));
        map.put(ComboStateRegistry.AERIAL_RAVE_B4.getId(), () -> new VmdAnimation(MotionLocation, 1500, 1547, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.UPPERSLASH.getId(), () -> new VmdAnimation(MotionLocation, 1600, 1693, false));
        map.put(ComboStateRegistry.UPPERSLASH_JUMP.getId(), () -> new VmdAnimation(MotionLocation, 1700, 1717, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.AERIAL_CLEAVE.getId(), () -> new VmdAnimation(MotionLocation, 1800, 1817, false).setBlendLegs(false));
        map.put(ComboStateRegistry.AERIAL_CLEAVE_LOOP.getId(), () -> new VmdAnimation(MotionLocation, 1812, 1817, true).setBlendLegs(false));
        map.put(ComboStateRegistry.AERIAL_CLEAVE_LANDING.getId(), () -> new VmdAnimation(MotionLocation, 1816, 1886, false));
        
        map.put(ComboStateRegistry.RAPID_SLASH.getId(), () -> new VmdAnimation(MotionLocation, 2000, 2073, false).setBlendLegs(false));
        map.put(ComboStateRegistry.RAPID_SLASH_QUICK.getId(), () -> new VmdAnimation(MotionLocation, 2000, 2073, false).setBlendLegs(false));
        map.put(ComboStateRegistry.RISING_STAR.getId(), () -> new VmdAnimation(MotionLocation, 2100, 2147, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.JUDGEMENT_CUT.getId(), () -> new VmdAnimation(MotionLocation, 1900, 1963, false).setBlendLegs(false));
        map.put(ComboStateRegistry.JUDGEMENT_CUT_SLASH_AIR.getId(), () -> new VmdAnimation(MotionLocation, 1923, 1963, false).setBlendLegs(false));
        map.put(ComboStateRegistry.JUDGEMENT_CUT_SLASH_JUST.getId(), () -> new VmdAnimation(MotionLocation, 1923, 1963, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.VOID_SLASH.getId(), () -> new VmdAnimation(MotionLocation, 2200, 2299, false).setBlendLegs(false));
        
        map.put(ComboStateRegistry.SAKURA_END_LEFT.getId(), () -> new VmdAnimation(MotionLocation, 1816, 1859, false).setBlendLegs(false));
        map.put(ComboStateRegistry.SAKURA_END_RIGHT.getId(), () -> new VmdAnimation(MotionLocation, 204, 314, false).setBlendLegs(false));
        map.put(ComboStateRegistry.SAKURA_END_LEFT_AIR.getId(), () -> new VmdAnimation(MotionLocation, 1300, 1328, false).setBlendLegs(false));
        map.put(ComboStateRegistry.SAKURA_END_RIGHT_AIR.getId(), () -> new VmdAnimation(MotionLocation, 1200, 1241, false).setBlendLegs(false));
        map.put(ComboStateRegistry.DRIVE_HORIZONTAL.getId(), () -> new VmdAnimation(MotionLocation, 400, 488, false));
        map.put(ComboStateRegistry.DRIVE_VERTICAL.getId(), () -> new VmdAnimation(MotionLocation, 1600, 1693, false));
        map.put(ComboStateRegistry.WAVE_EDGE_VERTICAL.getId(), () -> new VmdAnimation(MotionLocation, 1600, 1693, false));
        map.put(ComboStateRegistry.JUDGEMENT_CUT_END.getId(), () -> new VmdAnimation(MotionLocation, 1923, 1963, false));
        
        return map;
    }
    
}
