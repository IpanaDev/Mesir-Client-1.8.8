package ipana.irc.user;

import ipana.irc.packet.Packets;
import ipana.utils.render.RenderUtils;
import net.minecraft.util.ResourceLocation;

public class PlayerCosmetics {
    public int maskID;
    public static final int CAPE = 0b1;
    public static final int EARS = 0b10;
    public static final int CHILD = 0b100;
    public static final int MODELS = 0b1000;
    public Cosmetic[] cosmetics = new Cosmetic[4];

    public PlayerCosmetics(User user, int maskID) {
        cosmetics[0] = new Cosmetic("none", "none", CapeType.EMPTY, new ResourceLocation(""));
        cosmetics[1] = new Cosmetic(-2173);
        cosmetics[2] = new Cosmetic(1F);
        cosmetics[3] = new Cosmetic("none", new ResourceLocation(""));
    }

    public Cosmetic getCosmetic(int id) {
        return cosmetics[(int) (Math.log(id) / Math.log(2))];
    }

    public void setCosmetics(int id, Object... parameters) {
        getCosmetic(id).parameters = parameters;
    }

    public boolean doesPlayerHave(int nigga) {
        return (maskID & nigga) != 0;
    }

    public ResourceLocation parseCape(String capeURL) {
        ResourceLocation location;
        if (capeURL.startsWith("http")) {
            location = RenderUtils.downloadUrl(capeURL);
        } else {
            location = new ResourceLocation("mesir/capes/"+capeURL+".png");
        }
        return location;
    }

    public static class Cosmetic {
        private Object[] parameters;

        public Cosmetic(Object... parameter) {
            this.parameters = parameter;
        }

        public Object[] params() {
            return parameters.clone();
        }
    }

    public void sendPacket() {
        Object[] capeParams = getCosmetic(CAPE).params();
        Object[] earsParams = getCosmetic(EARS).params();
        Object[] childParams = getCosmetic(CHILD).params();
        Object[] modelParams = getCosmetic(MODELS).params();
        // @DISABLE_FORMATTING
        String[] array = new String[]{
                s(maskID),
                s(capeParams[0]), s(capeParams[1]), s(capeParams[2]),
                s(earsParams[0]),
                s(childParams[0]),
                s(modelParams[0])
        };
        Packets.COSMETIC_PACKET.sendAsPacket(array);
        // @ENABLE_FORMATTING
    }

    public String s(Object object) {
        return String.valueOf(object).replace(" ","");
    }

    public enum CapeType {
        URL,
        SHADERS,
        PHYSICS,
        LOCAL,
        EMPTY
    }
}
