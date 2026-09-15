package mods.flammpfeil.slashblade.item;

import com.mojang.serialization.Codec;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public enum SwordType implements IExtensibleEnum {
    NONE((state, item) -> false),
    EDGEFRAGMENT((state, item) -> state.isEmpty()),
    BROKEN((state, item) -> !item.has(DataComponents.UNBREAKABLE) && state.map(ISlashBladeState::isBroken).orElse(false)),
    ENCHANTED((state, item) -> item.isEnchanted() && !state.map(ISlashBladeState::isSealed).orElse(false)),
    BEWITCHED((state, item) -> state.map(s -> !s.isSealed() && item.isEnchanted() && (item.has(DataComponents.CUSTOM_NAME) || s.isDefaultBewitched())).orElse(false)),
    FIERCEREDGE((state, item) -> state.map(s -> s.getKillCount() >= 1000).orElse(false)),
    NOSCABBARD((state, item) -> state.isEmpty()),
    SEALED((state, item) -> state.map(ISlashBladeState::isSealed).orElse(false)),
    UNBREAKABLE((state, item) -> false),
    SOULEATER((state, item) -> state.map(s -> s.getProudSoulCount() >= 10000).orElse(false)),
    ;
    
    public final BiFunction<Optional<ISlashBladeState>, ItemStack, Boolean> checkFunction;
    
    SwordType(BiFunction<Optional<ISlashBladeState>, ItemStack, Boolean> checkFunction) {
        this.checkFunction = checkFunction;
    }
    
    public static final Codec<SwordType> CODEC = Codec.STRING.xmap(string -> SwordType.valueOf(string.toUpperCase()),
        instance -> instance.name().toLowerCase());
    
    public static EnumSet<SwordType> from(ItemStack itemStackIn) {
        EnumSet<SwordType> types = EnumSet.noneOf(SwordType.class);
        Optional<ISlashBladeState> state = BladeStateAccess.of(itemStackIn);
        for (SwordType type : SwordType.values()) {
            if (type.checkFunction.apply(state, itemStackIn)) {
                types.add(type);
            }
        }
        if (itemStackIn.getItem() instanceof ItemSlashBlade slashBlade) {
            types = slashBlade.getSwordType(types.clone(), itemStackIn, state);
        }
        types.addAll(state.map(ISlashBladeState::getDefaultSwordTypes).orElse(List.of()));
        return types;
    }
    
    public static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(SwordType.class);
    }
}
