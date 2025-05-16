package ipana.renders.ide.panels.pack.item;

import java.util.ArrayList;

public class Package extends PanelItem {
    private Package[] children;
    private Package[] parent;
    private ArrayList<ClassItem> classes;
    private boolean extended;

    public Package(String path) {
        super(path);
        children = new Package[0];
        parent = new Package[0];
        classes = new ArrayList<>();
        this.extended = false;
    }
    public Package(String path, boolean extended) {
        super(path);
        children = new Package[0];
        parent = new Package[0];
        classes = new ArrayList<>();
        this.extended = extended;
    }
    public Package[] getChildren() {
        return children;
    }

    public Package[] getParent() {
        return parent;
    }

    public ArrayList<ClassItem> getClasses() {
        return classes;
    }

    public Package addChild(String child) {
        int childrenSize = children.length;
        Package[] oldChildren = children;
        children = new Package[childrenSize+1];
        System.arraycopy(oldChildren, 0, children, 0, oldChildren.length);
        Package newPackage = new Package(" "+child);
        children[childrenSize] = newPackage;

        int parentLength = parent.length;
        Package[] oldParent = parent;
        newPackage.parent = new Package[parentLength+1];
        System.arraycopy(oldParent, 0, newPackage.parent, 0, oldParent.length);
        newPackage.parent[parentLength] = this;

        return newPackage;
    }

    public Package[] getBrothers() {
        Package parent = getParent()[getParent().length-1];
        Package[] brothers = new Package[parent.getChildren().length-1];
        for (int i = 0; i < parent.getChildren().length-1; i++) {
            brothers[i] = parent.getChildren()[i];
        }
        return brothers;
    }

    public ClassItem addClass(String className) {
        ClassItem classItem = new ClassItem(getPath()+" "+className,this);
        classes.add(classItem);
        return classItem;
    }
    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public String getAbsolutePath() {
        StringBuilder str = new StringBuilder();
        for (Package paths : getParent()) {
            str.append(paths.getName()).append(".");
        }
        str.append(getName());
        return str.toString();
    }
}
