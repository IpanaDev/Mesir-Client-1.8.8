package ipana.managements.value.values;

import ipana.managements.module.Module;
import ipana.managements.value.Condition;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;

public class TextValue extends Value<String> {
    private String text;

    public TextValue(String name, Module module, String text, String description) {
        super(name, module, description);
        this.text = text;
        ValueManager.addToList(this);
    }
    public TextValue(String name, Module module, String text, String description, Condition condition) {
        this(name, module, text, description);
        this.setCondition(condition);
    }

    @Override
    public String getValue() {
        return text;
    }

    @Override
    public void setValue(String newValue) {
        text = newValue;
        getModule().onSuffixChange();
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
