package ipana.utils.gl;

import optifine.Config;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class GLCall {
    public static <T> void drawDirect(GList<T> gList) {
        if (Config.isGlCalls()) {
            gList.render();
        }
    }
    public static <T> void draw(GList<T> gList, T type, Consumer<Boolean> consumer) {
        if (Config.isGlCalls()) {
            gList.render(type, consumer);
        } else {
            consumer.accept(true);
        }
    }
    public static <T> boolean checkAndCompile(GLists<T> gList, T type, int index, Consumer<Boolean> consumer) {
        if (Config.isGlCalls()) {
            return gList.checkAndCompile(type, index, consumer);
        } else {
            consumer.accept(true);
        }
        return false;
    }
    public static <T> boolean check(GLists<T> gList, T type, int index) {
        if (Config.isGlCalls()) {
            return gList.check(type, index);
        }
        return false;
    }
    public static <T> void compile(GLists<T> gList, int index, Consumer<Boolean> consumer) {
        if (Config.isGlCalls()) {
            gList.compile(index, consumer);
        } else {
            consumer.accept(true);
        }
    }
    public static <T> void drawLists(GLists<T> gList) {
        if (Config.isGlCalls()) {
            gList.render();
        }
    }
}
