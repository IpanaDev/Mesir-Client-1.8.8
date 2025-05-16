package ipana.renders.ide.panels.code;

public class Line {
    private String raw;
    private String colored;
    private boolean needCompile;

    public Line(String text) {
        this.raw = text;
        this.colored = text;
        this.needCompile = true;
    }

    public void changeText(String text) {
        raw = text;
        needCompile = true;
    }

    public String raw() {
        return raw;
    }

    public String colored() {
        return colored;
    }

    public void setColored(String _colored) {
        colored = _colored;
    }

    public void setNeedCompile(boolean _needCompile) {
        needCompile = _needCompile;
    }

    public boolean needCompile() {
        return needCompile;
    }

}
