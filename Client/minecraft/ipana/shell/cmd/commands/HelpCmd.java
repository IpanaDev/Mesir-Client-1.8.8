package ipana.shell.cmd.commands;

import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.CmdManager;
import ipana.shell.cmd.Status;

public class HelpCmd extends Cmd {
    public HelpCmd() {
        super(new String[]{"help"},"List of commands.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        for (Cmd cmd : CmdManager.getList()) {
            printToShell(shell,cmd.getNames()[0]+" : "+cmd.getDesc(), Status.Info);
        }
    }
}
