package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ResponseHandler {
    public ResponseHandler(SocketChannel channel){
        ByteBuffer byteLength = ByteBuffer.allocate(4);
        try {
            while (byteLength.hasRemaining()){
                if(channel.read(byteLength) == -1){
                    throw new IOException("Server disconneso durante lettura della lunghezza");
                }
            }
            byteLength.flip();
            int length = byteLength.getInt();
            ByteBuffer byteMessage = ByteBuffer.allocate(length);
            while (byteMessage.hasRemaining()){
                if(channel.read(byteMessage) == -1){
                    throw new IOException("Server disconneso durante lettura del messaggio");
                }
            }
            String messageString = new String(byteMessage.array(), StandardCharsets.UTF_8);
            JsonObject messageJson = JsonParser.parseString(messageString).getAsJsonObject();
            System.out.println(messageJson.get("response").getAsString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
