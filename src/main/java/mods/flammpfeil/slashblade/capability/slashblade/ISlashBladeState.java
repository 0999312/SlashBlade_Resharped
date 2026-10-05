package mods.flammpfeil.slashblade.capability.slashblade;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.event.BladeMotionEvent;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeCriteriaTriggerRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.NBTHelper;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

public interface ISlashBladeState {
    
    private static Holder<Enchantment> soulSpeedHolder(LivingEntity user) {
        return user.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SOUL_SPEED);
    }
    
    default CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        // action state
        tag.putLong("lastActionTime", this.getLastActionTime());
        tag.putInt("TargetEntity", this.getTargetEntityId());
        tag.putBoolean("_onClick", this.onClick());
        tag.putFloat("fallDecreaseRate", this.getFallDecreaseRate());
        tag.putFloat("AttackAmplifier", this.getAttackAmplifier());
        tag.putString("currentCombo", this.getComboSeq().toString());
        tag.putInt("Damage", this.getDamage());
        tag.putInt("maxDamage", this.getMaxDamage());
        tag.putInt("proudSoul", this.getProudSoulCount());
        tag.putBoolean("isBroken", this.isBroken());
        
        // passive state
        tag.putBoolean("isSealed", this.isSealed());
        
        tag.putFloat("baseAttackModifier", this.getBaseAttackModifier());
        
        tag.putInt("killCount", this.getKillCount());
        tag.putInt("RepairCounter", this.getRefine());
        
        // performance setting
        
        tag.putString("SpecialAttackType", Optional.of(this.getSlashArtsKey()).orElse(SlashArtsRegistry.JUDGEMENT_CUT.getId()).toString());
        tag.putBoolean("isDefaultBewitched", this.isDefaultBewitched());
        tag.putBoolean("destructable", this.isDestructable());
        tag.putString("translationKey", this.getTranslationKey());
        
        // render info
        tag.putByte("StandbyRenderType", (byte) this.getCarryType().ordinal());
        tag.putInt("SummonedSwordColor", this.getColorCode());
        tag.putBoolean("SummonedSwordColorInverse", this.isEffectColorInverse());
        tag.put("adjustXYZ", NBTHelper.newDoubleNBTList(this.getAdjust()));
        
        this.getTexture().ifPresent(loc -> tag.putString("TextureName", loc.toString()));
        this.getModel().ifPresent(loc -> tag.putString("ModelName", loc.toString()));
        
        tag.putString("ComboRoot", Optional.of(this.getComboRoot()).orElse(ComboStateRegistry.STANDBY.getId()).toString());
        
        if (!this.getSpecialEffects().isEmpty()) {
            ListTag seList = new ListTag();
            this.getSpecialEffects().forEach(se -> seList.add(StringTag.valueOf(se.toString())));
            tag.put("SpecialEffects", seList);
        }
        
        return tag;
    }
    
    default void deserializeNBT(@Nullable CompoundTag tag) {
        if (tag == null) {
            return;
        }
        this.setNonEmpty();
        // action state
        this.setLastActionTime(tag.getLong("lastActionTime"));
        this.setTargetEntityId(tag.getInt("TargetEntity"));
        this.setOnClick(tag.getBoolean("_onClick"));
        this.setFallDecreaseRate(tag.getFloat("fallDecreaseRate"));
        this.setAttackAmplifier(tag.getFloat("AttackAmplifier"));
        this.setComboSeq(ResourceLocation.parse(tag.getString("currentCombo")));
        this.setDamage(tag.getInt("Damage"));
        this.setMaxDamage(tag.getInt("maxDamage"));
        this.setProudSoulCount(tag.getInt("proudSoul"));
        this.setBroken(tag.getBoolean("isBroken"));
        
        // passive state
        this.setSealed(tag.getBoolean("isSealed"));
        
        this.setBaseAttackModifier(tag.getFloat("baseAttackModifier"));
        
        this.setKillCount(tag.getInt("killCount"));
        this.setRefine(tag.getInt("RepairCounter"));
        
        // performance setting
        
        this.setSlashArtsKey(ResourceLocation.parse(tag.getString("SpecialAttackType")));
        this.setDefaultBewitched(tag.getBoolean("isDefaultBewitched"));
        this.setDestructable(tag.getBoolean("destructable"));
        
        this.setTranslationKey(tag.getString("translationKey"));
        
        // render info
        this.setCarryType(
            EnumSetConverter.fromOrdinal(CarryType.values(), tag.getByte("StandbyRenderType"), CarryType.PSO2));
        this.setColorCode(tag.getInt("SummonedSwordColor"));
        this.setEffectColorInverse(tag.getBoolean("SummonedSwordColorInverse"));
        this.setAdjust(NBTHelper.getVector3d(tag, "adjustXYZ"));
        
        if (tag.contains("TextureName")) {
            this.setTexture(ResourceLocation.tryParse(tag.getString("TextureName")));
        } else {
            this.setTexture(null);
        }
        
        if (tag.contains("ModelName")) {
            this.setModel(ResourceLocation.tryParse(tag.getString("ModelName")));
        } else {
            this.setModel(null);
        }
        
        this.setComboRoot(ResourceLocation.parse(tag.getString("ComboRoot")));
        if (tag.contains("SpecialEffects")) {
            ListTag list = tag.getList("SpecialEffects", 8);
            this.setSpecialEffects(list);
        }
    }
    
    long getLastActionTime();
    
    void setLastActionTime(long lastActionTime);
    
    default long getElapsedTime(LivingEntity user) {
        long ticks = (Math.max(0, user.level().getGameTime() - this.getLastActionTime()));
        
        if (user.level().isClientSide()) {
            ticks = Math.max(0, ticks + 1);
        }
        
        return ticks;
    }
    
    boolean onClick();
    
    void setOnClick(boolean onClick);
    
    float getFallDecreaseRate();
    
    void setFallDecreaseRate(float fallDecreaseRate);
    
    float getAttackAmplifier();
    
    void setAttackAmplifier(float attackAmplifier);
    
    ResourceLocation getComboSeq();
    
    void setComboSeq(ResourceLocation comboSeq);
    
    boolean isBroken();
    
    void setBroken(boolean broken);
    
    boolean isSealed();
    
    void setSealed(boolean sealed);
    
    float getBaseAttackModifier();
    
    void setBaseAttackModifier(float baseAttackModifier);
    
    int getProudSoulCount();
    
    void setProudSoulCount(int psCount);
    
    int getKillCount();
    
    void setKillCount(int killCount);
    
    int getRefine();
    
    void setRefine(int refine);
    
    default SlashArts getSlashArts() {
        ResourceLocation key = getSlashArtsKey();
        SlashArts result = SlashArtsRegistry.REGISTRY.containsKey(key) ? SlashArtsRegistry.REGISTRY.get(key)
            : SlashArtsRegistry.JUDGEMENT_CUT.get();
        
        if (SlashArtsRegistry.NONE.getId().equals(key)) {
            result = null;
        }
        
        return result != null ? result : SlashArtsRegistry.JUDGEMENT_CUT.get();
    }
    
    void setSlashArtsKey(@Nullable ResourceLocation slashArts);
    
    ResourceLocation getSlashArtsKey();
    
    boolean isDefaultBewitched();
    
    void setDefaultBewitched(boolean defaultBewitched);
    
    boolean isDestructable();
    
    void setDestructable(boolean destructable);
    
    List<SwordType> getDefaultSwordTypes();
    
    void setDefaultSwordTypes(List<SwordType> defaultSwordTypes);
    
    String getTranslationKey();
    
    void setTranslationKey(String translationKey);
    
    CarryType getCarryType();
    
    void setCarryType(CarryType carryType);
    
    Color getEffectColor();
    
    void setEffectColor(Color effectColor);
    
    boolean isEffectColorInverse();
    
    void setEffectColorInverse(boolean effectColorInverse);
    
    default void setColorCode(int colorCode) {
        setEffectColor(new Color(colorCode));
    }
    
    default int getColorCode() {
        return getEffectColor().getRGB();
    }
    
    Vec3 getAdjust();
    
    void setAdjust(Vec3 adjust);
    
    Optional<ResourceLocation> getTexture();
    
    void setTexture(@Nullable ResourceLocation texture);
    
    Optional<ResourceLocation> getModel();
    
    void setModel(@Nullable ResourceLocation model);
    
    int getTargetEntityId();
    
    void setTargetEntityId(int id);
    
    @Deprecated
    long getLastProcessedComboTick();
    
    @Deprecated
    void setLastProcessedComboTick(long tick);
    
    @Nullable
    default Entity getTargetEntity(Level world) {
        int id = getTargetEntityId();
        if (id < 0) {
            return null;
        } else {
            return world.getEntity(id);
        }
    }
    
    default void setTargetEntityId(@Nullable Entity target) {
        if (target != null) {
            this.setTargetEntityId(target.getId());
        } else {
            this.setTargetEntityId(-1);
        }
    }
    
    default int getFullChargeTicks(LivingEntity user) {
        return SlashArts.ChargeTicks;
    }
    
    default boolean isCharged(LivingEntity user) {
        if (!(SwordType.from(user.getMainHandItem()).contains(SwordType.ENCHANTED))) {
            return false;
        }
        if (this.isBroken() || this.isSealed()) {
            return false;
        }
        int elapsed = user.getTicksUsingItem();
        return getFullChargeTicks(user) < elapsed;
    }
    
    @Nullable
    default ResourceLocation progressCombo(LivingEntity user, boolean isVirtual) {
        ResourceLocation currentloc = resolvCurrentComboState(user);
        ComboState current = ComboStateRegistry.REGISTRY.get(currentloc);
        
        if (current == null) {
            return ComboStateRegistry.NONE.getId();
        }
        
        var event = new SlashBladeEvent.NextComboEvent(user.getMainHandItem(), this, user, current.getNext(user));
        NeoForge.EVENT_BUS.post(event);
        ResourceLocation next = event.getNextCombo();
        if (!next.equals(ComboStateRegistry.NONE.getId()) && next.equals(currentloc)) {
            return ComboStateRegistry.NONE.getId();
        }
        
        ComboState comboRoot = ComboStateRegistry.REGISTRY.get(getComboRoot());
        ResourceLocation rootNext = null;
        if (comboRoot != null) {
            rootNext = comboRoot.getNext(user);
        }
        ComboState nextCS = ComboStateRegistry.REGISTRY.get(next);
        ComboState rootNextCS = ComboStateRegistry.REGISTRY.get(rootNext);
        ResourceLocation resolved = null;
        if (rootNextCS != null) {
            if (nextCS != null) {
                resolved = nextCS.getPriority() <= rootNextCS.getPriority() ? next : rootNext;
            }
        }
        
        if (!isVirtual) {
            this.updateComboSeq(user, resolved);
        }
        
        return resolved;
    }
    
    @Nullable
    default ResourceLocation progressCombo(LivingEntity user) {
        return progressCombo(user, false);
    }
    
    @Nullable
    default ResourceLocation doChargeAction(LivingEntity user, int elapsed) {
        if (elapsed <= 2) {
            return ComboStateRegistry.NONE.getId();
        }
        
        if (this.isBroken() || this.isSealed()) {
            return ComboStateRegistry.NONE.getId();
        }
        
        Map.Entry<Integer, ResourceLocation> currentloc = resolvCurrentComboStateTicks(user);
        
        ComboState current = ComboStateRegistry.REGISTRY.get(currentloc.getValue());
        if (current == null) {
            return ComboStateRegistry.NONE.getId();
        }
        
        // Uninterrupted
        if (!ComboStateRegistry.NONE.getId().equals(currentloc.getValue())
            && Objects.equals(current.getNext(user), currentloc.getValue())) {
            return ComboStateRegistry.NONE.getId();
        }
        
        int fullChargeTicks = getFullChargeTicks(user);
        int justReceptionSpan = SlashArts.getJustReceptionSpan(user);
        int justChargePeriod = fullChargeTicks + justReceptionSpan;
        
        RangeMap<Integer, SlashArts.ArtsType> charge_accept = ImmutableRangeMap.<Integer, SlashArts.ArtsType>builder()
            .put(Range.lessThan(fullChargeTicks), SlashArts.ArtsType.Fail)
            .put(Range.closedOpen(fullChargeTicks, justChargePeriod), SlashArts.ArtsType.Jackpot)
            .put(Range.atLeast(justChargePeriod), SlashArts.ArtsType.Success).build();
        
        SlashArts.ArtsType type = charge_accept.get(elapsed);
        
        if (type != SlashArts.ArtsType.Jackpot) {
            // quick charge
            SlashArts.ArtsType result = current.releaseAction(user, currentloc.getKey());
            
            if (result != SlashArts.ArtsType.Fail) {
                type = result;
            }
        }
        
        ResourceLocation csloc = null;
        if (type != null) {
            csloc = this.getSlashArts().doArts(type, user);
        }
        
        SlashBladeEvent.PerformSlashArtEvent event = new SlashBladeEvent.PerformSlashArtEvent(user, elapsed, this, csloc,
            type);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return ComboStateRegistry.NONE.getId();
        }
        
        csloc = event.getComboState();
        ComboState cs = ComboStateRegistry.REGISTRY.get(csloc);
        
        if (!ComboStateRegistry.NONE.getId().equals(csloc) && !currentloc.getValue().equals(csloc)) {
            
            if (cs != null && current.getPriority() > cs.getPriority()) {
                if (type == SlashArts.ArtsType.Jackpot) {
                    AdvancementHelper.grantedIf(soulSpeedHolder(user).value(), user);
                }
                this.updateComboSeq(user, csloc);
            }
        }
        if (user instanceof ServerPlayer serverPlayer) {
            SlashBladeCriteriaTriggerRegistry.DO_SLASH_ARTS.get().trigger(serverPlayer, user.getMainHandItem(), elapsed, type);
        }
        return csloc;
    }
    
    default void updateComboSeq(LivingEntity entity, @Nullable ResourceLocation loc) {
        if (!applyComboSeq(entity, loc, entity.level().getGameTime())) {
            return;
        }
        ComboState cs = ComboStateRegistry.REGISTRY.get(this.getComboSeq());
        if (cs != null) {
            cs.clickAction(entity);
        }
    }
    
    default void synchronizeComboSeq(LivingEntity entity, ResourceLocation loc) {
        applyComboSeq(entity, loc, this.getLastActionTime());
    }
    
    default ResourceLocation resolvCurrentComboState(LivingEntity user) {
        if (!(user.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
            return ComboStateRegistry.NONE.getId();
        }
        return resolvCurrentComboStateTicks(user).getValue();
    }
    
    default Map.Entry<Integer, ResourceLocation> peekCurrentComboStateTicks(LivingEntity user) {
        return resolveCurrentComboStateTicks(user, false);
    }
    
    default Map.Entry<Integer, ResourceLocation> resolvCurrentComboStateTicks(LivingEntity user) {
        return resolveCurrentComboStateTicks(user, true);
    }
    
    private Map.Entry<Integer, ResourceLocation> resolveCurrentComboStateTicks(LivingEntity user, boolean updateState) {
        ResourceLocation current = ComboStateRegistry.REGISTRY.containsKey(getComboSeq()) ? getComboSeq()
            : ComboStateRegistry.NONE.getId();
        ComboState currentCS = ComboStateRegistry.REGISTRY.get(current) != null
            ? ComboStateRegistry.REGISTRY.get(current)
            : ComboStateRegistry.NONE.get();
        int time = (int) TimeValueHelper.getMSecFromTicks(getElapsedTime(user));
        
        if (currentCS != null) {
            while (!current.equals(ComboStateRegistry.NONE.getId()) && currentCS.getTimeoutMS() < time) {
                time -= currentCS.getTimeoutMS();
                
                if (updateState) {
                    var event = new SlashBladeEvent.NextOfTimeOutComboEvent(user.getMainHandItem(), this, user, currentCS.getNextOfTimeout(user));
                    NeoForge.EVENT_BUS.post(event);
                    current = event.getNextCombo();
                    this.updateComboSeq(user, current);
                } else {
                    current = currentCS.getNextOfTimeout(user);
                }
                currentCS = ComboStateRegistry.REGISTRY.get(current);
                if (currentCS == null) {
                    current = ComboStateRegistry.NONE.getId();
                    currentCS = ComboStateRegistry.NONE.get();
                }
            }
        }
        
        int ticks = (int) TimeValueHelper.getTicksFromMSec(time);
        return new AbstractMap.SimpleImmutableEntry<>(ticks, current);
    }
    
    private boolean applyComboSeq(LivingEntity entity, @Nullable ResourceLocation loc, long actionTime) {
        if (loc == null || entity.level().isClientSide) {
            return false;
        }
        BladeMotionEvent event = new BladeMotionEvent(entity, loc, actionTime);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return false;
        }
        this.setComboSeq(event.getCombo());
        this.setLastActionTime(event.getActionTime());
        entity.getPersistentData().remove(ComboState.LAST_PROCESSED_TICK_KEY);
        if (entity instanceof ServerPlayer player) {
            SlashBladeCriteriaTriggerRegistry.COMBO_STATE.get().trigger(player, loc, player.getMainHandItem());
        }
        return true;
    }
    
    ResourceLocation getComboRoot();
    
    void setComboRoot(ResourceLocation resourceLocation);
    
    int getDamage();
    
    void setDamage(int damage);
    
    int getMaxDamage();
    
    void setMaxDamage(int damage);
    
    Collection<ResourceLocation> getSpecialEffects();
    
    void setSpecialEffects(ListTag list);
    
    boolean addSpecialEffect(ResourceLocation se);
    
    boolean removeSpecialEffect(ResourceLocation se);
    
    boolean hasSpecialEffect(ResourceLocation se);
    
    boolean isEmpty();
    
    void setNonEmpty();
}
