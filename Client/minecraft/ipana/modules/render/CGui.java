package ipana.modules.render;

import ipana.Ipana;
import ipana.clickgui.autistic.panels.CategoryPanel;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.ColorValue;
import ipana.utils.config.ConfigUtils;
import org.lwjgl.input.Keyboard;
import stelixobject.objectfile.SxfDataObject;
import stelixobject.objectfile.SxfFile;
import stelixobject.objectfile.reader.SXfReader;
import stelixobject.objectfile.writer.SxfWriter;

import java.awt.*;

public class CGui extends Module {
    public BoolValue anim = new BoolValue("Anim",this,false,"ClickGui animation (Ipana).");
    public BoolValue darkTheme = new BoolValue("DarkTheme",this,false,"Dark theme (Ipana).");
    public ColorValue color = new ColorValue("Color",this, Color.cyan,"Click Gui color.");
    public CGui() {
        super("ClickGui", Keyboard.KEY_RSHIFT,Category.Render,"Shows modules and values.");
    }


    @Override
    public void onEnable() {
        if (Ipana.newClickGui == null) {
            setEnabled(false);
            return;
        }
        for (CategoryPanel categoryPanel : Ipana.newClickGui.categoryPanels) {
            categoryPanel.transparent = categoryPanel.prevTransparent = 0;
        }
        mc.displayGuiScreen(Ipana.newClickGui);
        loadNew();
        ConfigUtils.saveModsAndVals();
        setEnabled(false);
    }

    public static void loadNew() {
        SxfFile file = SXfReader.Read(ConfigUtils.getConfigFile("NewClickGui").getAbsolutePath());
        if (file.base().size() == 0) return;
        for (CategoryPanel panel : Ipana.newClickGui.categoryPanels) {
            String data = file.get("NewClickGui").variable(panel.category.name());
            String[] split = data.split("-");
            panel.x = Float.parseFloat(split[0]);
            panel.y = Float.parseFloat(split[1]);
        }
    }
    public static void saveNew() {
        SxfFile file = new SxfFile();
        SxfDataObject data = new SxfDataObject();
        for (final CategoryPanel cat : Ipana.newClickGui.categoryPanels) {
            data.variables().put(cat.category.name(),String.format("%s-%s",cat.x,cat.y));
        }
        file.base().put("NewClickGui",data);
        new SxfWriter(file).write(ConfigUtils.getConfigFile("NewClickGui").getAbsolutePath());
    }
}
