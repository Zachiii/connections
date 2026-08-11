package server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.*;
import common.RegisterMessage;

public class ClientHandler implements Runnable{
    private final Socket socket;
    ConcurrentHashMap<String,ClientHandler> client;
    private ConcurrentHashMap<String,User> registeredUsers;
    public ClientHandler(Socket socket, ConcurrentHashMap<String,ClientHandler> client, ConcurrentHashMap<String,User> registeredUsers) {
        this.socket = socket;
        this.registeredUsers = registeredUsers;
        this.client = client;
    }
    //devo fare il controllo del client: se è registrato o meno, gestire User, gestire scritture su registeredUsersr
    @Override
    public void run() {
        System.out.println("client connesso");
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //inizio lettura input
            String jsonMessage;
               while((jsonMessage = input.readLine()) != null){
                JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();
                String operation = jsonObject.get("operation").getAsString();
                //capisco che tipo exdi operazione ha richiesto il client
                switch (operation) {
                    case "register":
                        String username = jsonObject.get("username").getAsString();
                        String password = jsonObject.get("psw").getAsString();
                        RegisterMessage message = new RegisterMessage(operation,username,password);
                        if(registeredUsers.containsKey(username)){
                            //l'utente è già registrato quindi mando messaggio di errore
                            System.out.println("Username is already in use");
                        } else {
                            //salvare mappatura su file JSON
                            //rispondi a client
                            System.out.println("Username is registered");
                        }
                        break;
                    case "login":
                        System.out.println("Fatto il login");
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
