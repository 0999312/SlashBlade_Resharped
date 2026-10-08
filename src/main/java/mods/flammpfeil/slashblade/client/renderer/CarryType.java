package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.serialization.Codec;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

public enum CarryType implements IExtensibleEnum {
    NONE,
    DEFAULT,
    PSO2,
    NINJA,
    KATANA,
    RNINJA,
    ;
    
    public static final Codec<CarryType> CODEC = Codec.STRING.xmap(string -> CarryType.valueOf(string.toUpperCase()),
        instance -> instance.name().toLowerCase());
    
    public static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(CarryType.class);
    }
}
