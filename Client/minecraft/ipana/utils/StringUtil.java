package ipana.utils;

public class StringUtil {
    private final static String[] WORDS = new String[]{"̇̇","̇","İ","İ̇̇","لُلُصّبُلُلصّبُررً","İ","İ̇"};
    private final static String BANNED_WORD = "Banned Word!";

    public static String combine(Object... args) {
        StringBuilder builder = new StringBuilder();
        for (Object o : args) {
            builder.append(o);
        }
        return builder.toString();
    }

    public static String preventCrash(String str) {
        return str.replace("̇","");
    }

    public static String toString(Object[] array, String combiner) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length-1; i++) {
            if (!"".equals(array[i])) {
                builder.append(array[i]);
                builder.append(combiner);
            }
        }
        Object last = array[array.length - 1];
        if (!"".equals(last)) {
            builder.append(last);
        }
        return builder.toString();
    }
}
