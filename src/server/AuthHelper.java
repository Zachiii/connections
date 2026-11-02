package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.GameInfoResponse;
import common.RegisterMessage;
import common.ResponseMessage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class AuthHelper {
    // Gestione dei comandi di register, login e updateCredentials
    public static void handleRegister(String jsonMessage,
                                      ClientHandler client,
                                      ConcurrentHashMap<String, User> registeredUsers,
                                      DataOutputStream out) throws IOException {
        Gson gson = new Gson();
        RegisterMessage message = gson.fromJson(jsonMessage, RegisterMessage.class);

        // Controllo se i campi sono null o vuoti
        if (message.getUsername() == null || message.getUsername().trim().isEmpty() ||
                message.getPsw() == null || message.getPsw().trim().isEmpty()) {
            new SendTCPResponse(new ResponseMessage("registerResponse", "400", "Username o password vuoti/mancanti"), out);
            return;
        }

        if (registeredUsers.putIfAbsent(message.getUsername(), new User(message.getUsername(), message.getPsw())) == null) {
            UserManager.saveUsers(registeredUsers);
            client.setUsername(message.getUsername().trim());
            System.out.println("DEBUG: Registrazione effettuata: " + message.getUsername().trim());
            new SendTCPResponse(new ResponseMessage("registerResponse", "200", "Registrazione completata"), out);
        } else {
            new SendTCPResponse(new ResponseMessage("registerResponse", "409", "Username già registrato"), out);
        }
    }

    public static void handleLogin(String jsonMessage, ClientHandler client,
                                   ConcurrentHashMap<String, User> registeredUsers,
                                   ConcurrentHashMap<String, ClientHandler> currentUsers,
                                   ConcurrentHashMap<String, PlayerStatus> activePlayers,
                                   ConcurrentHashMap<String, GameStats> gameStats,
                                   GameTimeManager gameManager, DataOutputStream out) throws IOException {

        JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();

        // Estrazione sicura per evitare NullPointerException se le chiavi mancano
        String username = jsonObject.has("username") ? jsonObject.get("username").getAsString() : null;
        String psw = jsonObject.has("psw") ? jsonObject.get("psw").getAsString() : null;

        // Controllo se i campi sono null o vuoti
        if (username == null || username.trim().isEmpty() ||
                psw == null || psw.trim().isEmpty()) {
            new SendTCPResponse(new ResponseMessage("loginResponse", "400", "Username o password vuoti/mancanti"), out);
            return;
        }

        // Controllo se l'utente si era già registrato o meno
        User user = registeredUsers.get(username);

        if (user == null || !user.getPassword().equals(psw)) {
            new SendTCPResponse(new ResponseMessage("loginResponse", "401", "Username inesistente o password errata"), out);
            return;
        }
        if (currentUsers.containsKey(username)) {
            new SendTCPResponse(new ResponseMessage("loginResponse", "409", "Utente già connesso su un'altra sessione"), out);
            return;
        }

        // this.clientUDPport ,this.currentUsers e this.playerStatus in ClientHandler
        client.setUsername(username);
        if (jsonObject.has("udpPort")) {
            client.setClientUDPport(jsonObject.get("udpPort").getAsInt());
        }
        currentUsers.put(username, client);
        if (activePlayers.containsKey(username)) {
            // Non è il primo login durante questa partita quindi vado a recuperare le informazioni già possedute in PlayerStatus
            client.playerStatus = activePlayers.get(username);
            client.playerStatus.setOnline(true);
        } else {
            // Primo login quindi devo popolare anche PlayerStatus che va a gestire le statistiche del giocatore durante la partita
            client.playerStatus = new PlayerStatus(username, true);
            GameSupport activeGame = gameManager.getCurrentGame();
            if (activeGame != null) {
                client.playerStatus.setWordsLeft(activeGame.getShuffledWords());
                client.playerStatus.setGroupWords(activeGame.getGroupWords());
            }
            client.playerStatus.setOnline(true);
            gameStats.get(String.valueOf(gameManager.getGameID())).setNumberOfPlayers();
            activePlayers.put(username, client.playerStatus);
        }

        GameInfoResponse response = new GameInfoResponse("loginResponse", "200", "Utente loggato");
        response.setWon(client.playerStatus.isWon());
        response.setWordsLeft(client.playerStatus.getWordsLeft());
        new SendTCPResponse(response, out);
        System.out.println("DEBUG: Login effettuato: " + username);
    }

    public static void handleUpdateCredentials(String jsonMessage, ClientHandler client,
                                               ConcurrentHashMap<String, User> registeredUsers,
                                               ConcurrentHashMap<String, ClientHandler> currentUsers,
                                               DataOutputStream out) throws IOException {

        JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();

        String oldUsername = jsonObject.has("oldUsername") ? jsonObject.get("oldUsername").getAsString().trim() : "";
        String newUsername = jsonObject.has("newUsername") ? jsonObject.get("newUsername").getAsString().trim() : "";
        String oldPsw = jsonObject.has("oldPsw") ? jsonObject.get("oldPsw").getAsString().trim() : "";
        String newPsw = jsonObject.has("newPsw") ? jsonObject.get("newPsw").getAsString().trim() : "";

        // I campi vecchi devono essere sempre presenti
        if (oldUsername.isEmpty() || oldPsw.isEmpty()) {
            new SendTCPResponse(new ResponseMessage("updateCredentialsResponse", "400", "Username vecchio e password vecchia sono obbligatori"), out);
            return;
        }
        // Almeno uno dei nuovi campi deve essere compilato
        if (newUsername.isEmpty() && newPsw.isEmpty()) {
            new SendTCPResponse(new ResponseMessage("updateCredentialsResponse", "400", "Specificare almeno un nuovo username o una nuova password"), out);
            return;
        }
        // Verifica esistenza vecchio account
        if (!registeredUsers.containsKey(oldUsername)) {
            new SendTCPResponse(new ResponseMessage("updateCredentialsResponse", "401", "Utente inesistente"), out);
            return;
        }
        User currentUser = registeredUsers.get(oldUsername);
        if (!currentUser.getPassword().equals(oldPsw)) {
            new SendTCPResponse(new ResponseMessage("updateCredentialsResponse", "401", "Password errata"), out);
            return;
        }
        // Se vuole cambiare username, controlla che non sia già occupato da qualcun altro
        if (!newUsername.isEmpty() && !newUsername.equals(oldUsername) && registeredUsers.containsKey(newUsername)) {
            new SendTCPResponse(new ResponseMessage("updateCredentialsResponse", "409", "Il nuovo username scelto è già occupato"), out);
            return;
        }
        currentUser.updateCredentials(newUsername, newPsw);
        // Se lo username è effettivamente cambiato, aggiorna le chiavi delle mappe
        if (!currentUser.getUsername().equals(oldUsername)) {
            registeredUsers.remove(oldUsername);
            registeredUsers.put(currentUser.getUsername(), currentUser);

            currentUsers.remove(oldUsername);
            currentUsers.put(currentUser.getUsername(), client);
            client.setUsername(currentUser.getUsername());
        }
        UserManager.saveUsers(registeredUsers);

        System.out.println("DEBUG: Credenziali aggiornate per " + currentUser.getUsername());
        new SendTCPResponse(new ResponseMessage("updateCredentialsResponse", "200", "Credenziali aggiornate con successo"), out);
    }
}