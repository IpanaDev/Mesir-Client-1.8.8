package ipana.shell.cmd.commands;

import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;

public class VisibleCmd extends Cmd {
    public VisibleCmd() {
        super(new String[]{"visible","show"}, "Show modules.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        if (args.length == 2) {
            Module m = ModuleManager.getModule(args[1]);
            if (m != null) {
                m.visible = !m.visible;
                printToShell(shell, m.getName()+"'s visibility changed to: "+m.visible, Status.Success);
            } else {
                printToShell(shell, "Wrong module name", Status.Error);
            }
        } else {
            printToShell(shell, "Wrong usage <visible moduleName>", Status.Error);
        }
    }
}
