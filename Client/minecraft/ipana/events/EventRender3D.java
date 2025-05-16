package ipana.events;

import net.minecraft.client.renderer.culling.Frustum;
import pisi.unitedmeows.eventapi.event.Event;

public class EventRender3D extends Event{

    private float partialTicks;
    private Frustum camera;

    public EventRender3D(float partialTicks, Frustum camera) {
        this.partialTicks = partialTicks;
        this.camera = camera;
    }

    public Frustum camera() {
        return camera;
    }

    public float partialTicks() {
        return this.partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
