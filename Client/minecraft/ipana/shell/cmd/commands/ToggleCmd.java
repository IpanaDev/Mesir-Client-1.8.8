package ipana.shell.cmd.commands;


import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;

public class ToggleCmd extends Cmd {
    public ToggleCmd() {
        super(new String[]{"toggle","tog","t"},"Toggle on or off a module.");
    }

    @Override
    public void onCommand(Shell shell,String[] args) {
        if (args.length == 2) {
            Module module = ModuleManager.getModule(args[1]);
            if (module != null) {
                module.toggle();
                printToShell(shell,module.getName()+" toggled to "+(module.isEnabled() ? "§2on" : "§coff")+".", Status.Success);
            } else {
                printToShell(shell,"Wrong module name!", Status.Error);
            }
        } else {
            printToShell(shell,"Wrong usage (toggle moduleName)!", Status.Error);
        }
    }
}
