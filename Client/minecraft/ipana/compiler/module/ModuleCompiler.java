/**
 * @author Ipana
 */

package ipana.compiler.module;

import ipana.compiler.JavaStringCompiler;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ModuleCompiler {
    private Minecraft mc = Minecraft.getMinecraft();
    private ArrayList<Module>[] addonsToAdd = new ArrayList[5];

    public ModuleCompiler() {
        for (int i = 0; i < addonsToAdd.length; i++) {
            addonsToAdd[i] = new ArrayList<>();
        }
        loadAddons();
    }

    public ArrayList<Module>[] addonsToAdd() {
        return addonsToAdd;
    }

    private void loadAddons() {
        JavaStringCompiler compiler = new JavaStringCompiler();
        File config = new File(mc.mcDataDir, "Ipana Config");
        File addons = new File(config, "addons");
        File modules = new File(addons, "modules");
        if (!addons.exists()) {
            addons.mkdir();
        }
        if (!modules.exists()) {
            modules.mkdir();
        }
        if (modules.listFiles() == null) {
            System.out.println("Couldn't load modules from directory.");
            return;
        }
        Arrays.stream(modules.listFiles()).filter(f -> f.getName().endsWith(".java")).forEach(f -> {
            try {
                ClassProperty build = readClass(f);
                Map<String, byte[]> results = compiler.compile(f.getName(), build.builder.toString());
                //TODO: WHY RESULTS + BOK CHECK PLS FIX THX
                //if (results.size() == 1 && results.containsKey(build.className)) {
                    Class<Module> clazz = (Class<Module>) compiler.loadClass(build.className, results);
                    Module newModule = clazz.newInstance();
                    addonsToAdd[newModule.getCategory().ordinal()].add(newModule);
                    ModuleManager.getModuleList().add(newModule);
                //}
            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public ClassProperty readClass(File inputFile) {
        StringBuilder builder = new StringBuilder();
        String packageName = "";
        String className = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                String space = str.replace(" ","");
                if (space.startsWith("package")) {
                    packageName = space.replace("package","").replace(";","");
                }
                if (space.startsWith("publicclass") && space.endsWith("extendsModule{")) {
                    className = packageName+"."+space.replace("publicclass","").replace("extendsModule{","");
                }
                builder.append(str);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ClassProperty(className, builder);
    }

    class ClassProperty {
        String className;
        StringBuilder builder;

        public ClassProperty(String className, StringBuilder builder) {
            this.className = className;
            this.builder = builder;
        }
    }
}
