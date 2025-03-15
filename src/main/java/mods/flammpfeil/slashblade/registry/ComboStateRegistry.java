package mods.flammpfeil.slashblade.registry;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.event.FallHandler;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.combo.ComboCommands;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.slasharts.*;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import mods.flammpfeil.slashblade.util.AttackManager;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.KnockBacks;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ComboStateRegistry {
    public static final DeferredRegister<ComboState> COMBO_STATE = DeferredRegister.create(ComboState.REGISTRY_KEY,
            SlashBlade.MODID);

    public static final Supplier<IForgeRegistry<ComboState>> REGISTRY = COMBO_STATE.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<ComboState> NONE = COMBO_STATE.register("none",
            ComboState.Builder.newInstance().startAndEnd(0, 1).loop().motionLoc(DefaultResources.ExMotionLocation)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(UserPoseOverrider::resetRot)::build);

    public static final RegistryObject<ComboState> STANDBY = COMBO_STATE.register("standby",
            ComboState.Builder.newInstance().startAndEnd(0, 1).loop().timeout(1000)
                    .motionLoc(DefaultResources.ExMotionLocation).next(ComboCommands::initStandByCommand)
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(UserPoseOverrider::resetRot)::build);

    public static final RegistryObject<ComboState> COMBO_A1 = COMBO_STATE.register("combo_a1", ComboState.Builder
            .newInstance().startAndEnd(1, 10).priority(100).motionLoc(DefaultResources.ExMotionLocation)
            .next(ComboState.TimeoutNext.buildFromFrame(5, entity -> SlashBlade.prefix("combo_a2")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_a1_end"))
            .clickAction(entity -> AttackManager.doSlash(entity, -10, true,false,0.22))
            .addTickAction(entity -> UserPoseOverrider.resetRot(entity)).addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_A1_END = COMBO_STATE.register("combo_a1_end",
            ComboState.Builder.newInstance().startAndEnd(10, 21).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("combo_a2"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a1_end2"))
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_A1_END2 = COMBO_STATE.register("combo_a1_end2",
            ComboState.Builder.newInstance().startAndEnd(21, 41).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))::build);

    public static final RegistryObject<ComboState> COMBO_A2 = COMBO_STATE.register("combo_a2", ComboState.Builder
            .newInstance().startAndEnd(100, 115).priority(100).motionLoc(DefaultResources.ExMotionLocation)
            .next(ComboState.TimeoutNext.buildFromFrame(5, entity -> SlashBlade.prefix("combo_a3")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_a2_end"))
            .clickAction((e) -> AttackManager.doSlash(e, 180 - 10, true,false,0.22)).addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_A2_END = COMBO_STATE.register("combo_a2_end",
            ComboState.Builder.newInstance().startAndEnd(115, 132).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("combo_c"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a2_end2"))
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_A2_END2 = COMBO_STATE.register("combo_a2_end2",
            ComboState.Builder.newInstance().startAndEnd(132, 151).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))::build);

    public static final RegistryObject<ComboState> COMBO_C = COMBO_STATE.register("combo_c",
            ComboState.Builder.newInstance().startAndEnd(400, 459).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation)
                    .next(ComboState.TimeoutNext.buildFromFrame(15, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_c_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -30,false,false,0.44))
                            .put(3, (entityIn) -> AttackManager.doSlash(entityIn, -35, true,false,0.44)).build())
                    .addHitEffect(StunManager::setStun).clickAction(
                            a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_COMBO_C))::build);
    public static final RegistryObject<ComboState> COMBO_C_END = COMBO_STATE.register("combo_c_end",
            ComboState.Builder.newInstance().startAndEnd(459, 488).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_A3 = COMBO_STATE.register("combo_a3",
            ComboState.Builder.newInstance().startAndEnd(200, 218)

                    .priority(100)
                    .next(ComboState.TimeoutNext.buildFromFrame(9,
                            entity -> (entity.hasEffect(MobEffects.DAMAGE_BOOST) || entity.hasEffect(MobEffects.HUNGER))
                                    ? SlashBlade.prefix("combo_a4_ex")
                                    : SlashBlade.prefix("combo_a4")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a3_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -61,false,false,0.22))
                            .put(6, (entityIn) -> AttackManager.doSlash(entityIn, 180 - 42,false,false,0.22)).build())
                    .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_A3_END = COMBO_STATE.register("combo_a3_end",
            ComboState.Builder.newInstance().startAndEnd(218, 230).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("combo_b1"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a3_end2"))::build);

    public static final RegistryObject<ComboState> COMBO_A3_END2 = COMBO_STATE.register("combo_a3_end2",
            ComboState.Builder.newInstance().startAndEnd(230, 281).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a3_end3"))::build);

    public static final RegistryObject<ComboState> COMBO_A3_END3 = COMBO_STATE.register("combo_a3_end3",
            ComboState.Builder.newInstance().startAndEnd(281, 306).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_A4 = COMBO_STATE.register("combo_a4",
            ComboState.Builder.newInstance().startAndEnd(500, 576).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation)
                    .next(ComboState.TimeoutNext.buildFromFrame(21, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a4_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(8, (entityIn) -> AttackManager.doSlash(entityIn, 45,false,false,0.22))
                            .put(9, (entityIn) -> AttackManager.doSlash(entityIn, 50, true,false,0.22)).build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(8 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(8 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(8 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(8 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(8 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(8 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .clickAction(a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_COMBO_A))
                    .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_A4_END = COMBO_STATE.register("combo_a4_end",
            ComboState.Builder.newInstance().startAndEnd(576, 608).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_A4_EX = COMBO_STATE.register("combo_a4_ex",
            ComboState.Builder.newInstance().startAndEnd(800, 839).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation)
                    .next(ComboState.TimeoutNext.buildFromFrame(22, entity -> SlashBlade.prefix("combo_a5ex")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a4_ex_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(7, (entityIn) -> AttackManager.doSlash(entityIn, 70,false,false,0.5))
                            .put(14, (entityIn) -> AttackManager.doSlash(entityIn, 180 + 75,false,false,0.5)).build())

                    .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_A4_EX_END = COMBO_STATE.register("combo_a4_ex_end",
            ComboState.Builder.newInstance().startAndEnd(839, 877).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a4_ex_end2"))::build);
    public static final RegistryObject<ComboState> COMBO_A4_EX_END2 = COMBO_STATE.register("combo_a4_ex_end2",
            ComboState.Builder.newInstance().startAndEnd(877, 894).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_A5 = COMBO_STATE.register("combo_a5ex",
            ComboState.Builder.newInstance().startAndEnd(900, 1013).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation)
                    .next(ComboState.TimeoutNext.buildFromFrame(33, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_a5ex_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(15, (entityIn) -> AttackManager.doSlash(entityIn, 35, false, true,0.5))
                            .put(17, (entityIn) -> AttackManager.doSlash(entityIn, 40, true, true,0.5))
                            .put(19, (entityIn) -> AttackManager.doSlash(entityIn, 30, true, true,0.5)).build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(13 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(13 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(13 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(13 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(13 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(13 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .clickAction(a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_COMBO_A_EX))
                    .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_A5_END = COMBO_STATE.register("combo_a5ex_end",
            ComboState.Builder.newInstance().startAndEnd(1013, 1061).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> COMBO_B1 = COMBO_STATE.register("combo_b1",
            ComboState.Builder.newInstance().startAndEnd(700, 720).priority(100)
                    .motionLoc(DefaultResources.ExMotionLocation)
                    .next(ComboState.TimeoutNext.buildFromFrame(13, entity -> SlashBlade.prefix("combo_b2")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_b1_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(6, (entityIn) -> {
                        AttackManager.doSlash(entityIn, -30, false, false, 0.122f);
                        AttackManager.doSlash(entityIn, 180 - 35, true, false, 0.122f);
                    }).put(7 + 0,
                            (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                                    AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                            .put(7 + 1,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            +90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                            .put(7 + 2,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            -90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                            .put(7 + 3,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            +90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                            .put(7 + 4,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            -90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                            .put(7 + 5,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            +90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                            .put(7 + 6,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            -90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                            .put(7 + 7,
                                    (entityIn) -> AttackManager.doSlash(entityIn,
                                            +90 + 180 * entityIn.getRandom().nextFloat(),
                                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))

                            .build())
                    .clickAction(a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_COMBO_B))
                    .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_B1_END = COMBO_STATE.register("combo_b1_end",
            ComboState.Builder.newInstance().startAndEnd(720, 743).priority(100)
                    .next(entity -> SlashBlade.prefix("combo_b1_end"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_b1_end2"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(12 - 3, (entityIn) -> AttackManager.doSlash(entityIn, 0,
                                    new Vec3(entityIn.getRandom().nextFloat() - 0.5f, 0.8f, 0), false, true, 1.0))
                            .put(13 - 3, (entityIn) -> AttackManager.doSlash(entityIn, 5,
                                    new Vec3(entityIn.getRandom().nextFloat() - 0.5f, 0.8f, 0), true, false, 1.0))
                            .build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(12 - 3 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_B1_END2 = COMBO_STATE.register("combo_b1_end2",
            ComboState.Builder.newInstance().startAndEnd(743, 764).priority(100)
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_b1_end3"))::build);
    public static final RegistryObject<ComboState> COMBO_B1_END3 = COMBO_STATE.register("combo_b1_end3",
            ComboState.Builder.newInstance().startAndEnd(764, 787).priority(100)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())::build);

    public static final RegistryObject<ComboState> COMBO_B2 = COMBO_STATE.register("combo_b2", ComboState.Builder
            .newInstance().startAndEnd(710, 720).priority(100)
            .next(ComboState.TimeoutNext.buildFromFrame(6, entity -> SlashBlade.prefix("combo_b3")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(1, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(3, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(4, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(5, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(6, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .build())
            .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_B3 = COMBO_STATE.register("combo_b3", ComboState.Builder
            .newInstance().startAndEnd(710, 720).priority(100)
            .next(ComboState.TimeoutNext.buildFromFrame(6, entity -> SlashBlade.prefix("combo_b4")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(1, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(3, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(4, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(5, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(6, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .build())
            .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_B4 = COMBO_STATE.register("combo_b4", ComboState.Builder
            .newInstance().startAndEnd(710, 720).priority(100)
            .next(ComboState.TimeoutNext.buildFromFrame(6, entity -> SlashBlade.prefix("combo_b5")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(1, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(3, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(4, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(5, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(6, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .build())
            .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_B5 = COMBO_STATE.register("combo_b5", ComboState.Builder
            .newInstance().startAndEnd(710, 720).priority(100)
            .next(ComboState.TimeoutNext.buildFromFrame(6, entity -> SlashBlade.prefix("combo_b6")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(1, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(3, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(4, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(5, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(6, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .build())
            .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_B6 = COMBO_STATE.register("combo_b6", ComboState.Builder
            .newInstance().startAndEnd(710, 720).priority(100)
            .next(ComboState.TimeoutNext.buildFromFrame(6, entity -> SlashBlade.prefix("combo_b7")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(1, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(3, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(4, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(5, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(6, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .build())
            .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> COMBO_B7 = COMBO_STATE.register("combo_b7", ComboState.Builder
            .newInstance().startAndEnd(710, 764).priority(100)
            .next(ComboState.TimeoutNext.buildFromFrame(33, entity -> SlashBlade.prefix("none")))
            .nextOfTimeout(entity -> SlashBlade.prefix("combo_b7_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(1, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(3, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(4, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(5, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))
                    .put(6, (entityIn) -> AttackManager.doSlash(entityIn, -90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), false, false, 0.122f))
                    .put(7, (entityIn) -> AttackManager.doSlash(entityIn, +90 + 180 * entityIn.getRandom().nextFloat(),
                            AttackManager.genRushOffset(entityIn), true, false, 0.122f))

                    .put(12, (entityIn) -> AttackManager.doSlash(entityIn, 0,
                            new Vec3(entityIn.getRandom().nextFloat() - 0.5f, 0.8f, 0), false, true, 0.122f))
                    .put(13, (entityIn) -> AttackManager.doSlash(entityIn, 5,
                            new Vec3(entityIn.getRandom().nextFloat() - 0.5f, 0.8f, 0), true, false, 0.122f))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(12 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
            .addHitEffect(StunManager::setStun)
            .clickAction(a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_COMBO_B_MAX))::build);
    public static final RegistryObject<ComboState> COMBO_B7_END3 = COMBO_STATE.register("combo_b7_end",
            ComboState.Builder.newInstance().startAndEnd(764, 787).priority(100)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);
    public static final RegistryObject<ComboState> COMBO_B_END = COMBO_STATE.register("combo_b_end",
            ComboState.Builder.newInstance().startAndEnd(720, 743).priority(100)
                    .next(entity -> SlashBlade.prefix("combo_b_end"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end2"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(12 - 3,
                            (entityIn) -> AttackManager.doSlash(entityIn, 0,
                                    new Vec3(entityIn.getRandom().nextFloat() - 0.5f, 0.8f, 0), false, true, 0.122f))
                            .put(13 - 3, (entityIn) -> AttackManager.doSlash(entityIn, 5,
                                    new Vec3(entityIn.getRandom().nextFloat() - 0.5f, 0.8f, 0), true, false, 0.122f))
                            .build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(12 - 3 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(12 - 3 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> COMBO_B_END2 = COMBO_STATE.register("combo_b_end2",
            ComboState.Builder.newInstance().startAndEnd(743, 764).priority(100)
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("combo_b_end3"))::build);
    public static final RegistryObject<ComboState> COMBO_B_END3 = COMBO_STATE.register("combo_b_end3",
            ComboState.Builder.newInstance().startAndEnd(764, 787).priority(100)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> AERIAL_RAVE_A1 = COMBO_STATE.register("aerial_rave_a1",
            ComboState.Builder.newInstance().startAndEnd(1100, 1122).priority(80).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(5, entity -> SlashBlade.prefix("aerial_rave_a2")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_rave_a1_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(3) + 0,
                                    (entityIn) -> AttackManager.doSlash(entityIn, -20,false,false,0.14f))
                            .build().andThen(FallHandler::fallDecrease))
                    .addHitEffect(StunManager::setStun)
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))::build);
    public static final RegistryObject<ComboState> AERIAL_RAVE_A1_END = COMBO_STATE.register("aerial_rave_a1_end",
            ComboState.Builder.newInstance().startAndEnd(1122, 1132).priority(80)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> AERIAL_RAVE_A2 = COMBO_STATE.register("aerial_rave_a2",
            ComboState.Builder.newInstance().startAndEnd(1200, 1210).priority(80).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(5, entity -> SlashBlade.prefix("aerial_rave_a3")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_rave_a2_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(3) + 0,
                                    (entityIn) -> AttackManager.doSlash(entityIn, 180 - 30,false,false,0.14f))
                            .build())
                    .addHitEffect(StunManager::setStun).addTickAction(FallHandler::fallDecrease)::build);
    public static final RegistryObject<ComboState> AERIAL_RAVE_A2_END = COMBO_STATE.register("aerial_rave_a2_end",
            ComboState.Builder.newInstance().startAndEnd(1210, 1231).priority(80).aerial()
                    .next(entity -> SlashBlade.prefix("aerial_rave_b3"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_rave_a2_end2"))
                    .addTickAction(FallHandler::fallDecrease)::build);
    public static final RegistryObject<ComboState> AERIAL_RAVE_A2_END2 = COMBO_STATE.register("aerial_rave_a2_end2",
            ComboState.Builder.newInstance().startAndEnd(1231, 1241).priority(80)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> AERIAL_RAVE_A3 = COMBO_STATE.register("aerial_rave_a3",
            ComboState.Builder.newInstance().startAndEnd(1300, 1328).priority(80).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(9, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_rave_a3_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(4) + 0,
                                    (entityIn) -> AttackManager.doSlash(entityIn, 0, Vec3.ZERO, false, false, 0.14,
                                            KnockBacks.smash))
                            .put((int) TimeValueHelper.getTicksFromFrames(4) + 1,
                                    (entityIn) -> AttackManager.doSlash(entityIn, -3, Vec3.ZERO, true, true, 0.14,
                                            KnockBacks.smash))
                            .build())
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun)
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn)).clickAction(
                            a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_AERIAL_A))::build);
    public static final RegistryObject<ComboState> AERIAL_RAVE_A3_END = COMBO_STATE.register("aerial_rave_a3_end",
            ComboState.Builder.newInstance().startAndEnd(1328, 1338).priority(80)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> AERIAL_RAVE_B3 = COMBO_STATE.register("aerial_rave_b3",
            ComboState.Builder.newInstance().startAndEnd(1400, 1437).priority(80).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(9, entity -> SlashBlade.prefix("aerial_rave_b4")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_rave_b3_end")).clickAction((entityIn) -> {
                        Vec3 motion = entityIn.getDeltaMovement();
                        entityIn.setDeltaMovement(motion.x, 0.6, motion.z);
                    })
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(5),
                                    (entityIn) -> AttackManager.doSlash(entityIn, 180 + 57, Vec3.ZERO, false, false,
                                            0.17, KnockBacks.toss))
                            .put((int) TimeValueHelper.getTicksFromFrames(10),
                                    (entityIn) -> AttackManager.doSlash(entityIn, 180 + 57, Vec3.ZERO, false, false,
                                            0.17, KnockBacks.toss))
                            .build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, (entityIn) -> UserPoseOverrider.setRot(entityIn, -90, true))
                            .put(1, (entityIn) -> UserPoseOverrider.setRot(entityIn, -90, true))
                            .put(2, (entityIn) -> UserPoseOverrider.setRot(entityIn, -90, true))
                            .put(3, (entityIn) -> UserPoseOverrider.setRot(entityIn, -90, true))
                            .put(4, (entityIn) -> UserPoseOverrider.setRot(entityIn, -120, true))
                            .put(5, (entityIn) -> UserPoseOverrider.setRot(entityIn, -120, true))
                            .put(6, (entityIn) -> UserPoseOverrider.setRot(entityIn, -120, true))
                            .put(7, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> AERIAL_RAVE_B3_END = COMBO_STATE.register("aerial_rave_b3_end",
            ComboState.Builder.newInstance().startAndEnd(1437, 1443).priority(80)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> AERIAL_RAVE_B4 = COMBO_STATE.register("aerial_rave_b4",
            ComboState.Builder.newInstance().startAndEnd(1500, 1537).priority(80).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(9, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_rave_b4_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(10) + 0,
                                    (entityIn) -> AttackManager.doSlash(entityIn, 45, Vec3.ZERO, false, false, 0.17,
                                            KnockBacks.meteor))
                            .put((int) TimeValueHelper.getTicksFromFrames(10) + 1,
                                    (entityIn) -> AttackManager.doSlash(entityIn, 50, Vec3.ZERO, true, true, 0.17,
                                            KnockBacks.meteor))
                            .build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(5 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(5 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(5 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(5 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(5 + 4, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun).clickAction(
                            a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_AERIAL_B))::build);
    public static final RegistryObject<ComboState> AERIAL_RAVE_B4_END = COMBO_STATE.register("aerial_rave_b4_end",
            ComboState.Builder.newInstance().startAndEnd(1537, 1547).priority(80)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    private static final EnumSet<InputCommand> ex_upperslash_command = EnumSet.of(InputCommand.BACK,
            InputCommand.R_DOWN);
    public static final RegistryObject<ComboState> UPPERSLASH = COMBO_STATE.register("upperslash",
            ComboState.Builder.newInstance().startAndEnd(1600, 1659).priority(90)
                    .next(ComboState.TimeoutNext.buildFromFrame(11, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("upperslash_end")).addHoldAction((player) -> {
                        int elapsed = player.getTicksUsingItem();

                        int fireTime = (int) TimeValueHelper.getTicksFromFrames(9);
                        if (fireTime != elapsed)
                            return;

                        EnumSet<InputCommand> commands = player.getCapability(CapabilityInputState.INPUT_STATE)
                                .map((state) -> state.getCommands(player))
                                .orElseGet(() -> EnumSet.noneOf(InputCommand.class));

                        if (!commands.containsAll(ex_upperslash_command))
                            return;

                        player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                            state.updateComboSeq(player, SlashBlade.prefix("upperslash_jump"));
                            AdvancementHelper.grantCriterion(player, AdvancementHelper.ADVANCEMENT_UPPERSLASH_JUMP);
                        });
                    })
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(7),
                                    (entityIn) -> AttackManager.doSlash(entityIn, -80, Vec3.ZERO, false, false, 0.25,
                                            KnockBacks.toss))
                            .build())
                    .addHitEffect((t, a) -> StunManager.setStun(t, 15)).clickAction(
                            a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_UPPERSLASH))::build);
    public static final RegistryObject<ComboState> UPPERSLASH_END = COMBO_STATE.register("upperslash_end",
            ComboState.Builder.newInstance().startAndEnd(1659, 1693).priority(90)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> UPPERSLASH_JUMP = COMBO_STATE.register("upperslash_jump",
            ComboState.Builder.newInstance().startAndEnd(1700, 1713).priority(90).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(7, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("upperslash_jump_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(4, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun)
                    .clickAction((entityIn) -> {
                        Vec3 motion = entityIn.getDeltaMovement();
                        entityIn.setDeltaMovement(motion.x, 0.6f, motion.z);

                        entityIn.setOnGround(false);
                        entityIn.hasImpulse = true;
                    })::build);
    public static final RegistryObject<ComboState> UPPERSLASH_JUMP_END = COMBO_STATE.register("upperslash_jump_end",
            ComboState.Builder.newInstance().startAndEnd(1713, 1717).priority(90)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(
                            ComboState.TimeLineTickAction.getBuilder().put(0, AttackManager::playQuickSheathSoundAction)
                                    .build().andThen(FallHandler::fallDecrease))
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> AERIAL_CLEAVE = COMBO_STATE.register("aerial_cleave",
            ComboState.Builder.newInstance().startAndEnd(1800, 1812).priority(70).aerial()
                    .next(entity -> SlashBlade.prefix("aerial_cleave"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_cleave_loop")).clickAction((e) -> {
                        Vec3 motion = e.getDeltaMovement();
                        e.setDeltaMovement(motion.x, 0.1, motion.z);

                        AdvancementHelper.grantCriterion(e, AdvancementHelper.ADVANCEMENT_AERIAL_CLEAVE);
                    }).addTickAction((e) -> {
                        e.fallDistance = 1;

                        long elapsed = ComboState.getElapsed(e);

                        if (elapsed == 2) {
                            e.level().playSound((Player) null, e.getX(), e.getY(), e.getZ(),
                                    SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.75F, 1.0F);
                        }

                        if (2 < elapsed) {
                            Vec3 motion = e.getDeltaMovement();
                            e.setDeltaMovement(motion.x, motion.y - 3.0, motion.z);
                        }

                        if (elapsed % 2 == 0)
                            AttackManager.areaAttack(e, KnockBacks.meteor.action, 0.22f, true, false, true);

                        if (e.onGround()) {
                            AttackManager.doSlash(e, 55, Vec3.ZERO, true, true, 0.22f, KnockBacks.meteor);
                            e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                                state.updateComboSeq(e, SlashBlade.prefix("aerial_cleave_landing"));
                                FallHandler.spawnLandingParticle(e, 20);
                            });
                        }

                        if (elapsed == 1) {
                            e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                                if (state.getComboSeq() == SlashBlade.prefix("aerial_cleave")) {
                                    state.updateComboSeq(e, SlashBlade.prefix("aerial_cleave_loop"));
                                }
                            });
                        }
                    })
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 90, true))
                            .put(4, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())::build);
    public static final RegistryObject<ComboState> AERIAL_CLEAVE_LOOP = COMBO_STATE.register("aerial_cleave_loop",
            ComboState.Builder.newInstance().startAndEnd(1812, 1817).priority(70).loop().timeout(1000)
                    .next(entity -> SlashBlade.prefix("aerial_cleave_loop"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none")).addTickAction((e) -> {
                        e.fallDistance = 1;

                        Vec3 motion = e.getDeltaMovement();
                        e.setDeltaMovement(motion.x, motion.y - 3.0, motion.z);

                        long elapsed = ComboState.getElapsed(e);

                        if (elapsed % 2 == 0)
                            AttackManager.areaAttack(e, KnockBacks.meteor.action, 0.22f, true, false, true);

                        if (e.onGround()) {
                            AttackManager.doSlash(e, 55, Vec3.ZERO, true, true, 0.22f, KnockBacks.meteor);
                            e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                                state.updateComboSeq(e, SlashBlade.prefix("aerial_cleave_landing"));
                                FallHandler.spawnLandingParticle(e, 20);
                            });
                        }
                    }).addHitEffect((t, a) -> StunManager.setStun(t, 15))::build);

    public static final RegistryObject<ComboState> AERIAL_CLEAVE_LANDING = COMBO_STATE.register("aerial_cleave_landing",
            ComboState.Builder.newInstance().startAndEnd(1816, 1859).priority(70)
                    .next(ComboState.TimeoutNext.buildFromFrame(6, (a) -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("aerial_cleave_end"))
                    .clickAction((entityIn) -> AttackManager.doSlash(entityIn, 60, Vec3.ZERO, false, false, 0.22f,
                            KnockBacks.meteor))
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))::build);

    public static final RegistryObject<ComboState> AERIAL_CLEAVE_END = COMBO_STATE.register("aerial_cleave_end",
            ComboState.Builder.newInstance().startAndEnd(1859, 1886).priority(70)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> RAPID_SLASH = COMBO_STATE.register("rapid_slash",
            ComboState.Builder.newInstance().startAndEnd(2000, 2019).priority(70)
                    .next((a) -> (a.hasEffect(MobEffects.DAMAGE_BOOST) || a.hasEffect(MobEffects.HUNGER))
                            ? SlashBlade.prefix("rapid_slash_quick")
                            : SlashBlade.prefix("rapid_slash"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("rapid_slash_end"))
                    .clickAction((entityIn) -> AdvancementHelper.grantCriterion(entityIn,
                            AdvancementHelper.ADVANCEMENT_RAPID_SLASH))
                    .addHoldAction((e) -> {
                        int elapsed = e.getTicksUsingItem();

                        if (elapsed < 2)
                            return;

                        //疾走居合减少3的攻击距离
                        AttributeModifier rsr = new AttributeModifier("RapidSlashReach", -3,
                                AttributeModifier.Operation.ADDITION);
                        AttributeInstance mai = e.getAttribute(ForgeMod.ENTITY_REACH.get());
                        mai.addTransientModifier(rsr);
                        AttackManager.areaAttack(e, (t) -> {
                            boolean isRightDown = e.getCapability(CapabilityInputState.INPUT_STATE)
                                    .map((state) -> state.getCommands().contains(InputCommand.R_DOWN)).orElse(false);

                            if (isRightDown) {
                                e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                                    if (state.getComboSeq().equals(SlashBlade.prefix("rapid_slash"))) {
                                        List<Entity> hits = AttackManager.areaAttack(e, KnockBacks.toss.action, 0.22f,
                                                true, true, true);

                                        if (!hits.isEmpty()) {
                                            state.updateComboSeq(e, SlashBlade.prefix("rising_star"));
                                            AdvancementHelper.grantCriterion(e,
                                                    AdvancementHelper.ADVANCEMENT_RISING_STAR);
                                        }
                                    }
                                });
                            }
                        }, 0.22f, true, false, true);
                        mai.removeModifier(rsr);
                    }).addTickAction((e) -> {
                        long elapsed = ComboState.getElapsed(e);

                        if (elapsed == 0) {
                            e.level().playSound((Player) null, e.getX(), e.getY(), e.getZ(),
                                    SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }

                        if (elapsed <= 3 && e.onGround())
                            e.moveRelative(e.isInWater() ? 0.35f : 0.8f, new Vec3(0, 0, 1));

                        if (2 <= elapsed && elapsed < 6) {
                            float roll = -45 + 90 * e.getRandom().nextFloat();

                            if (elapsed % 2 == 0)
                                roll += 180;

                            boolean critical = e.hasEffect(MobEffects.DAMAGE_BOOST);

                            AttackManager.doSlash(e, roll, AttackManager.genRushOffset(e), false, critical,
                                    0.22f);
                        }

                        if (elapsed == 7) {
                            AttackManager.doSlash(e, -30, AttackManager.genRushOffset(e), false, true, 0.22f);
                        }

                        if (7 <= elapsed && elapsed <= 10) {
                            UserPoseOverrider.setRot(e, 90, true);
                        }
                        if (10 < elapsed) {
                            UserPoseOverrider.setRot(e, 0, false);
                        }
                    }).addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> RAPID_SLASH_QUICK = COMBO_STATE.register("rapid_slash_quick",
            ComboState.Builder.newInstance().startAndEnd(2000, 2001).priority(70)
                    .next(entity -> SlashBlade.prefix("rapid_slash_quick"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("rapid_slash"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> RAPID_SLASH_END = COMBO_STATE.register("rapid_slash_end",
            ComboState.Builder.newInstance().startAndEnd(2019, 2054).priority(70)
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("rapid_slash_end2"))::build);

    public static final RegistryObject<ComboState> RAPID_SLASH_END2 = COMBO_STATE.register("rapid_slash_end2",
            ComboState.Builder.newInstance().startAndEnd(2054, 2073).priority(70)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> RISING_STAR = COMBO_STATE.register("rising_star",
            ComboState.Builder.newInstance().startAndEnd(2100, 2137).priority(80).aerial()
                    .next(ComboState.TimeoutNext.buildFromFrame(18, (a) -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("rising_star_end")).clickAction((entityIn) -> {
                        entityIn.setDeltaMovement(0, 0.6, 0);
                        entityIn.setOnGround(false);
                        entityIn.hasImpulse = true;
                        AttackManager.doSlash(entityIn, -57, Vec3.ZERO, false, false, 0.3f, KnockBacks.toss);
                    })
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put((int) TimeValueHelper.getTicksFromFrames(9),
                                    (entityIn) -> AttackManager.doSlash(entityIn, -57, Vec3.ZERO, false, false, 0.3f,
                                            KnockBacks.toss))
                            .build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(0 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(0 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(0 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(0 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(5 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(5 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(5 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(5 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(5 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(5 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addTickAction((entityIn) -> {

                        long elapsed = ComboState.getElapsed(entityIn);

                        if (elapsed < 3) {
                            Vec3 motion = entityIn.getDeltaMovement();

                            double yMotion = motion.y;
                            if (yMotion <= 0) {
                                yMotion = 0.6;

                                entityIn.setOnGround(false);
                                entityIn.hasImpulse = true;
                            }

                            entityIn.setDeltaMovement(0, yMotion, 0);
                        }
                    }).addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> RISING_STAR_END = COMBO_STATE.register("rising_star_end",
            ComboState.Builder.newInstance().startAndEnd(2137, 2147).priority(70)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT = COMBO_STATE.register("judgement_cut",
            ComboState.Builder.newInstance().startAndEnd(1900, 1923).priority(50)
                    .next(entity -> SlashBlade.prefix("judgement_cut"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("judgement_cut_slash")).addTickAction((e) -> {

                        long elapsed = ComboState.getElapsed(e);

                        if (elapsed == 0) {
                            e.playSound(SoundEvents.TRIDENT_THROW, 0.80F, 0.625F + 0.1f * e.getRandom().nextFloat());
                            AdvancementHelper.grantCriterion(e, AdvancementHelper.ADVANCEMENT_JUDGEMENT_CUT);
                        }

                        if (elapsed <= 3) {
                            e.moveRelative(-0.3f, new Vec3(0, 0, 1));
                            Vec3 vec = e.getDeltaMovement();
                            {
                                double d0 = vec.x;
                                double d1 = vec.z;

                                while (d0 != 0.0D && e.level().noCollision(e,
                                        e.getBoundingBox().move(d0, (double) (-e.maxUpStep()), 0.0D))) {
                                    if (d0 < 0.05D && d0 >= -0.05D) {
                                        d0 = 0.0D;
                                    } else if (d0 > 0.0D) {
                                        d0 -= 0.05D;
                                    } else {
                                        d0 += 0.05D;
                                    }
                                }

                                while (d1 != 0.0D && e.level().noCollision(e,
                                        e.getBoundingBox().move(0.0D, (double) (-e.maxUpStep()), d1))) {
                                    if (d1 < 0.05D && d1 >= -0.05D) {
                                        d1 = 0.0D;
                                    } else if (d1 > 0.0D) {
                                        d1 -= 0.05D;
                                    } else {
                                        d1 += 0.05D;
                                    }
                                }

                                while (d0 != 0.0D && d1 != 0.0D && e.level().noCollision(e,
                                        e.getBoundingBox().move(d0, (double) (-e.maxUpStep()), d1))) {
                                    if (d0 < 0.05D && d0 >= -0.05D) {
                                        d0 = 0.0D;
                                    } else if (d0 > 0.0D) {
                                        d0 -= 0.05D;
                                    } else {
                                        d0 += 0.05D;
                                    }

                                    if (d1 < 0.05D && d1 >= -0.05D) {
                                        d1 = 0.0D;
                                    } else if (d1 > 0.0D) {
                                        d1 -= 0.05D;
                                    } else {
                                        d1 += 0.05D;
                                    }
                                }

                                vec = new Vec3(d0, vec.y, d1);
                            }

                            e.move(MoverType.SELF, vec);
                        }
                        e.setDeltaMovement(e.getDeltaMovement().multiply(0, 1, 0));
                    }).addTickAction(FallHandler::fallDecrease)
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SLASH = COMBO_STATE.register("judgement_cut_slash",
            ComboState.Builder.newInstance().startAndEnd(1923, 1928).speed(0.4F).priority(50)
                    .next(entity -> SlashBlade.prefix("judgement_cut_slash"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("judgement_cut_sheath"))
                    .addTickAction(
                            ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCut).build())
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SHEATH = COMBO_STATE.register("judgement_cut_sheath",
            ComboState.Builder.newInstance().startAndEnd(1928, 1963).priority(50)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SLASH_AIR = COMBO_STATE.register(
            "judgement_cut_slash_air",
            ComboState.Builder.newInstance().startAndEnd(1923, 1928).speed(0.5F).priority(50)
                    .next(entity -> SlashBlade.prefix("judgement_cut_slash_air"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("judgement_cut_sheath_air"))
                    .addTickAction(
                            ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCut).build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,
                            a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_JUDGEMENT_CUT))
                            .build())
                    .addTickAction(FallHandler::fallResist)
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SHEATH_AIR = COMBO_STATE.register(
            "judgement_cut_sheath_air",
            ComboState.Builder.newInstance().startAndEnd(1928, 1963).priority(50)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SLASH_JUST = COMBO_STATE.register(
            "judgement_cut_slash_just",
            ComboState.Builder.newInstance().startAndEnd(1923, 1928).priority(45)
                    .next(entity -> SlashBlade.prefix("judgement_cut_slash_just"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("judgement_cut_slash_just2"))
                    .addTickAction(
                            ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCutJust).build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,
                            a -> AdvancementHelper.grantCriterion(a, AdvancementHelper.ADVANCEMENT_JUDGEMENT_CUT_JUST))
                            .build())
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addTickAction(FallHandler::fallResist).addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SLASH_JUST2 = COMBO_STATE.register(
            "judgement_cut_slash_just2",
            ComboState.Builder.newInstance().startAndEnd(1923, 1928).speed(0.75F).priority(50)
                    .next(entity -> SlashBlade.prefix("judgement_cut_slash_just2"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("judgement_cut_slash_just_sheath"))
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addTickAction(FallHandler::fallResist)::build);

    public static final RegistryObject<ComboState> JUDGEMENT_CUT_SHEATH_JUST = COMBO_STATE.register(
            "judgement_cut_slash_just_sheath",
            ComboState.Builder.newInstance().startAndEnd(1928, 1963).priority(50)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> VOID_SLASH = COMBO_STATE.register("void_slash", ComboState.Builder
            .newInstance().startAndEnd(2200, 2277).priority(50).speed(1.0F)
            .next(entity -> SlashBlade.prefix("void_slash"))
            .nextOfTimeout(entity -> SlashBlade.prefix("void_slash_sheath"))
            .addTickAction(entity -> entity.setDeltaMovement(Vec3.ZERO))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(16, AttackManager::doVoidSlashAttack).build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(16 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(16 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(16 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(16 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(16 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(16 + 5, (entityIn) -> UserPoseOverrider.setRot(entityIn, 0, true))
                    .put(57 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 5, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 6, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 7, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 8, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 9, (entityIn) -> UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(57 + 10, (entityIn) -> UserPoseOverrider.setRot(entityIn, 0, true)).build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect((t, a) -> StunManager.setStun(t, 40))::build);

    public static final RegistryObject<ComboState> VOID_SLASH_SHEATH = COMBO_STATE.register("void_slash_sheath",
            ComboState.Builder.newInstance().startAndEnd(2278, 2299).priority(50)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> SAKURA_END_LEFT = COMBO_STATE.register("sakura_end_left",
            ComboState.Builder.newInstance().startAndEnd(1816, 1859).speed(6F).priority(50)
                    .next((entity) -> SlashBlade.prefix("sakura_end_right"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("sakura_end_right"))
                    .clickAction((entityIn) -> SakuraEnd.doSlash(entityIn, 22.5F, Vec3.ZERO, false, false, 0.5))
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> SAKURA_END_RIGHT = COMBO_STATE.register("sakura_end_right",
            ComboState.Builder.newInstance().startAndEnd(204, 218).speed(1.1F).priority(50)
                    .next((entity) -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("sakura_end_finish"))
                    .clickAction((entityIn) -> SakuraEnd.doSlash(entityIn, 180F - 22.5F, Vec3.ZERO, false, true, 0.76))
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addHitEffect((t, a) -> StunManager.setStun(t, 36))::build);

    public static final RegistryObject<ComboState> SAKURA_END_FINISH = COMBO_STATE.register("sakura_end_finish",
            ComboState.Builder.newInstance().startAndEnd(218, 281).priority(50).aerial()
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("sakura_end_finish2"))::build);
    public static final RegistryObject<ComboState> SAKURA_END_FINISH2 = COMBO_STATE.register("sakura_end_finish2",
            ComboState.Builder.newInstance().startAndEnd(281, 314).priority(80)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> SAKURA_END_LEFT_AIR = COMBO_STATE.register("sakura_end_left_air",
            ComboState.Builder.newInstance().startAndEnd(1300, 1328).speed(3.2F).priority(50)
                    .next((entity) -> SlashBlade.prefix("sakura_end_right_air"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("sakura_end_right_air"))
                    .clickAction((entityIn) -> SakuraEnd.doSlash(entityIn, 22.5F, Vec3.ZERO, false, false, 0.5))
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun).aerial()::build);

    public static final RegistryObject<ComboState> SAKURA_END_RIGHT_AIR = COMBO_STATE.register("sakura_end_right_air",
            ComboState.Builder.newInstance().startAndEnd(1200, 1210).priority(50)
                    .next((entity) -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("sakura_end_finish_air"))
                    .clickAction((entityIn) -> SakuraEnd.doSlash(entityIn, 180F - 22.5F, Vec3.ZERO, false, true, 0.76))
                    .addTickAction((entityIn) -> UserPoseOverrider.resetRot(entityIn))
                    .addTickAction(FallHandler::fallDecrease).addHitEffect(StunManager::setStun).aerial()::build);

    public static final RegistryObject<ComboState> SAKURA_END_FINISH_AIR = COMBO_STATE.register("sakura_end_finish_air",
            ComboState.Builder.newInstance().startAndEnd(1210, 1231).priority(50).aerial()
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("sakura_end_finish2_air"))
                    .addTickAction(FallHandler::fallDecrease)::build);
    public static final RegistryObject<ComboState> SAKURA_END_FINISH2_AIR = COMBO_STATE.register(
            "sakura_end_finish2_air",
            ComboState.Builder.newInstance().startAndEnd(1231, 1241).priority(50)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(FallHandler::fallDecrease)
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)
                    .addTickAction(FallHandler::fallDecrease)::build);

    public static final RegistryObject<ComboState> CIRCLE_SLASH = COMBO_STATE.register("circle_slash",
            ComboState.Builder.newInstance().startAndEnd(725, 743).priority(50)
                    .next(entity -> SlashBlade.prefix("circle_slash"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("circle_slash_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(4, (entityIn) -> CircleSlash.doCircleSlashAttack(entityIn, 180))
                            .put(5, (entityIn) -> CircleSlash.doCircleSlashAttack(entityIn, 90))
                            .put(6, (entityIn) -> CircleSlash.doCircleSlashAttack(entityIn, 0))
                            .put(7, (entityIn) -> CircleSlash.doCircleSlashAttack(entityIn, -90)).build())
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(7 - 3 + 0, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(7 - 3 + 1, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(7 - 3 + 2, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(7 - 3 + 3, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(7 - 3 + 4, (entityIn) -> UserPoseOverrider.setRot(entityIn, 72, true))
                            .put(7 - 3 + 5, (entityIn) -> UserPoseOverrider.resetRot(entityIn)).build())
                    .addHitEffect(StunManager::setStun)::build);
    public static final RegistryObject<ComboState> CIRCLE_SLASH_END = COMBO_STATE.register("circle_slash_end",
            ComboState.Builder.newInstance().startAndEnd(743, 764).priority(100)
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("circle_slash_end2"))::build);
    public static final RegistryObject<ComboState> CIRCLE_SLASH_END2 = COMBO_STATE.register("circle_slash_end2",
            ComboState.Builder.newInstance().startAndEnd(764, 787).priority(100)
                    .next(entity -> SlashBlade.prefix("none")).nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())::build);

    public static final RegistryObject<ComboState> DRIVE_HORIZONTAL = COMBO_STATE.register("drive_horizontal",
            ComboState.Builder.newInstance().startAndEnd(400, 459).priority(50)
                    .motionLoc(DefaultResources.ExMotionLocation)
                    .next(ComboState.TimeoutNext.buildFromFrame(15, entity -> SlashBlade.prefix("none")))
                    .nextOfTimeout(entity -> SlashBlade.prefix("drive_horizontal_end"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -30F, Vec3.ZERO, false, false, 0.21F))
                            .put(3, (entityIn) -> Drive.doSlash(entityIn, 0F, 10, Vec3.ZERO, false, 1.5f, 2f)).build())
                    .addHitEffect(StunManager::setStun)
                    ::build);
    public static final RegistryObject<ComboState> DRIVE_HORIZONTAL_END = COMBO_STATE.register("drive_horizontal_end",
            ComboState.Builder.newInstance().startAndEnd(459, 488).priority(50)
                    .motionLoc(DefaultResources.ExMotionLocation).next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                            .put(0, AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);

    public static final RegistryObject<ComboState> DRIVE_VERTICAL = COMBO_STATE.register("drive_vertical", 
        ComboState.Builder.newInstance()
            .startAndEnd(1600, 1659)
            .priority(50)
            .motionLoc(DefaultResources.ExMotionLocation)
            .next(ComboState.TimeoutNext.buildFromFrame(15, entity -> SlashBlade.prefix("none")))
            .nextOfTimeout(entity -> SlashBlade.prefix("drive_vertical_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -80F, Vec3.ZERO, false, false, 0.21F))
                    .put(3, (entityIn) -> Drive.doSlash(entityIn, -90F, 10, Vec3.ZERO, false, 1.5f, 2f)).build())
            .addHitEffect(StunManager::setStun)
            ::build
    );
    public static final RegistryObject<ComboState> DRIVE_VERTICALL_END = COMBO_STATE.register("drive_vertical_end", 
        ComboState.Builder.newInstance()
            .startAndEnd(1659, 1693)
            .priority(50)
            .motionLoc(DefaultResources.ExMotionLocation)
            .next(entity -> SlashBlade.prefix("none"))
            .nextOfTimeout(entity -> SlashBlade.prefix("none"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,AttackManager::playQuickSheathSoundAction).build())
            .releaseAction(ComboState::releaseActionQuickCharge)
            ::build
    );

    public static final RegistryObject<ComboState> WAVE_EDGE_VERTICAL = COMBO_STATE.register("wave_edge_vertical",
        ComboState.Builder.newInstance()
            .startAndEnd(1600, 1659)
            .priority(50)
            .motionLoc(DefaultResources.ExMotionLocation)
            .next(ComboState.TimeoutNext.buildFromFrame(15, entity -> SlashBlade.prefix("none")))
            .nextOfTimeout(entity -> SlashBlade.prefix("drive_vertical_end"))
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                     .put(2, (entityIn) -> AttackManager.doSlash(entityIn, -80F, Vec3.ZERO, false, false, 0.21F))
                     .put(3, (entityIn) -> WaveEdge.doSlash(entityIn, 90F, 20, Vec3.ZERO, false, 0.4F, 0.2f, 1f, 4)).build())
            .addHitEffect(StunManager::setStun)
            ::build
    );
    
    public static final RegistryObject<ComboState> JUDGEMENT_CUT_END = COMBO_STATE.register
    (
        "judgement_cut_end",
        ComboState.Builder.newInstance()
        .startAndEnd(1923, 1928)
        .priority(50)
        .next(livingEntity -> SlashBlade.prefix("judgement_cut_end"))
        .nextOfTimeout(livingEntity -> SlashBlade.prefix("judgement_cut_sheath"))
        .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCutSuper).build())
        .addTickAction(FallHandler::fallDecrease)
        .addHitEffect(StunManager::setStun)
        ::build
    );
    
    public static final RegistryObject<ComboState> PIERCING = COMBO_STATE.register("piercing", ComboState.Builder
            .newInstance().startAndEnd(1, 31).priority(50).motionLoc(DefaultResources.testLocation)
            .next(entity -> SlashBlade.prefix("piercing"))
            .nextOfTimeout(entity -> SlashBlade.prefix("piercing_2"))
            .addTickAction(entity -> UserPoseOverrider.resetRot(entity))
            ::build);
    
    public static final RegistryObject<ComboState> PIERCING_2 = COMBO_STATE.register("piercing_2", ComboState.Builder
            .newInstance().startAndEnd(31, 55).priority(50).motionLoc(DefaultResources.testLocation)
            .next(ComboState.TimeoutNext.buildFromFrame(10, entity -> SlashBlade.prefix("none")))
            .nextOfTimeout(entity -> SlashBlade.prefix("piercing_end"))
            .addTickAction((entity) -> {

                long elapsed = ComboState.getElapsed(entity);

                if (elapsed < 3) {
                    if (entity.onGround())
                    	entity.moveRelative(entity.isInWater() ? 0.35f : 0.8f, new Vec3(0, 0, 1));
                    AttackManager.areaAttack(entity, KnockBacks.toss.action, 1.01f, true, false, true);
                } 
                if(elapsed == 1)
                	AttackManager.playPiercingSoundAction(entity);
            })
            .addTickAction(entity -> UserPoseOverrider.resetRot(entity))
            .addHitEffect(StunManager::setStun)::build);
    
    public static final RegistryObject<ComboState> PIERCING_JUST = COMBO_STATE.register("piercing_just", ComboState.Builder
            .newInstance().startAndEnd(32, 55).priority(50).motionLoc(DefaultResources.testLocation)
            .next(ComboState.TimeoutNext.buildFromFrame(10, entity -> SlashBlade.prefix("none")))
            .nextOfTimeout(entity -> SlashBlade.prefix("piercing_end"))
            .addTickAction((entity) -> {

                long elapsed = ComboState.getElapsed(entity);

                if (elapsed < 3) {
                    if (entity.onGround())
                        entity.moveRelative(entity.isInWater() ? 0.35f : 0.8f, new Vec3(0, 0, 1));
                    AttackManager.areaAttack(entity, KnockBacks.toss.action, 1.01f, true, false, true);
                } 
                if(elapsed == 1)
                	AttackManager.playPiercingSoundAction(entity);
            })
            .addTickAction(entity -> UserPoseOverrider.resetRot(entity))
            .addHitEffect(StunManager::setStun)::build);

    public static final RegistryObject<ComboState> PIERCING_END = COMBO_STATE.register("piercing_end",
            ComboState.Builder.newInstance().startAndEnd(55, 65).priority(50)
                    .motionLoc(DefaultResources.testLocation)
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("piercing_end2"))
                    ::build);
    
    public static final RegistryObject<ComboState> PIERCING_END2 = COMBO_STATE.register("piercing_end2",
            ComboState.Builder.newInstance().startAndEnd(65, 92).priority(50)
                    .motionLoc(DefaultResources.testLocation)
                    .next(entity -> SlashBlade.prefix("none"))
                    .nextOfTimeout(entity -> SlashBlade.prefix("none"))
                    .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,AttackManager::playQuickSheathSoundAction).build())
                    .releaseAction(ComboState::releaseActionQuickCharge)::build);
}
