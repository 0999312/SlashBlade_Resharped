package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.slashblade.EnchantmentDefinition;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RequestDefinition {

    public static final Codec<RequestDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("name", SlashBlade.prefix("none"))
                            .forGetter(RequestDefinition::getName),
                    Codec.INT.optionalFieldOf("proud_soul", 0).forGetter(RequestDefinition::getProudSoulCount),
                    Codec.INT.optionalFieldOf("kill", 0).forGetter(RequestDefinition::getKillCount),
                    Codec.INT.optionalFieldOf("refine", 0).forGetter(RequestDefinition::getRefineCount),
                    EnchantmentDefinition.CODEC.listOf().optionalFieldOf("enchantments", Lists.newArrayList())
                            .forGetter(RequestDefinition::getEnchantments),
                    SwordType.CODEC.listOf().optionalFieldOf("sword_type", Lists.newArrayList())
                            .forGetter(RequestDefinition::getDefaultType))
            .apply(instance, RequestDefinition::new));

    private final ResourceLocation name;
    private final int proudSoulCount;
    private final int killCount;
    private final int refineCount;
    private final List<EnchantmentDefinition> enchantments;
    private final List<SwordType> defaultType;

    public RequestDefinition(ResourceLocation name, int proud, int kill, int refine,
                             List<EnchantmentDefinition> enchantments, List<SwordType> defaultType) {
        this.name = name;
        this.proudSoulCount = proud;
        this.killCount = kill;
        this.refineCount = refine;
        this.enchantments = enchantments;
        this.defaultType = defaultType;
    }

    public ResourceLocation getName() {
        return name;
    }

    public int getProudSoulCount() {
        return proudSoulCount;
    }

    public int getKillCount() {
        return killCount;
    }

    public int getRefineCount() {
        return refineCount;
    }

    public List<EnchantmentDefinition> getEnchantments() {
        return enchantments;
    }

    public List<SwordType> getDefaultType() {
        return defaultType;
    }

    public static RequestDefinition fromJSON(JsonObject json) {
        return CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(msg -> SlashBlade.LOGGER.error("Failed to parse : {}", msg)).orElseGet(Builder.newInstance()::build);
    }

    public JsonElement toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).resultOrPartial(msg -> SlashBlade.LOGGER.error("Failed to encode : {}", msg)).orElseThrow();
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.getName());
        buffer.writeInt(this.getProudSoulCount());
        buffer.writeInt(this.getKillCount());
        buffer.writeInt(this.getRefineCount());
        buffer.writeCollection(this.getEnchantments(), (buf, request) -> {
            buf.writeResourceLocation(request.getEnchantmentID());
            buf.writeByte(request.getEnchantmentLevel());
        });

        buffer.writeCollection(this.getDefaultType(), (buf, request) -> buf.writeUtf(request.name().toLowerCase()));
    }

    public static RequestDefinition fromNetwork(FriendlyByteBuf buffer) {
        ResourceLocation name = buffer.readResourceLocation();
        int proud = buffer.readInt();
        int kill = buffer.readInt();
        int refine = buffer.readInt();
        var enchantments = buffer.readList((buf) -> new EnchantmentDefinition(buf.readResourceLocation(), buf.readByte()));
        var types = buffer.readList((buf) -> SwordType.valueOf(buf.readUtf().toUpperCase()));
        return new RequestDefinition(name, proud, kill, refine, enchantments, types);
    }

    public void initItemStack(ItemStack blade) {
        var state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElse(new SlashBladeState(blade));
        state.setNonEmpty();
        if (!this.name.equals(SlashBlade.prefix("none"))) {
            state.setTranslationKey(getTranslationKey());
        }
        state.setProudSoulCount(getProudSoulCount());
        state.setKillCount(getKillCount());
        state.setRefine(getRefineCount());

        this.getEnchantments()
                .forEach(enchantment -> blade.enchant(
                        Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getValue(enchantment.getEnchantmentID())),
                        enchantment.getEnchantmentLevel()));
        this.defaultType.forEach(type -> {
            switch (type) {
                case BEWITCHED -> state.setDefaultBewitched(true);
                case BROKEN -> {
                    blade.setDamageValue(blade.getMaxDamage() - 1);
                    state.setBroken(true);
                }
                case SEALED -> state.setSealed(true);
                default -> {
                }
            }
        });

        blade.getOrCreateTag().put("bladeState", state.serializeNBT());
    }


    public boolean test(ItemStack blade) {
        if (blade == null || blade.isEmpty()) {
            return false;
        }
        if (!blade.getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return false;
        }
        var state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        boolean nameCheck;
        if (this.name.equals(SlashBlade.prefix("none"))) {
            nameCheck = state.getTranslationKey().isBlank();
        } else {
            nameCheck = state.getTranslationKey().equals(getTranslationKey());
        }
        boolean proudCheck = state.getProudSoulCount() >= this.getProudSoulCount();
        boolean killCheck = state.getKillCount() >= this.getKillCount();
        boolean refineCheck = state.getRefine() >= this.getRefineCount();

        for (var enchantment : this.getEnchantments()) {
            if (blade.getEnchantmentLevel(ForgeRegistries.ENCHANTMENTS
                    .getValue(enchantment.getEnchantmentID())) < enchantment.getEnchantmentLevel()) {
                return false;
            }
        }

        boolean types = SwordType.from(blade).containsAll(this.getDefaultType());

        return nameCheck && proudCheck && killCheck && refineCheck && types;
    }

    public String getTranslationKey() {
        return Util.makeDescriptionId("item", this.getName());
    }

    public static class Builder {
        private ResourceLocation name;
        private int proudCount;
        private int killCount;
        private int refineCount;
        private final List<EnchantmentDefinition> enchantments;
        private final List<SwordType> defaultType;

        private Builder() {
            this.name = SlashBlade.prefix("none");
            this.proudCount = 0;
            this.killCount = 0;
            this.refineCount = 0;
            this.enchantments = new ArrayList<>();
            this.defaultType = new ArrayList<>();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder name(ResourceLocation name) {
            this.name = name;
            return this;
        }

        public Builder proudSoul(int proudCount) {
            this.proudCount = proudCount;
            return this;
        }

        public Builder killCount(int killCount) {
            this.killCount = killCount;
            return this;
        }

        public Builder refineCount(int refineCount) {
            this.refineCount = refineCount;
            return this;
        }

        public Builder addEnchantment(EnchantmentDefinition... enchantments) {
            this.enchantments.addAll(Arrays.asList(enchantments));
            return this;
        }

        public Builder addSwordType(SwordType... types) {
            this.defaultType.addAll(Arrays.asList(types));
            return this;
        }

        public RequestDefinition build() {
            return new RequestDefinition(name, proudCount, killCount, refineCount, enchantments, defaultType);
        }
    }
}
