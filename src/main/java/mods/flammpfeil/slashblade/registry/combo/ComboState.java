package mods.flammpfeil.slashblade.registry.combo;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.ArrowReflector;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComboState {
    public static final ResourceKey<Registry<ComboState>> REGISTRY_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation(SlashBlade.MODID, "combo_state"));

    private final ResourceLocation motionLoc;

    // frame
    private final int start;
    // frame
    private final int end;

    private final float speed;
    private final boolean loop;

    // Next input acceptance period *ms
    public int timeout;

    private final Function<LivingEntity, ResourceLocation> next;
    private final Function<LivingEntity, ResourceLocation> nextOfTimeout;

    private final Consumer<LivingEntity> holdAction;

    private final Consumer<LivingEntity> tickAction;

    private final BiConsumer<LivingEntity, LivingEntity> hitEffect;

    private final Consumer<LivingEntity> clickAction;

    private final BiFunction<LivingEntity, Integer, SlashArts.ArtsType> releaseAction;

    private final boolean isAerial;

    private final int priority;

    public ResourceLocation getMotionLoc() {
        return motionLoc;
    }

    public int getStartFrame() {
        return start;
    }

    public int getEndFrame() {
        return end;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean getLoop() {
        return loop;
    }

    public int getTimeoutMS() {
        return (int) (TimeValueHelper.getMSecFromFrames(Math.abs(getEndFrame() - getStartFrame())) / getSpeed())
                + timeout;
    }

    public void holdAction(LivingEntity user) {
        holdAction.accept(user);
    }

    public void tickAction(LivingEntity user) {
        tickAction.accept(user);
    }

    public void hitEffect(LivingEntity target, LivingEntity attacker) {
        hitEffect.accept(target, attacker);
    }

    public void clickAction(LivingEntity user) {
        clickAction.accept(user);
    }

    public SlashArts.ArtsType releaseAction(LivingEntity user, int elapsed) {
        return this.releaseAction.apply(user, elapsed);
    }

    public static ResourceLocation getRegistryKey(ComboState state) {
        return ComboStateRegistry.REGISTRY.get().getKey(state);
    }

    private ComboState(Builder builder) {
        this.start = builder.start;
        this.end = builder.end;

        this.speed = builder.speed;
        this.timeout = builder.timeout;
        this.loop = builder.loop;

        this.motionLoc = builder.motionLoc;

        this.next = builder.next;
        this.nextOfTimeout = builder.nextOfTimeout;

        this.holdAction = builder.holdAction;

        this.tickAction = builder.tickAction;

        this.hitEffect = builder.hitEffect;

        this.clickAction = builder.clickAction;

        this.releaseAction = builder.releaseAction;

        this.isAerial = builder.aerial;

        this.priority = builder.priority;
    }

    public ResourceLocation getNext(LivingEntity living) {
        return this.next.apply(living);
    }

    public ResourceLocation getNextOfTimeout(LivingEntity living) {
        return this.nextOfTimeout.apply(living);
    }

    @Nonnull
    public ComboState checkTimeOut(LivingEntity living, float msec) {
        return this.getTimeoutMS() < msec ? Objects.requireNonNull(ComboStateRegistry.REGISTRY.get().getValue(this.nextOfTimeout.apply(living)))
                : this;
    }

    public boolean isAerial() {
        return this.isAerial;
    }

    public int getPriority() {
        return priority;
    }

    static public SlashArts.ArtsType releaseActionQuickCharge(LivingEntity user, Integer elapsed) {
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, user);
        if (elapsed <= 3 + level) {
            AdvancementHelper.grantedIf(Enchantments.SOUL_SPEED, user);
            AdvancementHelper.grantCriterion(user, AdvancementHelper.ADVANCEMENT_QUICK_CHARGE);
            return SlashArts.ArtsType.Jackpot;
        } else {
            return SlashArts.ArtsType.Fail;
        }
    }

    public static class TimeoutNext implements Function<LivingEntity, ResourceLocation> {

        long timeout;
        Function<LivingEntity, ResourceLocation> next;

        static public TimeoutNext buildFromFrame(int timeoutFrame, Function<LivingEntity, ResourceLocation> next) {
            return new TimeoutNext((int) TimeValueHelper.getTicksFromFrames(timeoutFrame), next);
        }

        public TimeoutNext(long timeout, Function<LivingEntity, ResourceLocation> next) {
            this.timeout = timeout;
            this.next = next;
        }

        @Override
        public ResourceLocation apply(LivingEntity livingEntity) {

            long elapsed = ComboState.getElapsed(livingEntity);

            if (timeout <= elapsed) {
                return next.apply(livingEntity);
            } else {
                return livingEntity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                        .map(ISlashBladeState::getComboSeq).orElse(SlashBlade.prefix("none"));
            }
        }
    }

    public static class TimeLineTickAction implements Consumer<LivingEntity> {
        long offset = -1;

        public static TimeLineTickActionBuilder getBuilder() {
            return new TimeLineTickActionBuilder();
        }

        public static class TimeLineTickActionBuilder {
            Map<Integer, Consumer<LivingEntity>> timeLine = Maps.newHashMap();

            public TimeLineTickActionBuilder put(int ticks, Consumer<LivingEntity> action) {
                timeLine.put(ticks, action);
                return this;
            }

            public TimeLineTickAction build() {
                return new TimeLineTickAction(timeLine);
            }
        }

        Map<Integer, Consumer<LivingEntity>> timeLine = Maps.newHashMap();

        TimeLineTickAction(Map<Integer, Consumer<LivingEntity>> timeLine) {
            this.timeLine.putAll(timeLine);

        }

        @Override
        public void accept(LivingEntity livingEntity) {
            long elapsed = getElapsed(livingEntity);

            if (offset < 0) {
                offset = elapsed;
            }
            elapsed -= offset;

            Consumer<LivingEntity> action = timeLine.getOrDefault((int) elapsed, this::defaultConsumer);

            action.accept(livingEntity);
        }

        void defaultConsumer(LivingEntity entityIn) {
        }
    }

    static public long getElapsed(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                .map((state) -> state.getElapsedTime(livingEntity)).orElse(0L);
    }

    public static class Builder {
        private int priority;
        private int start;
        private int end;
        private float speed;
        private boolean loop;
        private int timeout;
        private ResourceLocation motionLoc;
        private Function<LivingEntity, ResourceLocation> next;
        private Function<LivingEntity, ResourceLocation> nextOfTimeout;

        private boolean aerial;

        private Consumer<LivingEntity> holdAction;
        private Consumer<LivingEntity> tickAction;
        private BiConsumer<LivingEntity, LivingEntity> hitEffect;
        private Consumer<LivingEntity> clickAction;
        private BiFunction<LivingEntity, Integer, SlashArts.ArtsType> releaseAction;

        private Builder() {
            this.motionLoc = DefaultResources.ExMotionLocation;
            this.priority = 1000;
            this.timeout = 0;
            this.speed = 1.0F;
            this.loop = false;
            this.aerial = false;
            this.next = entity -> SlashBlade.prefix("none");
            this.tickAction = ArrowReflector::doTicks;
            this.releaseAction = (u, e) -> SlashArts.ArtsType.Fail;
            this.holdAction = (a) -> {
            };
            this.hitEffect = (a, b) -> {
            };
            this.clickAction = (user) -> {
            };
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public ComboState build() {
            return new ComboState(this);
        }

        public Builder startAndEnd(int start, int end) {
            this.start = start;
            this.end = end;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder loop() {
            this.loop = true;
            return this;
        }

        public Builder aerial() {
            this.aerial = true;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder motionLoc(ResourceLocation motionLoc) {
            this.motionLoc = motionLoc;
            return this;
        }

        public Builder next(Function<LivingEntity, ResourceLocation> next) {
            this.next = next;
            return this;
        }

        public Builder nextOfTimeout(Function<LivingEntity, ResourceLocation> nextOfTimeout) {
            this.nextOfTimeout = nextOfTimeout;
            return this;
        }

        public Builder addHoldAction(Consumer<LivingEntity> holdAction) {
            this.holdAction = this.holdAction.andThen(holdAction);
            return this;
        }

        public Builder addTickAction(Consumer<LivingEntity> tickAction) {
            this.tickAction = this.tickAction.andThen(tickAction);
            return this;
        }

        public Builder addHitEffect(BiConsumer<LivingEntity, LivingEntity> hitEffect) {
            this.hitEffect = this.hitEffect.andThen(hitEffect);
            return this;
        }

        public Builder clickAction(Consumer<LivingEntity> clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        public Builder releaseAction(BiFunction<LivingEntity, Integer, SlashArts.ArtsType> clickAction) {
            this.releaseAction = clickAction;
            return this;
        }

    }
}
