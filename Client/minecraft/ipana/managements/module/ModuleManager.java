package ipana.managements.module;

import ipana.compiler.module.ModuleCompiler;
import ipana.modules.combat.*;
import ipana.modules.exploit.*;
import ipana.modules.movement.FastLadder;
import ipana.modules.movement.NoFall;
import ipana.modules.player.*;
import ipana.modules.render.TileESP;
import ipana.modules.render.Freecam;
import ipana.modules.render.TailEffect;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

import static ipana.managements.module.Modules.*;

public class ModuleManager {
    private static List<Module> moduleList = new ArrayList<>();
    private static ModuleCompiler compiler;

    public ModuleManager() {
        Minecraft.setStatus("Module Manager");
        addToList(KILL_AURA);
        addToList(NO_SLOW_DOWN);
        addToList(SPRINT);
        addToList(CAMERA);
        //addToList(new AutoMute());
        addToList(AUTO_TARGET);
        addToList(new NoFall());
        addToList(new FastBow());
        addToList(AUTO_CLAIM);
        addToList(new GodMode());
        addToList(NUKER);
        addToList(new AutoCode());
        addToList(new Freecam());
        addToList(INV_MOVE);
        addToList(X_RAY);
        //addToList(new Sneak());
        addToList(new TpAura());
        addToList(BLOCK_ANIM);
        addToList(INV_HELPER);
        addToList(COOL_PERSPECTIVE);
        addToList(CRITICALS);
        //addToList(new AutoMine());
        addToList(NO_ROTATE);
        addToList(QUICK_USE);
        addToList(FLY);
        addToList(STEP);
        addToList(CHEST_STEALER);
        addToList(NO_HURT);
        addToList(WIDE);
        addToList(new TileESP());
        addToList(HUD);
        addToList(SCAFFOLD);
        addToList(new MCF());
        addToList(TELEPORT);
        addToList(AUTO_DRINK);
        addToList(PHASE);
        addToList(new Blink());
        addToList(WATER_WALK);
        addToList(new PingSpoof());
        addToList(NAME_TAGS);
        addToList(new ArmorSwap());
        addToList(AUTO_POT);
        addToList(SPEED);
        addToList(NO_BOB);
        addToList(BRIGHTNESS);
        addToList(ANTI_KB);
        addToList(new TailEffect());

        addToList(CLICK_GUI);
        addToList(VIEW_CLIP);
        addToList(new Regen());
        addToList(new FastLadder());
        addToList(new AutoFish());
        addToList(new AngleFlagger());
        addToList(new AutoCocoa());
        addToList(LESS_PACKETS);
        addToList(WORLD_DUMPER);
        addToList(FAST_BREAK);
        addToList(new BowAimbot());
        compiler = new ModuleCompiler();

        //addToList(PLAYER_ESP);
        System.out.println("Loaded " + moduleList.size() + " modules.");
    }

    public static ModuleCompiler compiler() {
        return compiler;
    }

    private void addToList(Module m) {
        moduleList.add(m);
    }

    public static List<Module> getModulesFromCategory(Category category) {
        List<Module> gament = new ArrayList<>();
        for (Module module : moduleList) {
            if (module.getCategory() == category) {
                gament.add(module);
            }
        }
        return gament;
    }

    public static Module getModule(String name) {
        final Module[] m = {null};
        getModuleList().stream().filter(mod -> mod.getName().equalsIgnoreCase(name)).forEach(mod -> m[0] = mod);
        return m[0];
    }

    public static Module getModule(Class<? extends Module> clazz) {
        final Module[] m = {null};
        getModuleList().stream().filter(mod -> mod.getClass().equals(clazz)).forEach(mod -> m[0] = mod);
        return m[0];
    }

    public static List<Module> getModuleList() {
        return moduleList;
    }

}
