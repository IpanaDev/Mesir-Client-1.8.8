package ipana.utils.file;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class SSUploader {

    public static String sendPOSTRequest(String url, File binaryFile)
    {
        String charset = "UTF-8";
        String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";
        int responseCode;

        try
        {
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.addRequestProperty("User-Agent", "CheckpaySrv/1.0.0");
            connection.addRequestProperty("Accept", "*/*");

            OutputStream output = connection.getOutputStream();
            PrintWriter writer  = new PrintWriter(new OutputStreamWriter(output, charset), true);

            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(binaryFile.getName()).append("\"").append(CRLF);
            writer.append("Content-Type: application/octet-stream").append(CRLF);
            writer.append(CRLF).flush();

            Files.copy(binaryFile.toPath(), output);
            output.flush();

            writer.append(CRLF).append("--").append(boundary).append("--").flush();

            responseCode = ((HttpURLConnection) connection).getResponseCode();


            if(responseCode !=200)
                return "Error";

            InputStream Instream = connection.getInputStream();
            String finished = new String(IOUtils.toByteArray(Instream));
            IChatComponent ichatcomponent = new ChatComponentText(finished);
            ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,finished));
            Minecraft.getMinecraft().thePlayer.addChatMessage(ichatcomponent);
            return finished;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "Error";
    }
}
