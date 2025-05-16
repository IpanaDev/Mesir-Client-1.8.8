package net.minecraft.util;

public enum EnumWorldBlockLayer
{
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent"),
    ORE("Ore");

    private final String layerName;
    public static final EnumWorldBlockLayer[] VALUES = values();

    EnumWorldBlockLayer(String layerNameIn) {
        this.layerName = layerNameIn;
    }

    public String toString()
    {
        return this.layerName;
    }
}
