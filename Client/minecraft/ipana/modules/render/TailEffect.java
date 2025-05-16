package ipana.modules.render;

import ipana.events.EventPreUpdate;
import ipana.events.EventRender3D;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.ColorValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.tail.Tail3D;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;

public class TailEffect extends Module {
    public TailEffect() {
        super("TailEffect", Keyboard.KEY_NONE, Category.Render, "AYOOOO cool effect.");
    }
    private NumberValue<Integer> ms = new NumberValue<>("UpdateMs",this,25,0,100,5,"Update ms of tail3D.");
    private NumberValue<Integer> ticks = new NumberValue<>("Ticks",this,20,10,100,5,"Tail3D length.");
    private NumberValue<Float> width = new NumberValue<>("Width",this,10f,1f,20f,0.5f,"Tail3D width.");
    public ColorValue color1 = new ColorValue("Color1",this, new Color(0, 167, 255),"First color.");
    public ColorValue color2 = new ColorValue("Color2",this, new Color(255, 0, 234),"Second color.");
    private Tail3D tail3D = new Tail3D();

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        tail3D.setBoneMs(ms.getValue());
        tail3D.setBoneTicks(ticks.getValue());
        tail3D.setTailWidth(width.getValue());
        tail3D.setColor1(color1.getValue());
        tail3D.setColor2(color2.getValue());
        tail3D.update();
    });

    private Listener<EventRender3D> onRender = new Listener<>(event -> {
        tail3D.setTranslates(0,0,0);
        tail3D.setRenderPosition(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
        tail3D.render(true, true);
    });
}
