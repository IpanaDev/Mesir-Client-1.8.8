package ipana.utils;

public class Benchmark {



    public static void benchmark(String info, ThrowableFunction function) {
        long ms = System.currentTimeMillis();
        try {
            function.accept();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("BENCHMARK: "+info+" took "+(System.currentTimeMillis()-ms)+"ms.");
    }


    public interface ThrowableFunction {
        void accept() throws Exception;
    }
}
