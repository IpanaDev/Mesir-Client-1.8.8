package ipana.managements.value;

import ipana.managements.module.Module;
import ipana.managements.value.values.ModeValue;
import net.minecraft.client.gui.GuiTextField;

public abstract class Value<T> {
    private String name, description;
    private Module module;
    private Condition condition;

    public Value(String name, Module module, String description) {
        this.name = name;
        this.description = description;
        this.module = module;
    }
    public Value(String name, Module module, String description, Condition condition) {
        this(name, module, description);
        this.condition = condition;
    }
    public abstract T getValue();

    public abstract void setValue(T newValue);

    public boolean isOpen() {
        return condition == null || condition.accept();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public abstract Type getType();

}
