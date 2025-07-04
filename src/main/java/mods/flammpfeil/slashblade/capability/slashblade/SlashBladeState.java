/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Reference implementation of {@link ISlashBladeState}. Use/extend this or
 * implement your own.
 * <p>
 * Derived from the Redstone Flux power system designed by King Lemming and
 * originally utilized in Thermal Expansion and related mods. Created with
 * consent and permission of King Lemming and Team CoFH. Released with
 * permission under LGPL 2.1 when bundled with Forge.
 */
public class SlashBladeState implements ISlashBladeState {

    // action state
    protected long lastActionTime; // lastActionTime
    protected int targetEntityId; // TargetEntity
    protected boolean _onClick; // _onClick
    protected float fallDecreaseRate;
    protected boolean isCharged; // isCharged
    protected float attackAmplifier; // AttackAmplifier
    protected ResourceLocation comboSeq; // comboSeq
    protected String lastPosHash; // lastPosHash
    protected boolean isBroken; // isBroken

    // protected int lumbmanager; //lumbmanager EntityID

    // passive state
    protected boolean isNoScabbard; // isNoScabbard
    protected boolean isSealed; // isSealed

    protected float baseAttackModifier = 4F; // BaseAttackModifier

    protected int killCount; // killCount
    protected int refine; // RepairCounter

    protected UUID owner; // Owner

    protected UUID uniqueId = UUID.randomUUID(); // Owner

    protected String translationKey = "";

    // performance setting
    protected ResourceLocation slashArtsKey; // SpecialAttackType
    protected boolean isDefaultBewitched = false; // isDefaultBewitched

    protected ResourceLocation comboRootName;

    // render info
    protected Optional<CarryType> carryType = Optional.empty(); // StandbyRenderType
    protected Optional<Color> effectColor = Optional.empty(); // SummonedSwordColor
    protected boolean effectColorInverse;// SummonedSwordColorInverse
    protected Optional<Vec3> adjust = Optional.empty();// adjustXYZ

    protected Optional<ResourceLocation> texture = Optional.empty(); // TextureName
    protected Optional<ResourceLocation> model = Optional.empty();// ModelName

    protected LazyOptional<ResourceLocation> rootCombo = instantiateRootComboHolder();

    protected int maxDamage = 40;
    protected int damage = 0;

    protected int proudSoul = 0;

    protected boolean isEmpty = true;

    public SlashBladeState(ItemStack blade) {
        if (!blade.isEmpty()) {
            if (blade.getOrCreateTag().contains("bladeState")) {
                this.deserializeNBT(blade.getOrCreateTag().getCompound("bladeState"));
            }
        }
    }

    @Override
    public long getLastActionTime() {
        return lastActionTime;
    }

    @Override
    public void setLastActionTime(long lastActionTime) {
        this.lastActionTime = lastActionTime;

    }

    @Override
    public boolean onClick() {
        return _onClick;
    }

    @Override
    public void setOnClick(boolean onClick) {
        this._onClick = onClick;

    }

    @Override
    public float getFallDecreaseRate() {
        return fallDecreaseRate;
    }

    @Override
    public void setFallDecreaseRate(float fallDecreaseRate) {
        this.fallDecreaseRate = fallDecreaseRate;

    }

    @Override
    public float getAttackAmplifier() {
        return attackAmplifier;
    }

    @Override
    public void setAttackAmplifier(float attackAmplifier) {
        this.attackAmplifier = attackAmplifier;
    }

    @Override
    @Nonnull
    public ResourceLocation getComboSeq() {
        return comboSeq != null ? comboSeq : Objects.requireNonNull(ComboStateRegistry.NONE.getId());
    }

    @Override
    public void setComboSeq(ResourceLocation comboSeq) {
        this.comboSeq = comboSeq;

    }

    @Override
    public boolean isBroken() {
        return isBroken;
    }

    @Override
    public void setBroken(boolean broken) {
        isBroken = broken;
    }

    @Override
    public boolean isSealed() {
        return isSealed;
    }

    @Override
    public void setSealed(boolean sealed) {
        isSealed = sealed;
    }

    @Override
    public float getBaseAttackModifier() {
        return baseAttackModifier;
    }

    @Override
    public void setBaseAttackModifier(float baseAttackModifier) {
        this.baseAttackModifier = baseAttackModifier;
    }

    @Override
    public int getKillCount() {
        return killCount;
    }

    @Override
    public void setKillCount(int killCount) {
        this.killCount = killCount;

    }

    @Override
    public int getRefine() {
        return refine;
    }

    @Override
    public void setRefine(int refine) {
        this.refine = refine;
    }

    @Override
    public ResourceLocation getSlashArtsKey() {
        return this.slashArtsKey;
    }

    @Override
    public void setSlashArtsKey(ResourceLocation key) {
        this.slashArtsKey = key;
    }

    @Override
    public boolean isDefaultBewitched() {
        return isDefaultBewitched;
    }

    @Override
    public void setDefaultBewitched(boolean defaultBewitched) {
        isDefaultBewitched = defaultBewitched;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return translationKey;
    }

    @Override
    public void setTranslationKey(String translationKey) {
        this.translationKey = Optional.ofNullable(translationKey).orElse("");
    }

    @Override
    @Nonnull
    public CarryType getCarryType() {
        return carryType.orElse(CarryType.NONE);
    }

    @Override
    public void setCarryType(CarryType carryType) {
        this.carryType = Optional.ofNullable(carryType);
    }

    @Override
    public @NotNull Color getEffectColor() {
        return effectColor.orElseGet(() -> new Color(0x3333FF));
    }

    @Override
    public void setEffectColor(Color effectColor) {
        this.effectColor = Optional.ofNullable(effectColor);
    }

    @Override
    public boolean isEffectColorInverse() {
        return effectColorInverse;
    }

    @Override
    public void setEffectColorInverse(boolean effectColorInverse) {
        this.effectColorInverse = effectColorInverse;
    }

    @Override
    public @NotNull Vec3 getAdjust() {
        return adjust.orElse(Vec3.ZERO);
    }

    @Override
    public void setAdjust(Vec3 adjust) {
        this.adjust = Optional.ofNullable(adjust);
    }

    @Override
    public @NotNull Optional<ResourceLocation> getTexture() {
        return texture;
    }

    @Override
    public void setTexture(ResourceLocation texture) {
        this.texture = Optional.ofNullable(texture);
    }

    @Override
    public @NotNull Optional<ResourceLocation> getModel() {
        return model;
    }

    @Override
    public void setModel(ResourceLocation model) {
        this.model = Optional.ofNullable(model);
    }

    @Override
    public int getTargetEntityId() {
        return targetEntityId;
    }

    @Override
    public void setTargetEntityId(int id) {
        targetEntityId = id;
    }

    @Override
    public ResourceLocation getComboRoot() {
        if (this.comboRootName == null || !ComboStateRegistry.REGISTRY.get().containsKey(this.comboRootName)) {
            return ComboStateRegistry.STANDBY.getId();
        }
        return this.comboRootName;
    }

    @Override
    public void setComboRoot(ResourceLocation rootLoc) {
        this.comboRootName = ComboStateRegistry.REGISTRY.get().containsKey(rootLoc) ? rootLoc
                : ComboStateRegistry.STANDBY.getId();
        this.rootCombo = instantiateRootComboHolder();
    }

    private LazyOptional<ResourceLocation> instantiateRootComboHolder() {
        return LazyOptional.of(() -> {
            if (!ComboStateRegistry.REGISTRY.get().containsKey(this.getComboRoot())) {
                return Objects.requireNonNull(ComboStateRegistry.STANDBY.getId());
            } else {
                return this.getComboRoot();
            }
        });
    }

    @Override
    public int getMaxDamage() {
        return this.maxDamage;
    }

    @Override
    public void setMaxDamage(int damage) {
        this.maxDamage = damage;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = Math.max(0, damage);
    }

    @Override
    public int getProudSoulCount() {
        return this.proudSoul;
    }

    @Override
    public void setProudSoulCount(int psCount) {
        this.proudSoul = Math.max(0, psCount);
    }

    protected List<ResourceLocation> specialEffects = new ArrayList<>();

    @Override
    public List<ResourceLocation> getSpecialEffects() {
        return this.specialEffects;
    }

    @Override
    public void setSpecialEffects(ListTag list) {
        List<ResourceLocation> result = new ArrayList<>();
        list.forEach(tag -> {
            ResourceLocation se = ResourceLocation.tryParse(tag.getAsString());
            if (SpecialEffectsRegistry.REGISTRY.get().containsKey(se)) {
                result.add(se);
            }

        });
        this.specialEffects = result;
    }

    @Override
    public boolean addSpecialEffect(ResourceLocation se) {
        if (SpecialEffectsRegistry.REGISTRY.get().containsKey(se)) {
            return this.specialEffects.add(se);
        }
        return false;
    }

    @Override
    public boolean removeSpecialEffect(ResourceLocation se) {
        return this.specialEffects.remove(se);
    }

    @Override
    public boolean hasSpecialEffect(ResourceLocation se) {
        if (SpecialEffectsRegistry.REGISTRY.get().containsKey(se)) {
            return this.specialEffects.contains(se);
        }
        this.specialEffects.remove(se);
        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.isEmpty;
    }

    @Override
    public void setNonEmpty() {
        this.isEmpty = false;
    }

}
