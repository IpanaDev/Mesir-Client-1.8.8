package ipana.renders.ide.panels.pack;

import ipana.Ipana;
import ipana.clickgui.autistic.NewClickGui;
import ipana.compiler.JavaStringCompiler;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.renders.ide.panels.Screen;
import ipana.renders.ide.panels.pack.item.ClassItem;
import ipana.renders.ide.panels.pack.item.Package;
import ipana.utils.file.FileUtils;
import ipana.utils.font.FontHelper;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class EditClassScreen extends Screen {
    private Minecraft mc = Minecraft.getMinecraft();

    public EditClassScreen(float x, float y, Package pack, ClassItem classItem) {
        super(x, y, null);
        this.classItem = classItem;
        this.pack = pack;
        boolean moduleCategory = false;
        for (Category category : Category.VALUES) {
            if (classItem.getPackage().getAbsolutePath().equals("ipana.modules."+category.name().toLowerCase())) {
                moduleCategory = true;
                break;
            }
        }
        if (moduleCategory) {
            setElements(new Element[]{new Element("Delete"),new Element("Load"), new Element("Load as Module")});
        } else {
            setElements(new Element[]{new Element("Delete"),new Element("Load")});
        }
    }
    private ClassItem classItem;
    private Package pack;

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        float y = getY();
        for (Element element : getElements()) {
            if (isHovered(mouseX, mouseY, y, 10)) {
                RenderUtils.drawFixedRect(getX(),y,getX()+getWidth(),y+10, Color.pink);
            }
            FontHelper.SIZE_18.drawStringWithShadow(element.getName(),getX(),y+2,Color.white.getRGB());
            y+=10;
        }
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        if (button == 0) {
            double y = getY();
            for (Element element : getElements()) {
                if (isHovered(mouseX, mouseY, y, 10)) {
                    switch (element.getName()) {
                        case "Delete" -> {
                            this.pack.getClasses().remove(classItem);
                            for (Category category : Category.VALUES) {
                                if (classItem.getPackage().getAbsolutePath().equals("ipana.modules."+category.name().toLowerCase())) {
                                    Module module = ModuleManager.getModule(classItem.getName());
                                    if (module == null) {
                                        return;
                                    }
                                    if (module.isEnabled()) {
                                        module.toggle();
                                    }
                                    ModuleManager.getModuleList().remove(module);
                                    Ipana.newClickGui = new NewClickGui();
                                    break;
                                }
                            }
                        }
                        case "Load" -> {
                            File config = new File(mc.mcDataDir, "Ipana Config");
                            File addons = new File(config, "addons");

                            File bokPath = null;
                            for (String paths : classItem.getPackage().getAbsolutePath().split("\\.")) {
                                bokPath = new File(bokPath == null ? addons : bokPath, paths);
                            }
                            if (bokPath != null) {
                                if (!bokPath.exists()) {
                                    bokPath.mkdirs();
                                }
                                try {
                                    File javaFile = new File(bokPath, classItem.getName() + ".java");
                                    if (!javaFile.exists()) {
                                        javaFile.createNewFile();
                                    }
                                    FileUtils.write(javaFile, classItem.getCode().buildToList());
                                    JavaStringCompiler compiler = new JavaStringCompiler();
                                    Map<String, byte[]> results = compiler.compile(javaFile.getName(), classItem.getCode().buildToString());
                                    Class<Module> clazz = (Class<Module>) compiler.loadClass(classItem.getName(), results);
                                    //try to load class no need to use it
                                } catch (IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        case "Load as Module" -> {
                            File config = new File(mc.mcDataDir, "Ipana Config");
                            File addons = new File(config, "addons");
                            File modules = new File(addons, "modules");
                            try {
                                File javaFile = new File(modules, classItem.getName() + ".java");
                                if (!javaFile.exists()) {
                                    javaFile.createNewFile();
                                }
                                FileUtils.write(javaFile, classItem.getCode().buildToList());
                                JavaStringCompiler compiler = new JavaStringCompiler();
                                Map<String, byte[]> results = compiler.compile(javaFile.getName(), classItem.getCode().buildToString());
                                Class<Module> clazz = (Class<Module>) compiler.loadClass(classItem.getPackage().getAbsolutePath()+"."+classItem.getName(), results);
                                ModuleManager.getModuleList().removeIf(m -> m.getName().equals(classItem.getName()));
                                ModuleManager.getModuleList().add(clazz.newInstance());
                                Ipana.newClickGui = new NewClickGui();
                            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                y += 10;
            }
            setShouldRemove(true);
        }
    }

    @Override
    public void onPress(int key, char typedChar) {

    }

}
