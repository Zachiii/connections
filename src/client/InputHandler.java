package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.RegisterMessage;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InputHandler {
    private SocketChannel channel;
    public InputHandler(SocketChannel channel) {
        this.channel = channel;
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        System.out.print("Enter your choice: ");
        while(!done){
            String operation = scanner.nextLine().trim();
            if(operation.equals("exit")){
                done = true;
                break;
            }
            if(operation.equals("register")){
                try{
                    String username = scanner.nextLine().trim();
                    String password = scanner.nextLine().trim();
                    RegisterMessage message = new RegisterMessage(operation,username,password);
                    Gson gson = new GsonBuilder().create();
                    String jsonMessage =  gson.toJson(message);
                    System.out.println(jsonMessage);
                    byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
                    int length = jsonMessage.length();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4+length); // per dire al server esattamente quanto è lungo il messaggio
                    byteBuffer.putInt(length);
                    byteBuffer.put(jsonMessageBytes);
                    byteBuffer.flip();
                    while(byteBuffer.hasRemaining()){
                        channel.write(byteBuffer);
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
