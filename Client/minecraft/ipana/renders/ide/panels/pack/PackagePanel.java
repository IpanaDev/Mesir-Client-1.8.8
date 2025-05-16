package ipana.renders.ide.panels.pack;

import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.renders.ide.Panel;
import ipana.renders.ide.panels.Screen;
import ipana.renders.ide.panels.code.CodePanel;
import ipana.renders.ide.panels.pack.item.ClassItem;
import ipana.renders.ide.panels.pack.item.CopiedPackage;
import ipana.renders.ide.panels.pack.item.Package;
import ipana.renders.ide.util.ImageUtils;
import ipana.utils.file.FileUtils;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PackagePanel extends Panel {

    private List<Package> packages;
    private final FontUtil font = FontHelper.SIZE_18;
    public static final PackagePanel INSTANCE = new PackagePanel();
    private Package selectedPackage;
    public ClassItem selectedClass;

    public PackagePanel() {
        super(0,0,"200","window");
    }

    @Override
    public void initPanel() {
        packages = new ArrayList<>();
        Package mainPackage = new Package("ipana",true);
        /* Adding Main Package */
        addPackage(mainPackage);
        mainPackage.setExtended(false);

        /* Adding Module Package */
        Package modulePackage = mainPackage.addChild("modules");
        addPackage(modulePackage);
        for (Category category : Category.VALUES) {
            Package pack = modulePackage.addChild(category.name().toLowerCase());
            addPackage(pack);
            for (Module addons : ModuleManager.compiler().addonsToAdd()[category.ordinal()]) {
                ClassItem classItem = pack.addClass(addons.getName());
                classItem.getCode().setLines(classItem.getCode().linesFromList(FileUtils.read(new File(FileUtils.getConfigDir(), "addons\\modules\\"+addons.getName()+".java"))));
            }
        }
        /* Adding Util Package */
        addPackage(mainPackage.addChild("utils"));
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        updateSizes();
        RenderUtils.drawFixedRect(getX(),getY(),getX()+getWidth(),getY()+getHeight(),new Color(120,120,120));
        int x;
        int y = 5;
        double height = 10;
        CopiedPackage[] saves = new CopiedPackage[0];
        for (Package pack : packages) {
            if (pack.getParent().length == 0 || isParentsExtended(pack)) {
                x = (pack.getParent().length + 1) * 8 + 5;
                List<CopiedPackage> list = Arrays.asList(saves);
                Collections.reverse(list);
                for (CopiedPackage saved : list) {
                    boolean foundParent = false;
                    for (Package parents : pack.getParent()) {
                        if (saved != null && parents.getName().equals(saved.getPackage().getName())) {
                            foundParent = true;
                            break;
                        }
                    }
                    if (!foundParent && saved != null) {
                        for (ClassItem classItem : saved.getPackage().getClasses()) {
                            if (isHovered(mouseX, mouseY, (int) getX(), y - 1, getX() + getWidth(), y + height - 1) && getActiveScreens().isEmpty()) {
                                RenderUtils.drawFixedRect((int) getX(), y - 1, getX() + getWidth(), y + height - 1, new Color(255, 1, 1, 120));
                            }
                            RenderUtils.drawImage(saved.getX() - 10, y - 1, 10, 10, ImageUtils.CLASS);
                            font.drawString(classItem.getName(), saved.getX(), y, Color.white.getRGB());
                            y += height;
                        }
                    }
                }
                if (selectedPackage == pack) {
                    RenderUtils.drawFixedRect((int) getX(), y - 1, getX() + getWidth(), y + height - 1, new Color(100, 1, 1, 120));
                }
                if (isHovered(mouseX, mouseY, (int) getX(), y - 1, getX() + getWidth(), y + height - 1) && getActiveScreens().isEmpty()) {
                    RenderUtils.drawFixedRect((int) getX(), y - 1, getX() + getWidth(), y + height - 1, new Color(255, 1, 1, 120));
                }
                RenderUtils.drawImage(x - 12, y - 2, 12, 12, ImageUtils.PACKAGE);
                String extendText = (pack.getChildren().length > 0 || pack.getClasses().size() > 0) ? pack.isExtended() ? "v" : ">" : "";
                font.drawString(pack.getName()+" "+extendText, x, y, Color.white.getRGB());
                if (!pack.getClasses().isEmpty() && pack.isExtended()) {
                    if (pack.getChildren().length == 0) {
                        x += 8;
                        for (ClassItem classItem : pack.getClasses()) {
                            y += height;
                            if (isHovered(mouseX, mouseY, (int) getX(), y - 1, getX() + getWidth(), y + height - 1) && getActiveScreens().isEmpty()) {
                                RenderUtils.drawFixedRect((int) getX(), y - 1, getX() + getWidth(), y + height - 1, new Color(255, 1, 1, 120));
                            }
                            RenderUtils.drawImage(x - 10, y - 1, 10, 10, ImageUtils.CLASS);
                            font.drawString(classItem.getName(), x, y, Color.white.getRGB());
                        }
                    } else {
                        CopiedPackage[] old = saves;
                        saves = new CopiedPackage[old.length+1];
                        System.arraycopy(old, 0, saves, 0, old.length);
                        saves[saves.length-1] = new CopiedPackage(pack,x+8);
                    }
                }
                y += height;
            }
        }
        int count = 1;
        for (CopiedPackage copiedPackage : saves) {
            System.out.println(copiedPackage.getPackage().getName()+" : "+count);
            count+=1;
        }
        for (Screen screen : getActiveScreens()) {
            screen.draw(mouseX, mouseY);
        }
        int[] array = new int[getActiveScreens().size()];
        Arrays.fill(array, -1);
        for (int i = 0; i < getActiveScreens().size(); i++) {
            Screen screen = getActiveScreens().get(i);
            if (screen.isShouldRemove()) {
                array[i] = i;
            }
        }
        for (int i : array) {
            if (i != -1) {
                getActiveScreens().remove(i);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        int y = 5;
        double height = 10;
        Package saved = null;
        for (Package pack : packages) {
            if (pack.getParent().length == 0 || isParentsExtended(pack)) {
                boolean foundParent = false;
                for (Package parents : pack.getParent()) {
                    if (saved != null && parents.getName().equals(saved.getName())) {
                        foundParent = true;
                        break;
                    }
                }
                if (!foundParent && saved != null) {
                    for (ClassItem classItem : saved.getClasses()) {
                        if (isHovered(mouseX, mouseY, (int) getX(), y - 1, getX() + getWidth(), y + height - 1) && getActiveScreens().isEmpty()) {
                            selectedClass = classItem;
                            if (button == 0) {
                                CodePanel.INSTANCE.setClassItem(selectedClass);
                            } else if (button == 1) {
                                getActiveScreens().add(new EditClassScreen(getX() + 30, y - 1, saved, classItem));
                            }
                        }
                        y += height;
                    }
                    saved = null;
                }
                if (isHovered(mouseX, mouseY, (int) getX(), y - 1, getX() + getWidth(), y + height - 1) && getActiveScreens().isEmpty()) {
                    if (button == 0) {
                        selectedPackage = pack;
                        pack.setExtended(!pack.isExtended());
                    } else if (button == 1) {
                        selectedPackage = pack;
                        if (createScreenIndex() != -1) {
                            getActiveScreens().remove(createScreenIndex());
                        }
                        getActiveScreens().add(new CreateItemScreen(getX() + 30, y - 1, selectedPackage));
                    }
                }
                if (pack.getClasses().size() > 0 && pack.isExtended()) {
                    if (pack.getChildren().length == 0) {
                        for (ClassItem classItem : pack.getClasses()) {
                            y += height;
                            if (isHovered(mouseX, mouseY, (int) getX(), y - 1, getX() + getWidth(), y + height - 1) && getActiveScreens().isEmpty()) {
                                selectedClass = classItem;
                                if (button == 0) {
                                    CodePanel.INSTANCE.setClassItem(selectedClass);
                                } else if (button == 1) {
                                    getActiveScreens().add(new EditClassScreen(getX() + 30, y - 1, pack, classItem));
                                }
                            }
                        }
                    } else {
                        saved = pack;
                    }
                }
                y += height;
            }
        }
        for (Screen screen : getActiveScreens()) {
            screen.onClick(mouseX, mouseY, button);
        }
    }

    private int createScreenIndex() {
        int index = -1;
        for (Screen screen : getActiveScreens()) {
            if (screen instanceof CreateItemScreen) {
                index = getActiveScreens().indexOf(screen);
                break;
            }
        }
        return index;
    }

    @Override
    public void keyPressed(int key, char typedChar) {
        for (Screen screen : getActiveScreens()) {
            screen.onPress(key, typedChar);
        }
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, double width, double height) {
        return mouseX > x && mouseX < width && mouseY > y && mouseY < height;
    }

    public boolean isParentsExtended(Package pack) {
        boolean extended = true;
        for (Package p : pack.getParent()) {
            if (!p.isExtended()) {
                extended = false;
                break;
            }
        }
        return extended;
    }

    public void addPackage(Package newPackage) {
        if (!packages.contains(newPackage)) {
            if (newPackage.getParent().length > 0) {
                Package parent = newPackage.getParent()[newPackage.getParent().length-1];
                List<Package> workTable = new ArrayList<>();
                int parentIndex = packages.indexOf(parent);
                int size = packages.size();
                boolean hasBrothers = newPackage.getBrothers().length > 0;
                boolean hasBrotherHasChildren = hasBrothers && newPackage.getBrothers()[newPackage.getBrothers().length-1].getChildren().length > 0;
                for (int i = 0; i < parentIndex+1; i++) {
                    workTable.add(packages.get(i));
                }
                if (!hasBrothers || !hasBrotherHasChildren) {
                    workTable.add(newPackage);
                }
                for (int i = parentIndex+1; i < size; i++) {
                    workTable.add(packages.get(i));
                }
                if (hasBrothers && hasBrotherHasChildren) {
                    workTable.add(newPackage);
                }
                packages = workTable;
            } else {
                packages.add(newPackage);
            }
        }
    }
}