package ipana;

import ipana.clickgui.autistic.NewClickGui;
import ipana.irc.IRC;
import ipana.irc.NativeIRC;
import ipana.irc.user.UsersGui;
import ipana.managements.module.ModuleManager;
import ipana.managements.module.Modules;
import ipana.managements.value.ValueManager;
import ipana.modules.render.CGui;
import ipana.renders.account.AccountManager;
import ipana.renders.ide.IDEScreen;
import ipana.renders.ingame.EmoteGui;
import ipana.renders.ChangelogUI;
import ipana.renders.ingame.cosmetics.CosmeticsGui;
import ipana.renders.settings.nbt.NbtGui;
import ipana.shell.Shell;
import ipana.utils.async.Async;
import ipana.utils.config.ConfigUtils;
import ipana.utils.font.FontHelper;
import ipana.utils.gamepad.GamePad;
import ipana.utils.gamepad.GamePadManager;
import ipana.utils.math.MathUtils;
import ipana.utils.music.Musics;
import ipana.utils.ncp.handler.NCP3_11_1Handler;
import ipana.utils.net.Pinger;
import ipana.utils.render.ColorUtil;
import ipana.utils.render.DynamicTextureManager;
import ipana.utils.render.EmoteUtils;
import ipana.utils.voice.Voice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class Ipana {

    public final static String name = "Ipana";
    public final static String clientName = "Mesir";
    public final static String version = "2.1";
    public static NewClickGui newClickGui;
    public static ChangelogUI changelogUI;
    public static AccountManager accountManager;
    public static EmoteGui emoteGUI;
    public static CosmeticsGui cosmeticsGUI;
    public static NbtGui nbtGui;
    public static Voice voice;
    public static Shell shell;
    public static DynamicTextureManager dynamicTextureManager;
    @Deprecated
    public static NCP3_11_1Handler ncp3_11_1Handler;
    //public static AntiCheatGui antiCheatGui;
    private static IRC mainIRC;
    private static ArrayList<IRC> connectedIRCs = new ArrayList<>();

    public static boolean play = true;

    public static int background = 9;
    private static int r,g,b;
    private static Color color;
    private static UsersGui usersGui;
    public static GuiMainMenu.Theme mainMenuTheme;
    private static boolean processNetEvents = true;

    public static Color getClientColor() {
        CGui cGui = Modules.CLICK_GUI;
        if (r != cGui.color.getValue().getRed() || g != cGui.color.getValue().getGreen() || b != cGui.color.getValue().getBlue()) {
            r = cGui.color.getValue().getRed();
            g = cGui.color.getValue().getGreen();
            b = cGui.color.getValue().getBlue();
            Modules.HUD.markArraylistDirty();
            color = new Color(r,g,b);
        }
        return color;
    } 

    public static void startToothpastes() {
        //mainMenuTheme = GuiMainMenu.Theme.VALUES[MathUtils.random(0, GuiMainMenu.Theme.VALUES.length)];
        mainMenuTheme = GuiMainMenu.Theme.PORTAL;
        System.out.println(Minecraft.getMinecraft().mcDataDir.getAbsolutePath());
        try {
            System.out.println("Current HWID: "+getHWID());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        dynamicTextureManager = new DynamicTextureManager("mesir");
        if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            Minecraft.setStatus("Game pads");
            GamePadManager.initGamePads();
        }
        FontHelper.setupFont();
        ColorUtil.INSTANCE.initColorTables();
        //voice = new Voice();
        new ModuleManager();
        new ValueManager();
        ConfigUtils.loadModsAndVals();
        ConfigUtils.loadFriends();
        Minecraft.setStatus("Guis");
        newClickGui = new NewClickGui();
        changelogUI = new ChangelogUI();
        shell = new Shell(name,Minecraft.getMinecraft().session.getUsername(),20,20);
        EmoteUtils.init();
        accountManager = new AccountManager();
        nbtGui = new NbtGui();
        IDEScreen.INSTANCE.setupIDE();
        Minecraft.setStatus("IRC");
        try {
            NativeIRC.connect(Minecraft.getMinecraft().getSession().getUsername());
            mainIRC = new IRC("konyacraft2173");
            mainIRC.startIRC();
            usersGui = new UsersGui(mainIRC);
            connectedIRCs.add(mainIRC);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //ncp3_11_1Handler = new NCP3_11_1Handler();
        //antiCheatGui = new AntiCheatGui();
        Musics.init();
        Pinger.setEnabled(true);
        //Minecraft.setStatus("Senors");
        //ClientSensor.startAtatrk();
        if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            System.out.println("Width : " + Toolkit.getDefaultToolkit().getScreenSize().getWidth());
            System.out.println("Height : " + Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        }
        System.out.println("Toothpaste is active.");
    }

    public static void addNewChat(GuiNewChat guiNewChat) {
        Minecraft.getMinecraft().ingameGUI.addNewChat(guiNewChat);
    }

    public static String getHWID() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder s = new StringBuilder();
        final String main = System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("COMPUTERNAME") + System.getProperty("user.name").trim();
        final byte[] bytes = main.getBytes(StandardCharsets.UTF_8);
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        final byte[] md5 = messageDigest.digest(bytes);
        int i = 0;
        for (final byte b : md5) {
            s.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
            if (i != md5.length - 1) {
                s.append("-");
            }
            i++;
        }
        return s.toString();
    }
    public static void error(String msg,String title) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    public static void warn(String msg,String title) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
    }
    public static GamePad getPad() {
        return GamePadManager.getPads().get(0);
    }

    public static IRC mainIRC() {
        return mainIRC;
    }

    public static ArrayList<IRC> connectedIRCs() {
        return connectedIRCs;
    }

    public static UsersGui usersGui() {
        return usersGui;
    }

    public static boolean isProcessNetEvents() {
        return processNetEvents;
    }
    public static void setProcessNetEvents(boolean process) {
        processNetEvents = process;
    }

    public static void getMem(String description) {
        System.out.println(STR."\{description} -> \{(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1_000_000_000D} GB");
    }
}
