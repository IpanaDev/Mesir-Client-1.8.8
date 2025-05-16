package ipana.shell.cmd.commands;

import ipana.Ipana;
import ipana.irc.IRC;
import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ConnectCmd extends Cmd {
    public ConnectCmd() {
        super(new String[]{"connect"},"connect to an irc.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        if (args.length == 2) {
            String channel = args[1];
            if (channel.length() > 0) {
                try {
                    IRC irc = new IRC(channel);
                    irc.startIRC(Ipana.mainIRC().getName());
                    Ipana.connectedIRCs().add(irc);
                    printToShell(shell,"Successfully connected to #"+channel+"!", Status.Success);
                } catch (IOException | NoSuchAlgorithmException e) {
                    printToShell(shell,"An exception caught!", Status.Error);
                    e.printStackTrace();
                }
            }
        } else {
            printToShell(shell,"Wrong usage (connect channelName)!", Status.Error);
        }
    }
}