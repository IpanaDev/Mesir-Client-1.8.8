package optifine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ReflectorForge
{

    public static boolean renderFirstPersonHand(RenderGlobal p_renderFirstPersonHand_0_, float p_renderFirstPersonHand_1_, int p_renderFirstPersonHand_2_)
    {
        return false;
    }

    public static InputStream getOptiFineResourceStream(String p_getOptiFineResourceStream_0_)
    {
        if (!Reflector.OptiFineClassTransformer_instance.exists())
        {
            return null;
        }
        else
        {
            Object object = Reflector.getFieldValue(Reflector.OptiFineClassTransformer_instance);

            if (object == null)
            {
                return null;
            }
            else
            {
                if (p_getOptiFineResourceStream_0_.startsWith("/"))
                {
                    p_getOptiFineResourceStream_0_ = p_getOptiFineResourceStream_0_.substring(1);
                }

                byte[] abyte = (byte[])((byte[])Reflector.call(object, Reflector.OptiFineClassTransformer_getOptiFineResource, new Object[] {p_getOptiFineResourceStream_0_}));

                if (abyte == null)
                {
                    return null;
                }
                else
                {
                    InputStream inputstream = new ByteArrayInputStream(abyte);
                    return inputstream;
                }
            }
        }
    }

    public static boolean blockHasTileEntity(IBlockState p_blockHasTileEntity_0_)
    {
        Block block = p_blockHasTileEntity_0_.getBlock();
        return block.hasTileEntity();
    }
}
