package ipana.shell.cmd;

public class Written {
    private boolean sentByShell;
    private String string;

    public Written(String string,boolean sentByShell) {
        this.string = string;
        this.sentByShell = sentByShell;
    }

    public boolean isSentByShell() {
        return sentByShell;
    }

    public String getString() {
        return string;
    }
}
