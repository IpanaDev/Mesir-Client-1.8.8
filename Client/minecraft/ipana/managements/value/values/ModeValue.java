package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ModeValue<K extends Mode<? extends Module>> extends Value<K> {
    private K currentMode;
    private K[] modes;
    private int index;

    @SafeVarargs
    public ModeValue(String name, Module module, String description, Class<? extends K>... classes) {
        super(name, module, description);
        try {
            index = 0;
            modes = (K[]) new Mode[classes.length];
            Class<? extends Module> trollModule = module.getClass();
            for (int i = 0; i < classes.length; i++) {
                K newInstance = classes[i].getConstructor(trollModule).newInstance(module);
                modes[i] = newInstance;
            }
            currentMode = modes[0];
            ValueManager.addToList(this);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public ModeValue(String name, Module module, String description, Condition condition, Class<? extends K>... classes) {
        this(name, module, description, classes);
        this.setCondition(condition);
    }

    @Override
    public K getValue() {
        return currentMode;
    }

    @Override
    public void setValue(K newValue) {
        currentMode = newValue;
        getModule().onSuffixChange();
    }

    @Override
    public Type getType() {
        return Type.MODE;
    }

    public void setValueFromString(String name) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].getName().equals(name)) {
                currentMode = modes[i];
                index = i;
                return;
            }
        }
    }

    public void setNext() {
        index++;
        if (index >= modes.length) {
            index = 0;
        }
        currentMode = modes[index];
    }

    public void setPrevious() {
        index--;
        if (index < 0) {
            index = modes.length-1;
        }
        currentMode = modes[index];
    }
}
