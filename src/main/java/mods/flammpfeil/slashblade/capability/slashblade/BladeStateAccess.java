package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDetune;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class BladeStateAccess {
    public static Optional<ISlashBladeState> of(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return Optional.empty();
        }
        ensureComponent(stack);
        return Optional.of(new ComponentBackedState(stack));
    }
    
    private static void ensureComponent(ItemStack stack) {
        if (stack.has(SlashBladeDataComponents.BLADE_STATE_DATA.get())) {
            return;
        }
        BladeStateData data = BladeStateData.DEFAULT;
        if (stack.getItem() instanceof ItemSlashBladeDetune detune) {
            data = new BladeStateData(
                "", detune.getBaseAttackModifier(), 0, 0, 0, false, false,
                BladeStateData.DEFAULT_SLASH_ARTS, false, Optional.empty(), detune.isDestructable(), BladeStateData.DEFAULT_COMBO_ROOT,
                CarryType.PSO2, 0xFF3333FF, false, Vec3.ZERO,
                Optional.of(detune.getTexture()),
                Optional.of(detune.getModel()),
                java.util.Collections.emptyList()
            );
        }
        stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), data);
    }
    
    public static Optional<BladeStateData> getData(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(stack.get(SlashBladeDataComponents.BLADE_STATE_DATA.get()));
    }
    
    public static BladeStateData getDataOrDefault(ItemStack stack) {
        return stack.getOrDefault(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT);
    }
    
    public static void setData(ItemStack stack, BladeStateData data) {
        stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), data);
    }
    
    public static void updateData(ItemStack stack, UnaryOperator<BladeStateData> updater) {
        stack.update(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT, updater);
    }
    
    public static void ensureRuntimeComponent(ItemStack stack) {
        if (!stack.has(SlashBladeDataComponents.BLADE_RUNTIME_STATE.get())) {
            stack.set(SlashBladeDataComponents.BLADE_RUNTIME_STATE.get(), BladeRuntimeStateData.DEFAULT);
        }
    }
    
    @SuppressWarnings("deprecation")
    static class ComponentBackedState implements ISlashBladeState {
        private final ItemStack stack;
        
        ComponentBackedState(ItemStack stack) {
            this.stack = stack;
        }
        
        private BladeStateData data() {
            return stack.getOrDefault(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT);
        }
        
        private BladeRuntimeStateData runtime() {
            ensureRuntimeComponent(stack);
            return stack.getOrDefault(SlashBladeDataComponents.BLADE_RUNTIME_STATE.get(), BladeRuntimeStateData.DEFAULT);
        }
        
        private void updateRuntime(java.util.function.UnaryOperator<BladeRuntimeStateData> updater) {
            stack.update(SlashBladeDataComponents.BLADE_RUNTIME_STATE.get(), BladeRuntimeStateData.DEFAULT, updater);
        }
        
        private void update(UnaryOperator<BladeStateData> updater) {
            stack.update(SlashBladeDataComponents.BLADE_STATE_DATA.get(), BladeStateData.DEFAULT, updater);
        }
        
        // ========== Persistent fields (BladeStateData) ==========
        
        @Override
        public String getTranslationKey() {
            return data().translationKey();
        }
        
        @Override
        public void setTranslationKey(@Nullable String translationKey) {
            update(d -> new BladeStateData(
                Optional.ofNullable(translationKey).orElse(""), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public float getBaseAttackModifier() {
            return data().baseAttackModifier();
        }
        
        @Override
        public void setBaseAttackModifier(float baseAttackModifier) {
            update(d -> new BladeStateData(d.translationKey(), baseAttackModifier,
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public int getProudSoulCount() {
            return data().proudSoul();
        }
        
        @Override
        public void setProudSoulCount(int psCount) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                Math.max(0, psCount), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public int getKillCount() {
            return data().killCount();
        }
        
        @Override
        public void setKillCount(int killCount) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), killCount, d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public int getRefine() {
            return data().refine();
        }
        
        @Override
        public void setRefine(int refine) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), refine, d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public boolean isBroken() {
            return data().broken();
        }
        
        @Override
        public void setBroken(boolean broken) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), broken, d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public boolean isSealed() {
            return data().sealed();
        }
        
        @Override
        public void setSealed(boolean sealed) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), sealed,
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public ResourceLocation getSlashArtsKey() {
            return data().slashArtsKey();
        }
        
        @Override
        public void setSlashArtsKey(@Nullable ResourceLocation key) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                key != null ? key : BladeStateData.DEFAULT_SLASH_ARTS, d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public boolean isDefaultBewitched() {
            return data().defaultBewitched();
        }
        
        @Override
        public void setDefaultBewitched(boolean defaultBewitched) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), defaultBewitched, d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public CarryType getCarryType() {
            return data().carryType();
        }
        
        @Override
        public void setCarryType(@Nullable CarryType carryType) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                carryType != null ? carryType : CarryType.PSO2,
                d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public Color getEffectColor() {
            return new Color(data().effectColor());
        }
        
        @Override
        public int getColorCode() {
            return data().effectColor();
        }
        
        @Override
        public void setEffectColor(@Nullable Color effectColor) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), effectColor != null ? effectColor.getRGB() : 0xFF3333FF,
                d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public boolean isEffectColorInverse() {
            return data().effectColorInverse();
        }
        
        @Override
        public void setEffectColorInverse(boolean effectColorInverse) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), effectColorInverse, d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public Vec3 getAdjust() {
            return data().adjust();
        }
        
        @Override
        public void setAdjust(@Nullable Vec3 adjust) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(),
                adjust != null ? adjust : Vec3.ZERO,
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public Optional<ResourceLocation> getTexture() {
            return data().texture();
        }
        
        @Override
        public void setTexture(@Nullable ResourceLocation texture) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                Optional.ofNullable(texture), d.model(), d.specialEffects()));
        }
        
        @Override
        public Optional<ResourceLocation> getModel() {
            return data().model();
        }
        
        @Override
        public void setModel(@Nullable ResourceLocation model) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), Optional.ofNullable(model), d.specialEffects()));
        }
        
        @Override
        public ResourceLocation getComboRoot() {
            ResourceLocation root = data().comboRoot();
            if (!ComboStateRegistry.REGISTRY.containsKey(root)) {
                return ComboStateRegistry.STANDBY.getId();
            }
            return root;
        }
        
        @Override
        public void setComboRoot(ResourceLocation resourceLocation) {
            ResourceLocation resolved = ComboStateRegistry.REGISTRY.containsKey(resourceLocation)
                ? resourceLocation : ComboStateRegistry.STANDBY.getId();
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), d.destructable(), resolved,
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public Collection<ResourceLocation> getSpecialEffects() {
            return data().specialEffects();
        }
        
        @Override
        public void setSpecialEffects(@Nullable ListTag list) {
            List<ResourceLocation> result = new ArrayList<>();
            if (list != null) {
                list.forEach(tag -> {
                    ResourceLocation se = ResourceLocation.tryParse(tag.getAsString());
                    if (se != null && SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                        result.add(se);
                    }
                });
            }
            update(d -> d.withSpecialEffects(result));
        }
        
        @Override
        public boolean addSpecialEffect(ResourceLocation se) {
            if (!SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                return false;
            }
            BladeStateData current = data();
            if (current.specialEffects().contains(se)) {
                return false;
            }
            List<ResourceLocation> updated = new ArrayList<>(current.specialEffects());
            updated.add(se);
            update(d -> d.withSpecialEffects(updated));
            return true;
        }
        
        @Override
        public boolean removeSpecialEffect(ResourceLocation se) {
            BladeStateData current = data();
            if (!current.specialEffects().contains(se)) {
                return false;
            }
            List<ResourceLocation> updated = new ArrayList<>(current.specialEffects());
            updated.remove(se);
            update(d -> d.withSpecialEffects(updated));
            return true;
        }
        
        @Override
        public boolean hasSpecialEffect(ResourceLocation se) {
            if (!SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                removeSpecialEffect(se);
                return false;
            }
            return data().specialEffects().contains(se);
        }
        
        // ========== Vanilla durability (routed to DataComponents) ==========
        
        @Override
        public int getDamage() {
            return stack.getDamageValue();
        }
        
        @Override
        public void setDamage(int damage) {
            int maxDamage = stack.getMaxDamage();
            BladeStateData d = data();
            if (d.broken()) {
                if (damage <= 0 && !d.sealed()) {
                    setBroken(false);
                } else if (maxDamage < damage) {
                    damage = Math.min(damage, maxDamage - 1);
                }
            }
            stack.set(DataComponents.DAMAGE, Mth.clamp(damage, 0, stack.getMaxDamage()));
        }
        
        @Override
        public int getMaxDamage() {
            return stack.getMaxDamage();
        }
        
        @Override
        public void setMaxDamage(int damage) {
            stack.set(DataComponents.MAX_DAMAGE, Math.max(1, damage));
        }
        
        @Override
        public boolean isDestructable() {
            return data().destructable();
        }
        
        @Override
        public void setDestructable(boolean destructable) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), d.defaultSwordTypes(), destructable, d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        @Override
        public List<SwordType> getDefaultSwordTypes() {
            return data().defaultSwordTypes().orElse(List.of());
        }
        
        @Override
        public void setDefaultSwordTypes(List<SwordType> defaultSwordTypes) {
            update(d -> new BladeStateData(d.translationKey(), d.baseAttackModifier(),
                d.proudSoul(), d.killCount(), d.refine(), d.broken(), d.sealed(),
                d.slashArtsKey(), d.defaultBewitched(), Optional.of(defaultSwordTypes), d.destructable(), d.comboRoot(),
                d.carryType(), d.effectColor(), d.effectColorInverse(), d.adjust(),
                d.texture(), d.model(), d.specialEffects()));
        }
        
        // ========== Runtime fields (BladeRuntimeStateData) ==========
        
        @Override
        public long getLastActionTime() {
            return runtime().lastActionTime();
        }
        
        @Override
        public void setLastActionTime(long lastActionTime) {
            updateRuntime(r -> new BladeRuntimeStateData(r.comboSeq(), lastActionTime, r.lastProcessedComboTick(), r.targetEntityId(), r.onClick(), r.fallDecreaseRate(), r.attackAmplifier()));
        }
        
        @Override
        public boolean onClick() {
            return runtime().onClick();
        }
        
        @Override
        public void setOnClick(boolean onClick) {
            updateRuntime(r -> new BladeRuntimeStateData(r.comboSeq(), r.lastActionTime(), r.lastProcessedComboTick(), r.targetEntityId(), onClick, r.fallDecreaseRate(), r.attackAmplifier()));
        }
        
        @Override
        public float getFallDecreaseRate() {
            return runtime().fallDecreaseRate();
        }
        
        @Override
        public void setFallDecreaseRate(float fallDecreaseRate) {
            updateRuntime(r -> new BladeRuntimeStateData(r.comboSeq(), r.lastActionTime(), r.lastProcessedComboTick(), r.targetEntityId(), r.onClick(), fallDecreaseRate, r.attackAmplifier()));
        }
        
        @Override
        public float getAttackAmplifier() {
            return runtime().attackAmplifier();
        }
        
        @Override
        public void setAttackAmplifier(float attackAmplifier) {
            updateRuntime(r -> new BladeRuntimeStateData(r.comboSeq(), r.lastActionTime(), r.lastProcessedComboTick(), r.targetEntityId(), r.onClick(), r.fallDecreaseRate(), attackAmplifier));
        }
        
        @Override
        public ResourceLocation getComboSeq() {
            return runtime().comboSeq();
        }
        
        @Override
        public void setComboSeq(@Nullable ResourceLocation comboSeq) {
            ResourceLocation resolved = comboSeq != null ? comboSeq : ComboStateRegistry.NONE.getId();
            updateRuntime(r -> new BladeRuntimeStateData(resolved, r.lastActionTime(), -1L, r.targetEntityId(), r.onClick(), r.fallDecreaseRate(), r.attackAmplifier()));
        }
        
        @Override
        public int getTargetEntityId() {
            return runtime().targetEntityId();
        }
        
        @Override
        public void setTargetEntityId(int id) {
            updateRuntime(r -> new BladeRuntimeStateData(r.comboSeq(), r.lastActionTime(), r.lastProcessedComboTick(), id, r.onClick(), r.fallDecreaseRate(), r.attackAmplifier()));
        }
        
        @Override
        public long getLastProcessedComboTick() {
            return runtime().lastProcessedComboTick();
        }
        
        @Override
        public void setLastProcessedComboTick(long tick) {
            updateRuntime(r -> new BladeRuntimeStateData(r.comboSeq(), r.lastActionTime(), tick, r.targetEntityId(), r.onClick(), r.fallDecreaseRate(), r.attackAmplifier()));
        }
        
        // ========== Lifecycle ==========
        
        @Override
        public boolean isEmpty() {
            return !stack.has(SlashBladeDataComponents.BLADE_STATE_DATA.get());
        }
        
        @Override
        public void setNonEmpty() {
            if (isEmpty()) {
                BladeStateData data = BladeStateData.DEFAULT;
                if (stack.getMaxDamage() <= 0) {
                    stack.set(DataComponents.MAX_DAMAGE, 40);
                }
                stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), data);
            }
        }
        
        // ========== NBT serialization (compat bridge) ==========
        
        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            BladeStateData d = data();
            
            tag.putString("translationKey", d.translationKey());
            tag.putFloat("baseAttackModifier", d.baseAttackModifier());
            tag.putInt("proudSoul", d.proudSoul());
            tag.putInt("killCount", d.killCount());
            tag.putInt("RepairCounter", d.refine());
            tag.putBoolean("isBroken", d.broken());
            tag.putBoolean("isSealed", d.sealed());
            tag.putString("SpecialAttackType", d.slashArtsKey().toString());
            tag.putBoolean("isDefaultBewitched", d.defaultBewitched());
            tag.putBoolean("destructable", d.destructable());
            tag.putByte("StandbyRenderType", (byte) d.carryType().ordinal());
            tag.putInt("SummonedSwordColor", d.effectColor());
            tag.putBoolean("SummonedSwordColorInverse", d.effectColorInverse());
            tag.put("adjustXYZ", NBTHelper.newDoubleNBTList(d.adjust()));
            d.texture().ifPresent(loc -> tag.putString("TextureName", loc.toString()));
            d.model().ifPresent(loc -> tag.putString("ModelName", loc.toString()));
            tag.putString("ComboRoot", d.comboRoot().toString());
            
            if (!d.specialEffects().isEmpty()) {
                ListTag seList = new ListTag();
                d.specialEffects().forEach(se -> seList.add(StringTag.valueOf(se.toString())));
                tag.put("SpecialEffects", seList);
            }
            
            BladeRuntimeStateData rt = runtime();
            tag.putLong("lastActionTime", rt.lastActionTime());
            tag.putInt("TargetEntity", rt.targetEntityId());
            tag.putBoolean("_onClick", rt.onClick());
            tag.putFloat("fallDecreaseRate", rt.fallDecreaseRate());
            tag.putFloat("AttackAmplifier", rt.attackAmplifier());
            tag.putString("currentCombo", rt.comboSeq().toString());
            tag.putInt("Damage", stack.getDamageValue());
            tag.putInt("maxDamage", stack.getMaxDamage());
            
            return tag;
        }
        
        @Override
        public void deserializeNBT(@Nullable CompoundTag tag) {
            if (tag == null) {
                return;
            }
            setNonEmpty();
            
            BladeStateData d = data();
            String translationKey = tag.contains("translationKey") ? tag.getString("translationKey") : d.translationKey();
            float baseAttack = tag.contains("baseAttackModifier") ? tag.getFloat("baseAttackModifier") : d.baseAttackModifier();
            int proudSoul = tag.contains("proudSoul") ? tag.getInt("proudSoul") : d.proudSoul();
            int killCount = tag.contains("killCount") ? tag.getInt("killCount") : d.killCount();
            int refine = tag.contains("RepairCounter") ? tag.getInt("RepairCounter") : d.refine();
            boolean broken = tag.contains("isBroken") ? tag.getBoolean("isBroken") : d.broken();
            boolean sealed = tag.contains("isSealed") ? tag.getBoolean("isSealed") : d.sealed();
            ResourceLocation slashArts = tag.contains("SpecialAttackType")
                ? ResourceLocation.tryParse(tag.getString("SpecialAttackType")) : d.slashArtsKey();
            boolean defaultBewitched = tag.contains("isDefaultBewitched") ? tag.getBoolean("isDefaultBewitched") : d.defaultBewitched();
            boolean destructable = tag.contains("destructable") ? tag.getBoolean("destructable") : d.destructable();
            CarryType carryType = d.carryType();
            if (tag.contains("StandbyRenderType")) {
                int ordinal = tag.getByte("StandbyRenderType");
                CarryType[] values = CarryType.values();
                if (ordinal >= 0 && ordinal < values.length) {
                    carryType = values[ordinal];
                }
            }
            int color = tag.contains("SummonedSwordColor") ? tag.getInt("SummonedSwordColor") : d.effectColor();
            boolean colorInverse = tag.contains("SummonedSwordColorInverse") ? tag.getBoolean("SummonedSwordColorInverse") : d.effectColorInverse();
            Vec3 adjust = tag.contains("adjustXYZ") ? NBTHelper.getVector3d(tag, "adjustXYZ") : d.adjust();
            
            Optional<ResourceLocation> texture = tag.contains("TextureName")
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString("TextureName"))) : d.texture();
            Optional<ResourceLocation> model = tag.contains("ModelName")
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString("ModelName"))) : d.model();
            
            ResourceLocation comboRoot = tag.contains("ComboRoot")
                ? ResourceLocation.tryParse(tag.getString("ComboRoot")) : d.comboRoot();
            if (comboRoot == null) {
                comboRoot = BladeStateData.DEFAULT_COMBO_ROOT;
            }
            
            List<ResourceLocation> effects;
            if (tag.contains("SpecialEffects")) {
                List<ResourceLocation> newEffects = new ArrayList<>();
                ListTag list = tag.getList("SpecialEffects", 8);
                for (int i = 0; i < list.size(); i++) {
                    ResourceLocation se = ResourceLocation.tryParse(list.getString(i));
                    if (se != null) {
                        newEffects.add(se);
                    }
                }
                effects = newEffects;
            } else {
                effects = new ArrayList<>(d.specialEffects());
            }
            
            List<SwordType> defaultSwordTypes;
            if (tag.contains("DefaultSwordTypes")) {
                List<SwordType> newSwordTypes = new ArrayList<>();
                ListTag list = tag.getList("DefaultSwordTypes", 8);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        newSwordTypes.add(SwordType.valueOf(list.getString(i).toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        SlashBlade.LOGGER.warn("Unknown SwordType: {}", list.getString(i));
                    }
                }
                defaultSwordTypes = newSwordTypes;
            } else {
                defaultSwordTypes = d.defaultSwordTypes().orElse(new ArrayList<>());
            }
            
            BladeStateData newData = new BladeStateData(translationKey, baseAttack, proudSoul, killCount,
                refine, broken, sealed, slashArts != null ? slashArts : BladeStateData.DEFAULT_SLASH_ARTS,
                defaultBewitched, Optional.of(defaultSwordTypes), destructable, comboRoot, carryType, color, colorInverse, adjust,
                texture, model, effects);
            stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), newData);
            
            if (tag.contains("maxDamage")) {
                stack.set(DataComponents.MAX_DAMAGE, Math.max(1, tag.getInt("maxDamage")));
            }
            if (tag.contains("Damage")) {
                stack.setDamageValue(tag.getInt("Damage"));
            }
            
            ResourceLocation comboSeq = ComboStateRegistry.NONE.getId();
            if (tag.contains("currentCombo")) {
                ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("currentCombo"));
                if (parsed != null) {
                    comboSeq = parsed;
                }
            }
            long lastActionTime = tag.contains("lastActionTime") ? tag.getLong("lastActionTime") : 0L;
            int targetEntityId = tag.contains("TargetEntity") ? tag.getInt("TargetEntity") : -1;
            boolean onClick = tag.contains("_onClick") && tag.getBoolean("_onClick");
            float fallDecreaseRate = tag.contains("fallDecreaseRate") ? tag.getFloat("fallDecreaseRate") : 0.0F;
            float attackAmplifier = tag.contains("AttackAmplifier") ? tag.getFloat("AttackAmplifier") : 0.0F;
            
            long lastProcessedComboTick = -1L;
            BladeRuntimeStateData rt = new BladeRuntimeStateData(comboSeq, lastActionTime, lastProcessedComboTick, targetEntityId, onClick, fallDecreaseRate, attackAmplifier);
            stack.set(SlashBladeDataComponents.BLADE_RUNTIME_STATE.get(), rt);
            
            updateRarityIfApplicable();
        }
        
        private void updateRarityIfApplicable() {
            ItemSlashBlade.updateRarity(stack);
        }
    }
}
