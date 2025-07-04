package mods.flammpfeil.slashblade.util;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class VectorHelper {

    static public Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = pitch * ((float) Math.PI / 180F);
        float f1 = -yaw * ((float) Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

    static public Vec3i f2i(Vec3 src) {
        return new Vec3i(Mth.floor(src.x), Mth.floor(src.y), Mth.floor(src.z));
    }

    static public Vec3i f2i(double x, double y, double z) {
        return new Vec3i(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    static public Matrix4f matrix4fFromArray(float[] in) {
        return new Matrix4f(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8], in[9], in[10], in[11],
                in[12], in[13], in[14], in[15]);
    }
}
