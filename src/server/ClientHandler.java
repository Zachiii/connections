package server;

import com.google.gson.*;
import common.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket clientChannel;
    private final ConcurrentHashMap<String,User> registeredUsers;
    private final ConcurrentHashMap<String,ClientHandler> currentUsers;
    private PlayerStatus status = null;
    private String username;
    public ClientHandler(Socket clientChannel, ConcurrentHashMap<String,User> registeredUsers, ConcurrentHashMap<String, ClientHandler> currentUsers) {
        this.clientChannel = clientChannel;
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
    }
    @Override
    public void run() {
        Boolean isRunning = true;
        //devo fare il controllo dei messaggi del client
        try(DataInputStream inputStream = new DataInputStream(this.clientChannel.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(this.clientChannel.getOutputStream()))
        {
            while(isRunning){
                int messageLength = inputStream.readInt();
                byte[] byteMessage = new byte[messageLength];
                inputStream.readFully(byteMessage);
                String messageString = new String(byteMessage, StandardCharsets.UTF_8);
                JsonObject messageJson = JsonParser.parseString(messageString).getAsJsonObject();
                String operation = messageJson.get("operation").getAsString();
                switch(operation){
                    case "register": {
                        this.username = messageJson.get("username").getAsString();
                        String psw = messageJson.get("psw").getAsString();
                        if(registeredUsers.containsKey(username)){
                            ResponseMessage response = new ResponseMessage("registerResponse","409","Utente già registrato");
                            SendResponse(response,outputStream);
                            System.out.println("DEBUG: Utente già registrato");
                        } else {
                            registeredUsers.put(username, new User(username,psw));
                            System.out.println("DEBUG: Utente " + username + " registrato");
                            ResponseMessage response = new ResponseMessage("registerResponse","200", "Utente registrato");
                            SendResponse(response,outputStream);
                        }
                        break;
                    }
                    case "updateCredentials": {
                        String oldUsername = messageJson.get("oldUsername").getAsString();
                        String newUsername = messageJson.get("newUsername").getAsString();
                        String oldPsw = messageJson.get("oldPsw").getAsString();
                        String newPsw = messageJson.get("newPsw").getAsString();
                        if(registeredUsers.containsKey(oldUsername)){
                            User currentUser = registeredUsers.get(oldUsername);
                            String registeredPassword = currentUser.getPassword();
                            if(registeredPassword.equals(oldPsw)){
                                currentUser.updateCredentials(newUsername, newPsw);
                                registeredUsers.remove(oldUsername);
                                registeredUsers.put(newUsername, currentUser);
                                currentUsers.remove(oldUsername);
                                currentUsers.put(newUsername, this);
                                System.out.println("DEBUG: Credenziali aggiornate");
                                ResponseMessage response =  new ResponseMessage("updateCredentials","200", "Credenziali aggiornate");
                                SendResponse(response,outputStream);
                                break;
                            }
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse","401","Password errata");
                            SendResponse(response,outputStream);
                            break;
                        }
                        ResponseMessage response = new ResponseMessage("updateCredentialsResponse","401","Utente inesistente");
                        SendResponse(response,outputStream);
                        break;
                    }
                    case "login": {
                        this.username = messageJson.get("username").getAsString();
                        String psw = messageJson.get("psw").getAsString();
                        if(!registeredUsers.containsKey(username)){
                            ResponseMessage response = new ResponseMessage("loginResponse","401","Utente inesistente");
                            SendResponse(response,outputStream);
                            System.out.println("DEBUG: Utente non registrato, impossibile fare il login");
                            break;
                        }
                        else if(currentUsers.containsKey(username)){
                            ResponseMessage response = new ResponseMessage("loginResponse","409","Utente già loggato");
                            SendResponse(response,outputStream);
                            System.out.println("DEBUG: Login già fatto");
                            break;
                        }
                        else if(registeredUsers.get(username).getPassword().equals(psw)){
                            currentUsers.put(username, this);
                            ResponseMessage response = new ResponseMessage("loginResponse","200", "Utente loggato");
                            SendResponse(response,outputStream);
                            System.out.println("DEBUG: Login effettuato");
                            this.status = new PlayerStatus(username,true);
                            break;
                        }
                        ResponseMessage response = new ResponseMessage("loginResponse","401","Password errata");
                        SendResponse(response,outputStream);
                        System.out.println("DEBUG: Password errata");
                        break;
                    }
                    case  "logout": {
                        System.out.println("DEBUG: Logout");
                        ResponseMessage response = new ResponseMessage("logoutResponse","200", "Logout");
                        SendResponse(response,outputStream);
                        if(this.status != null)status.setOnline(false);
                        if(username != null && currentUsers.containsKey(username))currentUsers.remove(username,this);
                        isRunning = false;
                        break;
                    }
                    case "submitProposal": {}
                    case "requestGameInfo": {}
                    case "requestGameStats": {}
                    case "requestLeaderboard": {}
                    case "requestPlayerStats": {}
                }
            }
        } catch (IOException e) {
            System.out.println("Errore: client disconnesso in maniera anomala");
        } finally {
            if(this.status != null) status.setOnline(false);
            if(username != null) currentUsers.remove(username,this);
        }
    }

    private void SendResponse(Message message, DataOutputStream outputStream){
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

    public void MatchEnd(){
        if(this.status != null) this.status.setOnline(false);
        //salva il punteggio in User e cambia score generale partite vinte/perse
    }
}
