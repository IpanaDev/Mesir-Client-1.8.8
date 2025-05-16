package net.minecraft.client.renderer.block;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class BlockVisibility {
    public static final EnumFacing[][] facings = createFacings();
    public Block block;
    public int sideMaskID;

    public BlockVisibility(Block block) {
        this.block = block;
    }

    private static EnumFacing[][] createFacings() {
        EnumFacing[][] enumFacings = new EnumFacing[64][];
        for (int i = 0; i < enumFacings.length; ++i) {
            List<EnumFacing> list = new ArrayList<>();
            for (EnumFacing facing : EnumFacing.VALUES) {
                int l = 1 << facing.ordinal();

                if ((i & l) != 0) {
                    list.add(facing);
                }
            }
            EnumFacing[] aenumfacing = list.toArray(new EnumFacing[list.size()]);
            enumFacings[i] = aenumfacing;
        }
        return enumFacings;
    }

    public static EnumFacing[] getFacings(int maskID) {
        int i = ~maskID & 63;
        return facings[i];
    }
}
