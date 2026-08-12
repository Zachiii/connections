package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.RegisterMessage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket clientChannel;
    private ConcurrentHashMap<String,User> registeredUsers;
    private ConcurrentHashMap<String,ClientHandler> currentUsers;
    public ClientHandler(Socket clientChannel, ConcurrentHashMap<String,User> registeredUsers, ConcurrentHashMap<String, ClientHandler> currentUsers) {
        this.clientChannel = clientChannel;
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
    }
    @Override
    public void run() {
        //devo fare il controllo del client: se è registrato o meno, gestire User, gestire scritture su registeredUsersr
        try(DataInputStream inputStream = new DataInputStream(this.clientChannel.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(this.clientChannel.getOutputStream()))
        {
            while(true){
                int messageLength = inputStream.readInt();
                byte[] byteMessage = new byte[messageLength];
                inputStream.readFully(byteMessage);
                String messageString = new String(byteMessage, StandardCharsets.UTF_8);
                JsonObject messageJson = JsonParser.parseString(messageString).getAsJsonObject();
                String operation = messageJson.get("operation").getAsString();
                switch(operation){
                    case "register":
                        String username = messageJson.get("username").getAsString();
                        String psw = messageJson.get("psw").getAsString();
                        RegisterMessage message = new RegisterMessage(operation, username, psw);
                        if(registeredUsers.containsKey(username)){
                            System.out.println("Utente già registrato");
                        } else {
                            registeredUsers.put(username, new User(username,psw));
                            System.out.println("Utente " + username + " registrato");
                        }
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
