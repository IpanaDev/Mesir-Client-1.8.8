package ipana.renders.ide.panels.pack.item;

public class CopiedPackage {
    private Package pack;
    private float x;

    public CopiedPackage(Package pack, float x) {
        this.pack = pack;
        this.x = x;
    }

    public Package getPackage() {
        return pack;
    }

    public void setPackage(Package pack) {
        this.pack = pack;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }
}
