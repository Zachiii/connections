package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SendMessage {
    public SendMessage(SocketChannel channel,Message message){
        try{
            Gson gson = new GsonBuilder().create();
            String jsonMessage =  gson.toJson(message);
            byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
            int length = jsonMessageBytes.length;
            ByteBuffer byteBuffer = ByteBuffer.allocate(4+length);
            byteBuffer.putInt(length);
            byteBuffer.put(jsonMessageBytes);
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()){
                channel.write(byteBuffer);
            }
            //System.out.println("Inviato al server il messaggio: " + jsonMessage);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
