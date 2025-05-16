package ipana.shell.cmd.commands;

import ipana.shell.Shell;
import ipana.shell.ShellValues;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;

public class ThemeCmd extends Cmd {
    public ThemeCmd() {
        super(new String[]{"theme"},"Change the theme of shell.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        if (args.length == 2) {
            ShellValues.reload(args[1]);
            printToShell(shell,"Theme loaded!", Status.Success);
        } else {
            printToShell(shell,"Wrong usage (theme name)!", Status.Error);
        }
    }
}
