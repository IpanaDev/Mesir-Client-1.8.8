package ipana.utils.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ThreadDownloadTextureImage extends SimpleTexture {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final String imageUrl;
    private final Consumer<Boolean> consumer;
    private String userAgent = "Unknown";
    private BufferedImage bufferedImage;
    private boolean textureLoaded = false;
    private DynamicTextureManager hello;
    private int iDontFuckWithOpenGLgetFuckingReal = -1;

    public ThreadDownloadTextureImage(final String imageUrl,
                                      final ResourceLocation textureResourceLocation,
                                      final Consumer<Boolean> consumer,
                                      final String userAgent,
                                      final DynamicTextureManager hello) {
        super(textureResourceLocation);
        this.imageUrl = imageUrl;
        this.consumer = consumer;
        this.userAgent = userAgent;
        this.hello = hello;
    }


    public int getGlTextureId() {
        if (iDontFuckWithOpenGLgetFuckingReal == -1) {
            iDontFuckWithOpenGLgetFuckingReal = hello.getTexId();
        }
        final int i = iDontFuckWithOpenGLgetFuckingReal;
        if (!this.textureLoaded && this.bufferedImage != null) {
            this.textureLoaded = true;
            TextureUtil.uploadTextureImage(i, this.bufferedImage);
        }
        return i;
    }

    public void loadTexture(final IResourceManager resourceManager) throws IOException {
        executorService.execute(() -> {
            try {
                final HttpURLConnection httpurlconnection = (HttpURLConnection) (new URL(ThreadDownloadTextureImage.this.imageUrl)).openConnection();
                httpurlconnection.setRequestProperty("User-Agent", userAgent);
                httpurlconnection.connect();
                final int i = httpurlconnection.getResponseCode();
                if (i / 100 == 2) {
                    InputStream inputStream = httpurlconnection.getInputStream();
                    bufferedImage = TextureUtil.readBufferedImage(inputStream);
                    inputStream.close();
                } else {
                    System.out.println(i);
                }
                httpurlconnection.disconnect();
            } catch (final Exception exception) {
                exception.printStackTrace();
            }
        });
        consumer.accept(bufferedImage != null);
    }
}