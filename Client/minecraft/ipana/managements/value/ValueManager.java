package ipana.managements.value;

import ipana.managements.module.Module;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ValueManager {
    private static List<Value<?>> valueList = new ArrayList<>();

    public ValueManager() {
        Minecraft.setStatus("Value Manager");
        System.out.println("Loaded "+ valueList.size() + " values.");
    }

    public static List<Value<?>> getValuesFromModule(Module module) {
        List<Value<?>> values = new ArrayList<>();
        for (Value<?> val : getValueList()) {
            if (val.getModule().equals(module)) {
                values.add(val);
            }
        }
        return values;
    }

    public static Value<?> getValueByName(String name,Module m) {
        Value<?> value = null;
        for (Value<?> val : getValueList()) {
            if (val.getName().equalsIgnoreCase(name) && val.getModule() == m) {
                value = val;
            }
        }
        return value;
    }

    public static void addToList(Value<?> value) {
        valueList.add(value);
    }

    public static List<Value<?>> getValueList() {
        return valueList;
    }
}
