package ipana.shell.cmd.commands;

import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;
import ipana.managements.value.values.*;
import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;

import java.lang.reflect.Field;

public class ValueCmd extends Cmd {
    public ValueCmd() {
        super(new String[]{"value","val","v"},"Change the value of a module.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        if (args.length == 4) {
            Module module = ModuleManager.getModule(args[1]);
            Value<?> value = ValueManager.getValueByName(args[2],module);
            if (module != null && value != null) {
                switch (value.getType()) {
                    case NUMBER -> {
                        NumberValue<?> numberValue = (NumberValue<?>) value;
                        if (numberValue.getValue() instanceof Double) {
                            ((NumberValue<Double>) numberValue).setValue(Double.parseDouble(args[3]));
                        } else if (numberValue.getValue() instanceof Float) {
                            ((NumberValue<Float>) numberValue).setValue(Float.parseFloat(args[3]));
                        } else if (numberValue.getValue() instanceof Integer) {
                            ((NumberValue<Integer>) numberValue).setValue(Integer.parseInt(args[3]));
                        }
                        printToShell(shell, module.getName() + "'s " + numberValue.getName() + " changed to " + numberValue.getValue(), Status.Success);
                    }
                    case BOOLEAN -> {
                        BoolValue boolValue = (BoolValue) value;
                        boolValue.setValue(Boolean.parseBoolean(args[3]));
                        String colored = boolValue.getValue() ? "§a true" : "§c false";
                        printToShell(shell, module.getName() + "'s " + boolValue.getName() + " changed to" + colored, Status.Success);
                    }
                    case TEXT -> {
                        TextValue textValue = (TextValue) value;
                        textValue.setValue(args[3]);
                        printToShell(shell, module.getName() + "'s " + textValue.getName() + " changed to " + textValue.getValue(), Status.Success);
                    }
                    case MODE -> {
                        ModeValue<?> modeValue = (ModeValue<?>) value;
                        modeValue.setValueFromString(args[3]);
                        printToShell(shell, module.getName() + "'s " + modeValue.getName() + " changed to " + modeValue.getValue().getName(), Status.Success);
                    }
                    case ENUM -> {
                        EnumValue<?> modeValue = (EnumValue<?>) value;
                        modeValue.setValueFromString(args[3]);
                        String bok = "abicim ne yazdin inan bilmiyom";
                        try {
                            Field field = modeValue.getValue().getClass().getDeclaredField("enumName");
                            bok = String.valueOf(field.get(modeValue.getValue()));
                        } catch (NoSuchFieldException ignored) {
                            bok = modeValue.getValue().name();
                        } catch (IllegalAccessException ignored) {

                        }
                        printToShell(shell, module.getName() + "'s " + modeValue.getName() + " changed to " + bok, Status.Success);
                    }
                }
            } else {
                printToShell(shell,"Wrong module name or value name!", Status.Error);
            }
        } else {
            printToShell(shell,"Wrong usage (value <module> <value name> <new value>)!", Status.Error);
        }
    }
}
