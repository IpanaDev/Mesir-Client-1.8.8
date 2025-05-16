package ipana.utils.render;

import net.minecraft.util.ResourceLocation;

import java.beans.ConstructorProperties;

public class DynamicTexture {
    private ResourceLocation resourceLocation;
    private String url;

    public ResourceLocation getResourceLocation() { return this.resourceLocation; }

    public String getUrl() { return this.url; }

    public void setResourceLocation(final ResourceLocation resourceLocation) { this.resourceLocation = resourceLocation; }

    public void setUrl(final String url) { this.url = url; }

    @ConstructorProperties({ "resourceLocation", "url" })
    public DynamicTexture(final ResourceLocation resourceLocation, final String url) {
        this.resourceLocation = resourceLocation;
        this.url = url;
    }
}
