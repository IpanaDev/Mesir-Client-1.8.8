package ipana.utils.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class DynamicTextureManager {
    protected final Map<String, DynamicTexture> resourceLocations = new HashMap<>();
    private final String resourceName;
    public static final ResourceLocation allah = new ResourceLocation("mesir/void.png");
    private Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static int[] preTextures = new int[200];
    public static int index;

    public DynamicTextureManager(final String resourceName) {
        this.resourceName = resourceName;
        for(int i = 0; i < preTextures.length; i++){
            preTextures[i] = GL11.glGenTextures();
        }
        index = 0;
    }

    public ResourceLocation getHeadTexture(final GameProfile gameProfile) { return this.getTexture(gameProfile.getId().toString(), String.format("https://minotar.net/helm/%s/16.png", gameProfile.getId().toString())); }

    public ResourceLocation getTexture(final String identifier, final String url) {
        /*
        if (resourceLocations.size() > 50) {
            //Safe.GET.print("cleared all textures");
            index = 0;
            resourceLocations.clear();
        }
         */
        final TextureManager texturemanager = MINECRAFT.getTextureManager();
        DynamicTexture dynamicmodtexture = this.resourceLocations.get(identifier);
        final boolean flag = dynamicmodtexture != null && dynamicmodtexture.getUrl() != null && !dynamicmodtexture.getUrl().equals(url);
        if (dynamicmodtexture == null || flag) {
            if (flag) {
                texturemanager.deleteTexture(dynamicmodtexture.getResourceLocation());
                dynamicmodtexture.setResourceLocation(allah);
                dynamicmodtexture.setUrl(null);
            } else {
                dynamicmodtexture = new DynamicTexture(allah, null);
            }

            this.resourceLocations.put(identifier, dynamicmodtexture);
            dynamicmodtexture = this.resolveImageTexture(identifier, url, dynamicmodtexture);
        }

        return dynamicmodtexture.getResourceLocation();
    }

    private DynamicTexture resolveImageTexture(final String identifier, final String url, DynamicTexture defaultT) {
        final DynamicTexture[] t = {defaultT};
        if (identifier != null && url != null) {
            final TextureManager texturemanager = MINECRAFT.getTextureManager();
            final ResourceLocation resourcelocation = new ResourceLocation(this.resourceName + "/" + this.getHash(url));
            final ThreadDownloadTextureImage threaddownloadtextureimage = new ThreadDownloadTextureImage(url, resourcelocation, accepted -> {
                DynamicTexture dynamicTexture = new DynamicTexture(resourcelocation, url);
                t[0] = dynamicTexture;
                resourceLocations.put(identifier, dynamicTexture);
            },"Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/70.0.3538.77",this);
            texturemanager.loadTexture(resourcelocation, threaddownloadtextureimage);
        }
        return t[0];
    }

    public int getHash(final String url) {
        int i = 7;
        for (int j = 0; j < url.length(); ++j) { i = i * 31 + url.charAt(j); }
        return i;
    }

    public int getTexId() {
        return preTextures[index++];
    }

    public Map<String, DynamicTexture> getResourceLocations() { return this.resourceLocations; }

    public String getResourceName() { return this.resourceName; }
}