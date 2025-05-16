package ipana.renders.ide.panels.pack.item;

import ipana.renders.ide.panels.code.Code;

public class ClassItem extends PanelItem {
    private Package classPackage;
    private Code code;
    private int selectedLine = -1;
    private int cursor;

    public ClassItem(String path, Package classPackage) {
        super(path);
        this.classPackage = classPackage;
        code = new Code("package "+classPackage.getAbsolutePath()+";", "", "public class "+getName() +" {", "", "}");
    }

    public int selectedLine() {
        return selectedLine;
    }

    public void setSelectedLine(int _selectedLine) {
        selectedLine = _selectedLine;
    }

    public int cursor() {
        return cursor;
    }

    public void setCursor(int _cursor) {
        cursor = _cursor;
    }

    public Package getPackage() {
        return classPackage;
    }

    public Code getCode() {
        return code;
    }
}
