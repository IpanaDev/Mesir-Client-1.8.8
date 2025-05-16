package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.Condition;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;
import ipana.renders.ingame.cosmetics.ColorPicker;

import java.awt.*;

public class ColorValue extends Value<Color> {
    private Color color;
    private ColorPicker picker;

    public ColorValue(String name, Module module, Color color, String description) {
        super(name, module, description);
        this.color = color;
        picker = new ColorPicker(colorPicker -> this.color = colorPicker.currentColor, this.color.getRGB());
        ValueManager.addToList(this);
    }
    public ColorValue(String name, Module module, Color color, String description, Condition condition) {
        this(name, module, color, description);
        this.setCondition(condition);
    }
    @Override
    public Color getValue() {
        return color;
    }

    @Override
    public void setValue(Color newValue) {
        color = newValue;
        picker.color = newValue.getRGB();
        picker.hex = Integer.toHexString(newValue.getRGB()).substring(2);
        getModule().onSuffixChange();
    }

    @Override
    public Type getType() {
        return Type.COLOR;
    }

    public ColorPicker picker() {
        return picker;
    }
}