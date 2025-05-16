package ipana.shell.cmd;

import ipana.shell.Shell;

public abstract class Cmd {
    private String[] names;
    private String desc;

    public Cmd(String[] names, String desc) {
        this.names = names;
        this.desc = desc;
    }
    public void printToShell(Shell shell,String string, Status status) {
        shell.printToShell(string, status);
    }

    public abstract void onCommand(Shell shell,String[] args);

    public String[] getNames() {
        return names;
    }

    public String getDesc() {
        return desc;
    }
}
