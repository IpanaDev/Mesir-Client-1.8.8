package ipana.events;


import pisi.unitedmeows.eventapi.event.Event;

public class EventRender2D extends Event{

    private int width;
    private int height;
    private float partialTicks;

    public EventRender2D(int width, int height, float partialTicks)
    {
        this.width = width;
        this.height = height;
        this.partialTicks = partialTicks;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public float partialTicks() {
        return partialTicks;
    }
}
