package net.minecraft.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import net.minecraft.util.matrix.MatrixStack;
import org.apache.logging.log4j.Logger;

public class Util
{
    public static Util.EnumOS getOSType()
    {
        String s = System.getProperty("os.name").toLowerCase();
        return s.contains("win") ? Util.EnumOS.WINDOWS : (s.contains("mac") ? Util.EnumOS.OSX : (s.contains("solaris") ? Util.EnumOS.SOLARIS : (s.contains("sunos") ? Util.EnumOS.SOLARIS : (s.contains("linux") ? Util.EnumOS.LINUX : (s.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN)))));
    }

    public static <V> void func_181617_a(FutureTask<V> p_181617_0_, Logger p_181617_1_)
    {
        try
        {
            p_181617_0_.run();
            p_181617_0_.get();
        }
        catch (ExecutionException | InterruptedException executionexception)
        {
            p_181617_1_.fatal("Error executing task", executionexception);
        }

    }

    public static <T> T make(T object, Consumer<T> consumer)
    {
        consumer.accept(object);
        return object;
    }

    public enum EnumOS
    {
        LINUX,
        SOLARIS,
        WINDOWS,
        OSX,
        UNKNOWN
    }
}
