package server;

import com.google.gson.*;
import common.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket clientChannel;
    private final ConcurrentHashMap<String,User> registeredUsers;
    private final ConcurrentHashMap<String,ClientHandler> currentUsers;
    private ConcurrentHashMap<String,PlayerStatus> activePlayers;
    private PlayerStatus playerStatus = null;                //Indica se l'utente è online e le varie statistiche della partita
    private String username;
    private int clientUDPport;
    private GameManager gameManager;
    public ClientHandler(Socket clientChannel,
                         ConcurrentHashMap<String,User> registeredUsers,
                         ConcurrentHashMap<String, ClientHandler> currentUsers,
                         ConcurrentHashMap<String,PlayerStatus> activePlayers,
                         GameManager gameManager){
        this.clientChannel = clientChannel;
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
        this.activePlayers = activePlayers;
        this.gameManager = gameManager;
    }
    @Override
    public void run() {
        Boolean isRunning = true;
        //devo fare il controllo dei messaggi del client
        try(DataInputStream inputStream = new DataInputStream(this.clientChannel.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(this.clientChannel.getOutputStream()))
        {
            while(isRunning){
                //lettura del messaggio
                int messageLength = inputStream.readInt();
                byte[] byteMessage = new byte[messageLength];
                inputStream.readFully(byteMessage);
                String messageString = new String(byteMessage, StandardCharsets.UTF_8);
                JsonObject messageJson = JsonParser.parseString(messageString).getAsJsonObject();
                String operation = messageJson.get("operation").getAsString();
                //Switch che gestisce tutti i tipi di messaggi JSON
                switch(operation){
                    case "register": {
                        this.username = messageJson.get("username").getAsString();
                        String psw = messageJson.get("psw").getAsString();
                        if (Objects.equals(this.username, "") || Objects.equals(psw, "")){
                            System.out.println("DEBUG: Username o password non validi");
                            ResponseMessage response = new ResponseMessage("registerResponse","401", "Username e password non possono essere vuoti");
                            new SendResponse(response,outputStream);
                        }
                        if(registeredUsers.containsKey(username)){
                            ResponseMessage response = new ResponseMessage("registerResponse","409","Utente già registrato");
                            new SendResponse(response,outputStream);
                            System.out.println("DEBUG: Utente già registrato");
                        } else {
                            registeredUsers.put(username, new User(username,psw));
                            System.out.println("DEBUG: Utente " + username + " registrato");
                            ResponseMessage response = new ResponseMessage("registerResponse","200", "Utente registrato");
                            new SendResponse(response,outputStream);
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
                                new SendResponse(response,outputStream);
                                break;
                            }
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse","401","Password errata");
                            new SendResponse(response,outputStream);
                            break;
                        }
                        ResponseMessage response = new ResponseMessage("updateCredentialsResponse","401","Utente inesistente");
                        new SendResponse(response,outputStream);
                        break;
                    }
                    case "login": {
                        this.username = messageJson.get("username").getAsString();
                        String psw = messageJson.get("psw").getAsString();
                        if(!registeredUsers.containsKey(username)){
                            ResponseMessage response = new ResponseMessage("loginResponse","401","Utente inesistente");
                            new SendResponse(response,outputStream);
                            System.out.println("DEBUG: Utente non registrato, impossibile fare il login");
                            break;
                        }
                        else if(currentUsers.containsKey(username)){
                            ResponseMessage response = new ResponseMessage("loginResponse","409","Utente già loggato");
                            new SendResponse(response,outputStream);
                            System.out.println("DEBUG: Login già fatto");
                            break;
                        }
                        else if(!registeredUsers.get(username).getPassword().equals(psw)){
                            ResponseMessage response = new ResponseMessage("loginResponse","401","Password errata");
                            new SendResponse(response,outputStream);
                            System.out.println("DEBUG: Password errata");
                            break;
                        }
                        //Utente ha scritto username e password correttamente
                        currentUsers.put(username, this);
                        this.clientUDPport = messageJson.get("udpPort").getAsInt();
                        //Controllo se ha già giocato a questa partita
                        if (this.activePlayers.containsKey(username)) {
                            // Riconnessione: recupera lo stato esistente
                            this.playerStatus = this.activePlayers.get(username);
                            this.playerStatus.setOnline(true);
                            System.out.println("DEBUG: Utente " + username + " rientrato. Parole rimaste: " + this.playerStatus.getWordsLeft().size());
                        } else {
                            // Prima volta in questo turno: crea lo stato e gli assegno le parole da indovinare
                            this.playerStatus = new PlayerStatus(username, true);
                            GameSupport currentGame = this.gameManager.getCurrentGame();
                            if (currentGame != null) {
                                this.playerStatus.setWordsLeft(currentGame.getShuffledWords());
                                this.playerStatus.setGroupWords(currentGame.getGroupWords());
                            }
                            this.activePlayers.put(username, this.playerStatus);
                        }
                        ResponseMessage response = new ResponseMessage("loginResponse","200", "Utente loggato");
                        new SendResponse(response,outputStream);
                        System.out.println("DEBUG: Login effettuato");
                        break;
                    }
                    case  "logout": {
                        System.out.println("DEBUG: Logout");
                        ResponseMessage response = new ResponseMessage("logoutResponse","200", "Logout");
                        new SendResponse(response,outputStream);
                        if(this.playerStatus != null) playerStatus.setOnline(false);
                        if(username != null && currentUsers.containsKey(username))currentUsers.remove(username,this);
                        isRunning = false;
                        break;
                    }
                    //Per ora gestisce SOLO UN GRUPPO ma deve poterne gestire più alla volta
                    case "submitProposal": {
                        Gson gson = new Gson();
                        ProposalMessage proposalMessage = gson.fromJson(messageString, ProposalMessage.class);
                        ArrayList<String> proposal = proposalMessage.getProposal();
                        if(this.playerStatus == null || !this.playerStatus.isOnline()) {
                            ResponseMessage response = new  ResponseMessage("submitProposal","401","Utente non loggato");
                            new SendResponse(response,outputStream);
                            break;
                        };
                        String result = this.playerStatus.removeWordsGuessed(proposal);
                        if(result == null) {
                            ResponseMessage response = new ResponseMessage("submitProposalResponse","401", "Proposta errata");
                            new SendResponse(response,outputStream);
                            break;
                        } else if (result.equals("MALFORMED")) {
                            ResponseMessage response = new ResponseMessage("submitProposalResponse","401", "Proposta malformatata");
                            new SendResponse(response,outputStream);
                            break;
                        }
                        ResponseMessage response = new ResponseMessage("submitProposalResponse","200", "Proposta corretta");
                        new SendResponse(response,outputStream);
                        break;
                    }
                    case "requestGameInfo": {
                        if(this.playerStatus == null || !this.playerStatus.isOnline()) {
                            ResponseMessage response = new  ResponseMessage("requestGameInfoResponse","401","Utente non loggato");
                            new SendResponse(response,outputStream);
                            break;
                        };
                    }
                    case "requestGameStats": {}
                    case "requestLeaderboard": {}
                    case "requestPlayerStats": {}
                }
            }
        } catch (IOException e) {
            System.out.println("Errore: client disconnesso in maniera anomala");
        } finally {
            if(this.playerStatus != null) playerStatus.setOnline(false);
            if(username != null) currentUsers.remove(username,this);
        }
    }

    public void MatchEnd(){
        //Manda un messaggio di fine partita al Client connesso a questo thread
        if(this.playerStatus != null) this.playerStatus.setOnline(false);
        Message endGameMessage = new Message("EndGame");
        Gson gson = new GsonBuilder().create();
        String jsonMessage =  gson.toJson(endGameMessage);
        byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
        int messageLength = jsonMessageBytes.length;
        InetAddress clientAddr = this.clientChannel.getInetAddress();
        int clientPort = this.clientUDPport;
        DatagramPacket packet = new DatagramPacket(jsonMessageBytes,messageLength,clientAddr,clientPort);
        try (DatagramSocket socket = new DatagramSocket();){
            System.out.println("DEBUG: ClientSocket port: " + clientPort);
            socket.send(packet);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Setta la lista di parole da indovinare
    public void setShuffledListOfWords(ArrayList<String> shuffledListOfWords) {
        if(this.playerStatus != null)this.playerStatus.setWordsLeft(shuffledListOfWords);
    }
    // Setta i la lista di parole da indovinare divisa in gruppi per il controllo delle proposte dell'utente
    public void setGroupOfWords(ConcurrentHashMap<String,ArrayList<String>> groupOfWords) {
        if(this.playerStatus != null)this.playerStatus.setGroupWords(groupOfWords);
    }
}
