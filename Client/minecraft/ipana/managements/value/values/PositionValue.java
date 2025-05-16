package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.Condition;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;
import net.minecraft.client.gui.ScaledResolution;

public class PositionValue extends Value<double[]> {
    private double[] position;

    public PositionValue(String name, Module module, double x, double y, String description) {
        super(name, module, description);
        this.position = new double[]{x,y};
        ValueManager.addToList(this);
    }
    public PositionValue(String name, Module module, double x, double y, String description, Condition condition) {
        this(name, module, x, y, description);
        this.setCondition(condition);
    }

    @Override
    public double[] getValue() {
        return position;
    }

    @Override
    public void setValue(double[] newValue) {
        this.position = newValue;
        getModule().onSuffixChange();
    }

    @Override
    public Type getType() {
        return Type.POSITION;
    }

    public double[] getPosition(ScaledResolution sr) {
        return new double[]{sr.getScaledWidth()/100f*position[0],sr.getScaledHeight()/100f*position[1]};
    }

    public void setX(double x) {
        position[0] = x;
    }
    public void setY(double y) {
        position[1] = y;
    }
    public double getY() {
        return position[1];
    }
    public double getX() {
        return position[0];
    }
}
