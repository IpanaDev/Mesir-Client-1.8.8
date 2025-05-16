package ipana.utils.config;

import ipana.managements.friend.Friend;
import ipana.managements.friend.FriendManager;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.value.Mode;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;
import ipana.managements.value.values.*;
import ipana.utils.file.FileUtils;
import net.minecraft.client.Minecraft;
import stelixobject.objectfile.SxfDataObject;
import stelixobject.objectfile.SxfFile;
import stelixobject.objectfile.reader.SXfReader;
import stelixobject.objectfile.writer.SxfWriter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigUtils {

    public static void loadModsAndVals() {
        loadModsAndVals(null);
    }

    public static void saveModsAndVals() {
        saveModsAndVals(null);
    }

    public static void loadModsAndVals(String directory) {
        Minecraft.setStatus("Config");
        for (Module mod : ModuleManager.getModuleList()) {
            System.out.println("Loading "+mod.getCategory().name().toLowerCase() + File.separator + mod.getName() + ".sxf");
            SxfFile file;
            if (directory != null) {
                file = SXfReader.Read(getConfigFile(directory + File.separator +mod.getCategory().name().toLowerCase() + File.separator + mod.getName() + ".sxf").getAbsolutePath());
            } else {
                file = SXfReader.Read(getConfigFile(mod.getCategory().name().toLowerCase() + File.separator + mod.getName() + ".sxf").getAbsolutePath());;
            }
            if (file == null) {
                continue;
            }
            if (file.base().size() == 0) {
                continue;
            }
            SxfDataObject object = file.base().get(mod.getName());
            boolean enabled = object.variable("Enabled");
            int key = object.variable("Key");
            if (mod.isEnabled() != enabled) {
                mod.toggle();
            }
            mod.setKey(key);
            Boolean b = object.variable("Visible");
            if (b != null) {
                mod.visible = b;
            }
            SxfDataObject valueData = object.object("values");
            for (Value<?> value : ValueManager.getValuesFromModule(mod)) {
                switch (value) {
                    case PositionValue p -> {
                        SxfDataObject positionData = valueData.object(p.getName());
                        if (positionData == null) {
                            continue;
                        }
                        p.setValue(new double[]{positionData.variable("X"), positionData.variable("Y")});
                    }
                    case ColorValue c -> {
                        SxfDataObject colorData = valueData.object(c.getName());
                        if (colorData == null) {
                            continue;
                        }
                        c.setValue(new Color(colorData.variable("Red"), (int) colorData.variable("Green"), colorData.variable("Blue")));
                    }
                    case EnumValue<? extends Enum<?>> e -> {
                        if (valueData.variable(value.getName()) != null) {
                            e.setValueFromString(valueData.variable(value.getName()));
                        }
                    }
                    case ModeValue<? extends Mode<?>> e -> {
                        if (valueData.variable(value.getName()) != null) {
                            e.setValueFromString(valueData.variable(value.getName()));
                        }
                    }
                    case NumberValue n -> {
                        if (valueData.variable(value.getName()) != null) {
                            Number o = valueData.variable(value.getName());
                            if (n.getIncrement() instanceof Double) {
                                n.setValue(o.doubleValue());
                            } else if (n.getIncrement() instanceof Float) {
                                n.setValue(o.floatValue());
                            } else if (n.getIncrement() instanceof Integer) {
                                n.setValue(o.intValue());
                            }
                        }
                    }
                    default -> {
                        if (valueData.variable(value.getName()) != null) {
                            value.setValue(valueData.variable(value.getName()));
                        }
                    }
                }
            }
        }
        ModuleManager.getModuleList().forEach(Module::onSuffixChange);
    }

    public static void saveModsAndVals(String directory) {
        if (directory != null) {
            FileUtils.getConfigDir(directory);
        }
        for (Module mod : ModuleManager.getModuleList()) {
            SxfFile file = new SxfFile();
            SxfDataObject data = new SxfDataObject();
            data.variables().put("Enabled", mod.isEnabled());
            data.variables().put("Key", mod.getKey());
            data.variables().put("Visible", mod.visible);
            SxfDataObject valueData = new SxfDataObject();
            List<Value<?>> list = ValueManager.getValuesFromModule(mod);
            for (Value<?> value : list) {
                if (value instanceof NumberValue v) {
                    if (v.getIncrement() instanceof Double) {
                        valueData.variables().put(value.getName(), v.getValue().doubleValue());
                    } else if (v.getIncrement() instanceof Float) {
                        valueData.variables().put(value.getName(), v.getValue().floatValue());
                    } else if (v.getIncrement() instanceof Integer) {
                        valueData.variables().put(value.getName(), v.getValue().intValue());
                    }
                } else if (value instanceof BoolValue || value instanceof TextValue) {
                    valueData.variables().put(value.getName(), value.getValue());
                } else if (value instanceof EnumValue<?> v) {
                    valueData.variables().put(v.getName(), v.getValue().name());
                } else if (value instanceof ModeValue<?> v) {
                    valueData.variables().put(v.getName(), v.getValue().getName());
                } else if (value instanceof PositionValue v) {
                    SxfDataObject data2 = new SxfDataObject();
                    data2.variables().put("X", v.getX());
                    data2.variables().put("Y", v.getY());
                    valueData.objects().put(v.getName(), data2);
                } else if (value instanceof ColorValue v) {
                    SxfDataObject data2 = new SxfDataObject();
                    data2.variables().put("Red", v.getValue().getRed());
                    data2.variables().put("Green", v.getValue().getGreen());
                    data2.variables().put("Blue", v.getValue().getBlue());
                    valueData.objects().put(v.getName(), data2);
                }
            }
            data.objects().put("values", valueData);
            file.base().put(mod.getName(), data);
            SxfWriter writer = new SxfWriter(file);
            if (directory != null) {
                writer.write(getConfigFile(directory + File.separator + mod.getCategory().name().toLowerCase() + File.separator + mod.getName() + ".sxf").getAbsolutePath());
            } else {
                writer.write(getConfigFile(mod.getCategory().name().toLowerCase() + File.separator + mod.getName() + ".sxf").getAbsolutePath());
            }
        }
    }

    public static void saveFriends() {
        SxfFile file = new SxfFile();
        SxfDataObject data = new SxfDataObject();
        for (Friend friend : FriendManager.getFriends()) {
            data.variables().put(friend.name, friend.name);
        }
        file.base().put("Friends", data);
        new SxfWriter(file).write(getConfigFile("Friends.sxf").getAbsolutePath());
    }

    public static void loadFriends() {
        SxfFile file = SXfReader.Read(getConfigFile("Friends.sxf").getAbsolutePath());
        if (file.base().size() == 0) return;
        SxfDataObject object = file.base().get("Friends");
        for (Object data : object.variables().values()) {
            FriendManager.add(String.valueOf(data));
        }
    }

    public static File getConfigFile(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        File file2 = new File(mc.mcDataDir, "Ipana Config");
        if (!file2.exists()) {
            file2.mkdir();
        }
        File file = new File(file2,name);
        if (!file.exists()) {
            File parent = file.getParentFile();
            while (!parent.exists()) {
                parent.mkdir();
                parent = file.getParentFile();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
