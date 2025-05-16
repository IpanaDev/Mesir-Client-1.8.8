package ipana.utils.chunk;

import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkFacings {
    public static final EnumFacing[][] enumFacingArrays = makeEnumFacingArrays(false);
    public static final EnumFacing[][] enumFacingOppositeArrays = makeEnumFacingArrays(true);

    private static EnumFacing[][] makeEnumFacingArrays(boolean opposite) {
        int i = 64;
        EnumFacing[][] aenumfacing = new EnumFacing[i][];

        for (int j = 0; j < i; ++j) {
            List<EnumFacing> list = new ArrayList<>();

            for (int k = 0; k < EnumFacing.VALUES.length; ++k) {
                EnumFacing enumfacing = EnumFacing.VALUES[k];
                EnumFacing enumfacing1 = opposite ? enumfacing.getOpposite() : enumfacing;
                int l = 1 << enumfacing1.ordinal();

                if ((j & l) != 0) {
                    list.add(enumfacing);
                }
            }

            EnumFacing[] aenumfacing1 = list.toArray(new EnumFacing[list.size()]);
            aenumfacing[j] = aenumfacing1;
        }
        return aenumfacing;
    }

    public static EnumFacing[] getFacingsNotOpposite(int setDisabled) {
        return enumFacingOppositeArrays[~setDisabled & 63];
    }
}
