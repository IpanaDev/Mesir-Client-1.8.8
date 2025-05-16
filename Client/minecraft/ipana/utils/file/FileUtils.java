package ipana.utils.file;

import ipana.managements.module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class FileUtils
{

    public static void write(File outputFile, List<String> writeContent) {
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
            for (String line : writeContent) {
                out.write(line + System.getProperty("line.separator"));
            }
            out.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }


    public static ArrayList<String> read(File inputFile) {
        ArrayList<String> readContent = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                readContent.add(str);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readContent;
    }
    public static boolean hasTempPermission() {
        if (System.getSecurityManager() == null) {
            return true;
        }
        File f;
        boolean hasPerm = false;
        try {
            f = Files.createTempFile("+~JT", ".tmp").toFile();
            f.delete();
            hasPerm = true;
        } catch (Throwable t) {
            /* inc. any kind of SecurityException */
        }
        return hasPerm;
    }
    public static File getConfigDir() {
        File file = new File(Minecraft.getMinecraft().mcDataDir, "Ipana Config");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
    public static File getConfigDir(String path) {
        return getConfigDir(path, true);
    }

    public static File getConfigDir(String path, boolean create) {
        File file = new File(Minecraft.getMinecraft().mcDataDir, "Ipana Config\\"+path);
        if (create && !file.exists()) {
            file.mkdir();
        }
        return file;
    }
    public static ResourceLocation fileToResource(File file) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (img != null) {
            return Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation(file.getName(), new DynamicTexture(img));
        }
        return null;
    }
    public static ResourceLocation fileToResource(String name, BufferedImage img) {
        if (img != null) {
            return Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation(name, new DynamicTexture(img));
        }
        return null;
    }
    public static File getConfigFile(String name) {
        File file = new File(getConfigDir(), String.format("%s.txt", name));
        if (!file.exists()) {
            try
            {
                file.createNewFile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return file;
    }
}
