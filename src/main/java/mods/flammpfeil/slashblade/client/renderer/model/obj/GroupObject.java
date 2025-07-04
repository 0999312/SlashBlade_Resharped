package mods.flammpfeil.slashblade.client.renderer.model.obj;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class GroupObject {
    public String name;
    public ArrayList<Face> faces = new ArrayList<>();
    public int glDrawingMode;

    public GroupObject() {
        this("");
    }

    public GroupObject(String name) {
        this(name, -1);
    }

    public GroupObject(String name, int glDrawingMode) {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(VertexConsumer tessellator) {
        if (!faces.isEmpty()) {
            for (Face face : faces) {
                face.addFaceForRender(tessellator);
            }
        }
    }
}
