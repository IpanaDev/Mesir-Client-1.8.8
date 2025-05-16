package ipana.managements.update;

import net.minecraft.client.gui.GuiMainMenu;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download implements Runnable{
    private String link;
    private File out;
    private double percent;

    public Download(String link,File out) {
        this.link = link;
        this.out = out;
    }
    @Override
    public void run() {
        try {
            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
            double fileSize = (double)http.getContentLengthLong();
            System.out.println(url.toString());
            System.out.println(fileSize);
            BufferedInputStream in = new BufferedInputStream(http.getInputStream());
            FileOutputStream fos = new FileOutputStream(this.out);
            //BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
            byte[] buffer = new byte[1024];
            double downloaded = 0.0;
            int read;
            http.setConnectTimeout(10);
            http.setReadTimeout(10);
            while ((read = in.read(buffer,0,1024)) >= 0) {
                fos.write(buffer,0,read);
                downloaded+=read;
                setPercent((downloaded*100)/fileSize);
                //GuiMainMenu.percent = getPercent();
                System.out.println("Downloaded %"+getPercent());
            }
            fos.close();
            in.close();

            System.out.println("Download Complete.");
        } catch (IOException e) {
            System.out.println("Download Failed.");
            e.printStackTrace();
        }
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
