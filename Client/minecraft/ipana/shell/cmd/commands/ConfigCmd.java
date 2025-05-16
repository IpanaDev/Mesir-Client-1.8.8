package ipana.shell.cmd.commands;

import ipana.shell.Shell;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.Status;
import ipana.utils.config.ConfigUtils;
import ipana.utils.file.FileUtils;

import java.io.File;

public class ConfigCmd extends Cmd {
    public ConfigCmd() {
        super(new String[]{"config"}, "Auto configuration.");
    }

    @Override
    public void onCommand(Shell shell, String[] args) {
        if (args.length == 3) {
            String configName = args[2];
            if (configName.length() > 0) {
                if (args[1].equalsIgnoreCase("load")) {
                    File configFile = FileUtils.getConfigDir("configs\\"+configName, false);
                    if (!configFile.exists()) {
                        printToShell(shell,"A config named as "+configName+" not found.", Status.Error);
                    } else {
                        ConfigUtils.loadModsAndVals("configs\\"+configName);
                        ConfigUtils.saveModsAndVals();
                        printToShell(shell,"Config "+configName+" loaded.", Status.Success);
                    }
                } else if (args[1].equalsIgnoreCase("save")) {
                    ConfigUtils.saveModsAndVals("configs\\"+configName);
                    printToShell(shell,"Saved "+configName+" as config.", Status.Success);
                }
            } else {
                printToShell(shell,"Salak orospu evladı isimsiz config nasıl oluşturcan!", Status.Error);
            }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
            printToShell(shell, "List of configs:", Status.Info);
            File configFile = FileUtils.getConfigDir("configs", true);
            for (File file : configFile.listFiles()) {
                printToShell(shell, "- " + file.getName(), Status.Info);
            }
        } else {
            printToShell(shell,"Wrong usage (config load/save/list configName)!", Status.Error);
        }
    }
}
