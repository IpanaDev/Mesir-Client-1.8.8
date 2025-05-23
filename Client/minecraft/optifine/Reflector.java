package optifine;

import com.google.common.base.Optional;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4f;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

public class Reflector
{
    public static ReflectorClass BetterFoliageClient = new ReflectorClass("mods.betterfoliage.client.BetterFoliageClient");
    public static ReflectorClass Minecraft = new ReflectorClass(Minecraft.class);
    public static ReflectorField Minecraft_defaultResourcePack = new ReflectorField(Minecraft, DefaultResourcePack.class);
    public static ReflectorClass OptiFineClassTransformer = new ReflectorClass("optifine.OptiFineClassTransformer");
    public static ReflectorField OptiFineClassTransformer_instance = new ReflectorField(OptiFineClassTransformer, "instance");
    public static ReflectorMethod OptiFineClassTransformer_getOptiFineResource = new ReflectorMethod(OptiFineClassTransformer, "getOptiFineResource");
    public static ReflectorClass ResourcePackRepository = new ReflectorClass(ResourcePackRepository.class);
    public static ReflectorField ResourcePackRepository_repositoryEntries = new ReflectorField(ResourcePackRepository, List.class, 1);
    public static ReflectorClass ChunkProviderClient = new ReflectorClass(net.minecraft.client.multiplayer.ChunkProviderClient.class);
    public static ReflectorField ChunkProviderClient_chunkMapping = new ReflectorField(ChunkProviderClient, LongHashMap.class);
    public static void callVoid(ReflectorMethod p_callVoid_0_, Object... p_callVoid_1_)
    {
        try
        {
            Method method = p_callVoid_0_.getTargetMethod();

            if (method == null)
            {
                return;
            }

            method.invoke(null, p_callVoid_1_);
        }
        catch (Throwable throwable)
        {
            handleException(throwable, null, p_callVoid_0_, p_callVoid_1_);
        }
    }

    public static boolean callBoolean(ReflectorMethod p_callBoolean_0_, Object... p_callBoolean_1_)
    {
        try
        {
            Method method = p_callBoolean_0_.getTargetMethod();

            if (method == null)
            {
                return false;
            }
            else
            {
                return (Boolean)method.invoke(null, p_callBoolean_1_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, null, p_callBoolean_0_, p_callBoolean_1_);
            return false;
        }
    }

    public static int callInt(ReflectorMethod p_callInt_0_, Object... p_callInt_1_)
    {
        try
        {
            Method method = p_callInt_0_.getTargetMethod();

            if (method == null)
            {
                return 0;
            }
            else
            {
                return (Integer)method.invoke(null, p_callInt_1_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, null, p_callInt_0_, p_callInt_1_);
            return 0;
        }
    }

    public static float callFloat(ReflectorMethod p_callFloat_0_, Object... p_callFloat_1_)
    {
        try
        {
            Method method = p_callFloat_0_.getTargetMethod();

            if (method == null)
            {
                return 0.0F;
            }
            else
            {
                return (Float)method.invoke(null, p_callFloat_1_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, null, p_callFloat_0_, p_callFloat_1_);
            return 0.0F;
        }
    }

    public static String callString(ReflectorMethod p_callString_0_, Object... p_callString_1_)
    {
        try
        {
            Method method = p_callString_0_.getTargetMethod();

            if (method == null)
            {
                return null;
            }
            else
            {
                return (String)method.invoke(null, p_callString_1_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, null, p_callString_0_, p_callString_1_);
            return null;
        }
    }

    public static Object call(ReflectorMethod p_call_0_, Object... p_call_1_)
    {
        try
        {
            Method method = p_call_0_.getTargetMethod();

            if (method == null)
            {
                return null;
            }
            else
            {
                return method.invoke(null, p_call_1_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, null, p_call_0_, p_call_1_);
            return null;
        }
    }

    public static void callVoid(Object p_callVoid_0_, ReflectorMethod p_callVoid_1_, Object... p_callVoid_2_)
    {
        try
        {
            if (p_callVoid_0_ == null)
            {
                return;
            }

            Method method = p_callVoid_1_.getTargetMethod();

            if (method == null)
            {
                return;
            }

            method.invoke(p_callVoid_0_, p_callVoid_2_);
        }
        catch (Throwable throwable)
        {
            handleException(throwable, p_callVoid_0_, p_callVoid_1_, p_callVoid_2_);
        }
    }

    public static boolean callBoolean(Object p_callBoolean_0_, ReflectorMethod p_callBoolean_1_, Object... p_callBoolean_2_)
    {
        try
        {
            Method method = p_callBoolean_1_.getTargetMethod();

            if (method == null)
            {
                return false;
            }
            else
            {
                return (Boolean)method.invoke(p_callBoolean_0_, p_callBoolean_2_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, p_callBoolean_0_, p_callBoolean_1_, p_callBoolean_2_);
            return false;
        }
    }

    public static int callInt(Object p_callInt_0_, ReflectorMethod p_callInt_1_, Object... p_callInt_2_)
    {
        try
        {
            Method method = p_callInt_1_.getTargetMethod();

            if (method == null)
            {
                return 0;
            }
            else
            {
                return (Integer)method.invoke(p_callInt_0_, p_callInt_2_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, p_callInt_0_, p_callInt_1_, p_callInt_2_);
            return 0;
        }
    }

    public static double callDouble(Object p_callDouble_0_, ReflectorMethod p_callDouble_1_, Object... p_callDouble_2_)
    {
        try
        {
            Method method = p_callDouble_1_.getTargetMethod();

            if (method == null)
            {
                return 0.0D;
            }
            else
            {
                return (Double)method.invoke(p_callDouble_0_, p_callDouble_2_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, p_callDouble_0_, p_callDouble_1_, p_callDouble_2_);
            return 0.0D;
        }
    }

    public static void callString(Object p_callString_0_, ReflectorMethod p_callString_1_, Object... p_callString_2_)
    {
        try
        {
            Method method = p_callString_1_.getTargetMethod();

            if (method != null) {
                method.invoke(p_callString_0_, p_callString_2_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, p_callString_0_, p_callString_1_, p_callString_2_);
        }
    }

    public static Object call(Object p_call_0_, ReflectorMethod p_call_1_, Object... p_call_2_)
    {
        try
        {
            Method method = p_call_1_.getTargetMethod();

            if (method == null)
            {
                return null;
            }
            else
            {
                return method.invoke(p_call_0_, p_call_2_);
            }
        }
        catch (Throwable throwable)
        {
            handleException(throwable, p_call_0_, p_call_1_, p_call_2_);
            return null;
        }
    }

    public static Object getFieldValue(ReflectorField p_getFieldValue_0_)
    {
        return getFieldValue(null, p_getFieldValue_0_);
    }

    public static Object getFieldValue(Object p_getFieldValue_0_, ReflectorField p_getFieldValue_1_)
    {
        try
        {
            Field field = p_getFieldValue_1_.getTargetField();

            if (field == null)
            {
                return null;
            }
            else
            {
                return field.get(p_getFieldValue_0_);
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
            return null;
        }
    }

    public static float getFieldValueFloat(Object p_getFieldValueFloat_0_, ReflectorField p_getFieldValueFloat_1_, float p_getFieldValueFloat_2_)
    {
        Object object = getFieldValue(p_getFieldValueFloat_0_, p_getFieldValueFloat_1_);

        if (!(object instanceof Float))
        {
            return p_getFieldValueFloat_2_;
        }
        else
        {
            return (Float)object;
        }
    }

    public static void setFieldValue(ReflectorField p_setFieldValue_0_, Object p_setFieldValue_1_)
    {
        setFieldValue( null, p_setFieldValue_0_, p_setFieldValue_1_);
    }

    public static boolean setFieldValue(Object p_setFieldValue_0_, ReflectorField p_setFieldValue_1_, Object p_setFieldValue_2_)
    {
        try
        {
            Field field = p_setFieldValue_1_.getTargetField();

            if (field == null)
            {
                return false;
            }
            else
            {
                field.set(p_setFieldValue_0_, p_setFieldValue_2_);
                return true;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
            return false;
        }
    }

    public static Object newInstance(ReflectorConstructor p_newInstance_0_, Object... p_newInstance_1_)
    {
        Constructor constructor = p_newInstance_0_.getTargetConstructor();

        if (constructor == null)
        {
            return null;
        }
        else
        {
            try
            {
                return constructor.newInstance(p_newInstance_1_);
            }
            catch (Throwable throwable)
            {
                handleException(throwable, p_newInstance_0_, p_newInstance_1_);
                return null;
            }
        }
    }

    public static boolean matchesTypes(Class[] p_matchesTypes_0_, Class[] p_matchesTypes_1_)
    {
        if (p_matchesTypes_0_.length != p_matchesTypes_1_.length)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < p_matchesTypes_1_.length; ++i)
            {
                Class oclass = p_matchesTypes_0_[i];
                Class oclass1 = p_matchesTypes_1_[i];

                if (oclass != oclass1)
                {
                    return false;
                }
            }

            return true;
        }
    }

    private static void handleException(Throwable p_handleException_0_, Object p_handleException_1_, ReflectorMethod p_handleException_2_, Object[] p_handleException_3_)
    {
        if (p_handleException_0_ instanceof InvocationTargetException)
        {
            Throwable throwable = p_handleException_0_.getCause();

            if (throwable instanceof RuntimeException)
            {
                throw (RuntimeException)throwable;
            }
            else
            {
                p_handleException_0_.printStackTrace();
            }
        }
        else
        {
            if (p_handleException_0_ instanceof IllegalArgumentException)
            {
                Config.warn("*** IllegalArgumentException ***");
                Config.warn("Method: " + p_handleException_2_.getTargetMethod());
                Config.warn("Object: " + p_handleException_1_);
                Config.warn("Parameter classes: " + Config.arrayToString(getClasses(p_handleException_3_)));
                Config.warn("Parameters: " + Config.arrayToString(p_handleException_3_));
            }

            Config.warn("*** Exception outside of method ***");
            Config.warn("Method deactivated: " + p_handleException_2_.getTargetMethod());
            p_handleException_2_.deactivate();
            p_handleException_0_.printStackTrace();
        }
    }

    private static void handleException(Throwable p_handleException_0_, ReflectorConstructor p_handleException_1_, Object[] p_handleException_2_)
    {
        if (!(p_handleException_0_ instanceof InvocationTargetException)) {
            if (p_handleException_0_ instanceof IllegalArgumentException) {
                Config.warn("*** IllegalArgumentException ***");
                Config.warn("Constructor: " + p_handleException_1_.getTargetConstructor());
                Config.warn("Parameter classes: " + Config.arrayToString(getClasses(p_handleException_2_)));
                Config.warn("Parameters: " + Config.arrayToString(p_handleException_2_));
            }

            Config.warn("*** Exception outside of constructor ***");
            Config.warn("Constructor deactivated: " + p_handleException_1_.getTargetConstructor());
            p_handleException_1_.deactivate();
        }
        p_handleException_0_.printStackTrace();
    }

    private static Object[] getClasses(Object[] p_getClasses_0_)
    {
        if (p_getClasses_0_ == null)
        {
            return new Class[0];
        }
        else
        {
            Class[] aclass = new Class[p_getClasses_0_.length];

            for (int i = 0; i < aclass.length; ++i)
            {
                Object object = p_getClasses_0_[i];

                if (object != null)
                {
                    aclass[i] = object.getClass();
                }
            }

            return aclass;
        }
    }

}
