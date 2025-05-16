package ipana.shell.cmd.commands;

import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;

public class ClearCmd extends Cmd {
    public ClearCmd() {
        super(new String[]{"clear","clr"}, "Clear shell.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        shell.writtenList.clear();
        printToShell(shell,"Cleared shell.", Status.Info);
    }
}
