package ipana.renders.ide.panels.pack.item;

public abstract class PanelItem {
    private String path,name;

    public PanelItem(String path) {
        this.path = path;
        if (path.contains(" ")) {
            String[] pathNames = path.split(" ");
            this.name = pathNames[pathNames.length-1];
        } else {
            this.name = path;
        }
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
