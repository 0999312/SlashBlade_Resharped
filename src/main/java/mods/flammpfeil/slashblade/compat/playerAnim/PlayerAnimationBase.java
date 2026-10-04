package mods.flammpfeil.slashblade.compat.playerAnim;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * playeranimator {@link IAnimation} 的抽象基类，与具体动画来源（MMD、其他骨骼动画）解耦。
 * <p>
 * 承接与动画格式无关的通用状态机：播放/停止/计 tick、起止区间与循环、采样结果缓存、
 * 四肢混合开关、克隆模板。具体骨骼采样（{@link #get3DTransform(String, dev.kosmx.playerAnim.api.TransformType, float, dev.kosmx.playerAnim.core.util.Vec3f)}）
 * 与每帧动画更新（{@link #updateAnimation(float)}）由子类实现。
 */
public abstract class PlayerAnimationBase implements IAnimation {
    protected final ResourceLocation loc;
    protected final double start;
    protected final double end;
    protected final double span;
    protected double speed = 1;
    protected boolean loop;
    protected boolean blendArms = false;
    protected boolean blendLegs = true;
    
    protected double currentTick;
    protected double lastCachedTick = -1;
    protected float lastCachedPartial = -1.0f;
    protected boolean isRunning = true;
    
    protected PlayerAnimationBase(ResourceLocation loc, double start, double end, boolean loop) {
        this.loc = loc;
        this.start = start;
        this.end = end;
        this.span = TimeValueHelper.getTicksFromFrames((float) Math.abs(end - start));
        this.loop = loop;
        
        this.currentTick = 0;
    }
    
    @Override
    public void tick() {
        if (this.isRunning) {
            this.currentTick += this.speed;
            
            if (this.span <= this.currentTick) {
                if (this.loop) {
                    this.currentTick -= this.span;
                } else {
                    this.stop();
                }
            }
        }
    }
    
    public void play() {
        this.play(0);
    }
    
    public void play(int ticks) {
        this.currentTick = Math.max(0, ticks);
        this.isRunning = true;
    }
    
    public void stop() {
        this.isRunning = false;
    }
    
    @Override
    public boolean isActive() {
        return this.isRunning;
    }
    
    public PlayerAnimationBase setBlendArms(boolean blend) {
        this.blendArms = blend;
        return this;
    }
    
    public PlayerAnimationBase setBlendLegs(boolean blend) {
        this.blendLegs = blend;
        return this;
    }
    
    public PlayerAnimationBase setSpeed(double speed) {
        this.speed = speed;
        return this;
    }
    
    public PlayerAnimationBase getClone() {
        PlayerAnimationBase tmp = this.createClone();
        tmp.setBlendArms(this.blendArms);
        tmp.setBlendLegs(this.blendLegs);
        tmp.setSpeed(this.speed);
        return tmp;
    }
    
    protected abstract PlayerAnimationBase createClone();
    
    @Override
    public void setupAnim(float tickDelta) {
        if (this.currentTick == this.lastCachedTick
            && Float.floatToIntBits(tickDelta) == Float.floatToIntBits(this.lastCachedPartial)) {
            return;
        }
        this.lastCachedTick = this.currentTick;
        this.lastCachedPartial = tickDelta;
        
        this.updateAnimation((float) (tickDelta * this.speed));
    }
    
    protected abstract void updateAnimation(float tickDelta);
}
