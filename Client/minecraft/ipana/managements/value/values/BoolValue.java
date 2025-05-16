package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.Condition;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;

public class BoolValue extends Value<Boolean> {
    private boolean enabled;

    public BoolValue(String name, Module module, boolean bool, String description) {
        super(name, module, description);
        this.enabled = bool;
        ValueManager.addToList(this);
    }
    public BoolValue(String name, Module module, boolean bool, String description, Condition condition) {
        this(name, module, bool, description);
        this.setCondition(condition);
    }
    public void toggle() {
        enabled = !enabled;
    }

    @Override
    public Boolean getValue() {
        return enabled;
    }

    @Override
    public void setValue(Boolean newValue) {
        enabled = newValue;
        getModule().onSuffixChange();
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }
}
