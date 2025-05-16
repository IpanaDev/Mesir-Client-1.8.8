package ipana.shell.cmd;

import ipana.shell.cmd.commands.*;

import java.util.ArrayList;
import java.util.List;

public class CmdManager {
    private static List<Cmd> list = new ArrayList<>();

    public CmdManager() {
        list.add(new ToggleCmd());
        list.add(new ClearCmd());
        list.add(new ThemeCmd());
        list.add(new ValueCmd());
        list.add(new HelpCmd());
        list.add(new ConfigCmd());
        list.add(new VisibleCmd());
        list.add(new ConnectCmd());
    }

    public static List<Cmd> getList() {
        return list;
    }
}
