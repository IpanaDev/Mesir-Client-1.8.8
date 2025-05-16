package ipana.utils.currency;

import ipana.utils.math.MathUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Currency {
    public static double DOLAR;
    public static int USAGE;
    private static long ms;
    private static String price = "";
    private static Thread thread;


    public static String[] get() {
        if (System.currentTimeMillis() - ms >= 300000) {
            thread = new Thread(() -> {
                try {
                    URLConnection connection = new URL("https://free.currconv.com/api/v7/convert?apiKey=f089ff51031256b05020&q=USD_TRY&compact=ultra").openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
                    BufferedReader bufRead = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String lines;
                    StringBuilder str = new StringBuilder();
                    while ((lines = bufRead.readLine()) != null) {
                        str.append(lines);
                    }
                    if (str.toString().contains("error")) {
                        DOLAR = -1;
                    } else {
                        DOLAR = Double.parseDouble(str.toString().split(":")[1].replace("}", ""));
                    }
                    bufRead.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("No Internet Connection");
                }
                try {
                    URLConnection connection = new URL("https://www.vatanbilgisayar.com/msi-geforce-gtx-1650-d6-ventus-xs-oc-4gb-gddr6-128bit-nvidia-ekran-karti.html").openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
                    BufferedReader bufRead = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String lines;
                    while (true) {
                        if ((lines = bufRead.readLine()).contains("ProductId")) break;
                    }
                    String raw = lines.split(",")[3];
                    char c = '"';
                    price = raw.split(":")[1].replace(String.valueOf(c), "");
                    bufRead.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("No Internet Connection");
                }
            });
            thread.start();
            ms = System.currentTimeMillis();
        }
        return new String[]{MathUtils.fixFormat(DOLAR, 2)+"" /*+ " (" + USAGE + " - " + refreshTime + ")"*/, "GTX 1650 : " + price + "TL"};
    }
}
