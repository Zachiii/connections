package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SendResponse {
    public SendResponse(Message message, DataOutputStream outputStream){
        //Manda messaggio di risposta al client
        Gson gson = new GsonBuilder().create();
        String jsonMessage =  gson.toJson(message);
        byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
        try{
            outputStream.writeInt(jsonMessageBytes.length);
            outputStream.write(jsonMessageBytes);
            outputStream.flush();
        } catch(IOException e){
            System.out.println("Errore durante scrittura risposta al client");
        }
    }
}
