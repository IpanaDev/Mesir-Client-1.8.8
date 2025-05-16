package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.Condition;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;

import java.lang.reflect.Field;

public class EnumValue<E extends Enum<E>> extends Value<E> {
    private E currentEnum;
    private E[] enums;
    private int index;

    public EnumValue(String name, Module module, Class<E> enumClass, String description) {
        super(name, module, description);
        this.enums = (E[]) new Enum[enumClass.getEnumConstants().length];
        for (E enumM : enumClass.getEnumConstants()) {
            enums[enumM.ordinal()] = enumM;
        }
        currentEnum = enums[0];
        index = 0;
        ValueManager.addToList(this);
    }

    public EnumValue(String name, Module module, Class<E> enumClass, String description, Condition condition) {
        this(name, module, enumClass, description);
        this.setCondition(condition);
    }

    @Override
    public E getValue() {
        return currentEnum;
    }

    @Override
    public void setValue(E newValue) {
        currentEnum = newValue;
        getModule().onSuffixChange();
    }

    public void setValueFromString(String name) {
        for (int i = 0; i < enums.length; i++) {
            E e = enums[i];
            try {
                Field field = e.getClass().getDeclaredField("enumName");
                String enumName = String.valueOf(field.get(e));
                if (enumName.equalsIgnoreCase(name)) {
                    currentEnum = e;
                    index = i;
                    return;
                }
            } catch (NoSuchFieldException ignored) {
                if (e.name().equalsIgnoreCase(name)) {
                    currentEnum = e;
                    index = i;
                    return;
                }
            } catch (IllegalAccessException ignored) {

            }
        }
    }

    public void setNext() {
        index++;
        if (index >= enums.length) {
            index = 0;
        }
        currentEnum = enums[index];
    }

    public void setPrevious() {
        index--;
        if (index < 0) {
            index = enums.length-1;
        }
        currentEnum = enums[index];
    }

    @Override
    public Type getType() {
        return Type.ENUM;
    }
}
