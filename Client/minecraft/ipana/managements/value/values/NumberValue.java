package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.Condition;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;
import ipana.utils.math.MathUtils;

import java.util.function.Consumer;

public class NumberValue<T extends Number> extends Value<T> {
    private T number, increment, min, max;

    public NumberValue(String name, Module module, T value, T min, T max, T increment, String description) {
        super(name, module, description);
        this.number = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
        ValueManager.addToList(this);
    }
    public NumberValue(String name, Module module, T value, T min, T max, T increment, String description, Condition condition) {
        this(name, module, value, min, max, increment, description);
        this.setCondition(condition);
    }
    public T getIncrement() {
        return increment;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    @Override
    public T getValue() {
        return number;
    }

    @Override
    public void setValue(T newValue) {
        number = newValue;
        getModule().onSuffixChange();
    }

    @Override
    public Type getType() {
        return Type.NUMBER;
    }
}
