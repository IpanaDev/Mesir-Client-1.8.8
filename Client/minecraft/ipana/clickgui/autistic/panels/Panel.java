package ipana.clickgui.autistic.panels;

public abstract class Panel {

    public abstract void render(int mouseX, int mouseY, float partialTicks);

    public abstract void mouseClicked(int mouseX, int mouseY, int button);

    public abstract void mouseReleased(int mouseX, int mouseY, int button);

    public abstract boolean isHovered(int mouseX, int mouseY);
}
