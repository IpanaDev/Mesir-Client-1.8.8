package ipana.events;

import net.minecraft.util.AxisAlignedBB;
import pisi.unitedmeows.eventapi.event.Event;

public class EventStep extends Event {
    private AxisAlignedBB collisionBB, postBB;

    public EventStep(AxisAlignedBB collisionBB, AxisAlignedBB postBB) {
        this.collisionBB = collisionBB;
        this.postBB = postBB;
    }

    public AxisAlignedBB collisionBB() {
        return collisionBB;
    }

    public AxisAlignedBB postBB() {
        return postBB;
    }
}
