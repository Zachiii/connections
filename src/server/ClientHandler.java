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
    private final ConcurrentHashMap<String, User> registeredUsers;
    private final ConcurrentHashMap<String, ClientHandler> currentUsers;
    private ConcurrentHashMap<String, PlayerStatus> activePlayers;
    private PlayerStatus playerStatus = null;
    private String username;
    private int clientUDPport;
    private final GameManager gameManager;

    public ClientHandler(Socket clientChannel,
                         ConcurrentHashMap<String, User> registeredUsers,
                         ConcurrentHashMap<String, ClientHandler> currentUsers,
                         ConcurrentHashMap<String, PlayerStatus> activePlayers,
                         GameManager gameManager) {
        this.clientChannel = clientChannel;
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
        this.activePlayers = activePlayers;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        boolean isRunning = true;
        try (DataInputStream inputStream = new DataInputStream(this.clientChannel.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(this.clientChannel.getOutputStream())) {

            while (isRunning) {
                // Lettura lunghezza prefissa e messaggio JSON
                int messageLength = inputStream.readInt();
                byte[] byteMessage = new byte[messageLength];
                inputStream.readFully(byteMessage);
                String messageString = new String(byteMessage, StandardCharsets.UTF_8);
                JsonObject messageJson = JsonParser.parseString(messageString).getAsJsonObject();
                String operation = messageJson.get("operation").getAsString();

                switch (operation) {
                    case "register": {
                        this.username = messageJson.has("username") ? messageJson.get("username").getAsString() : "";
                        String psw = messageJson.has("psw") ? messageJson.get("psw").getAsString() : "";

                        // 400: Parametri vuoti o mancanti
                        if (Objects.equals(this.username, "") || Objects.equals(psw, "")) {
                            System.out.println("DEBUG: Username o password vuoti");
                            ResponseMessage response = new ResponseMessage("registerResponse", "400", "Username e password non possono essere vuoti");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        // 409: Conflitto - utente già registrato
                        if (registeredUsers.containsKey(username)) {
                            System.out.println("DEBUG: Utente già registrato");
                            ResponseMessage response = new ResponseMessage("registerResponse", "409", "Utente già registrato");
                            new SendTCPResponse(response, outputStream);
                        } else {
                            // 200: Registrazione completata
                            registeredUsers.put(username, new User(username, psw));
                            UserManager.saveUsers(registeredUsers);
                            System.out.println("DEBUG: Utente " + username + " registrato e salvato");
                            ResponseMessage response = new ResponseMessage("registerResponse", "200", "Utente registrato");
                            new SendTCPResponse(response, outputStream);
                        }
                        break;
                    }

                    case "updateCredentials": {
                        String oldUsername = messageJson.has("oldUsername") ? messageJson.get("oldUsername").getAsString() : "";
                        String newUsername = messageJson.has("newUsername") ? messageJson.get("newUsername").getAsString() : "";
                        String oldPsw = messageJson.has("oldPsw") ? messageJson.get("oldPsw").getAsString() : "";
                        String newPsw = messageJson.has("newPsw") ? messageJson.get("newPsw").getAsString() : "";

                        // 400: Campi non validi
                        if (oldUsername.isEmpty() || oldPsw.isEmpty()) {
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse", "400", "I campi username/password correnti sono obbligatori");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        //Devono voler aggiornare almeno uno dei due campi
                        if (newUsername.isEmpty() && newPsw.isEmpty()) {
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse", "400", "Specificare almeno un nuovo username o una nuova password");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        if (!registeredUsers.containsKey(oldUsername)) {
                            // 401: Utente inesistente
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse", "401", "Utente inesistente");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        User currentUser = registeredUsers.get(oldUsername);
                        if (!currentUser.getPassword().equals(oldPsw)) {
                            // 401: Password errata
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse", "401", "Password errata");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        // 409: Il nuovo username scelto è già occupato da un altro utente
                        if (!oldUsername.equals(newUsername) && registeredUsers.containsKey(newUsername)) {
                            ResponseMessage response = new ResponseMessage("updateCredentialsResponse", "409", "Il nuovo username è già in uso");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        // 200: Successo aggiornamento
                        currentUser.updateCredentials(newUsername, newPsw);
                        if (!currentUser.getUsername().equals(oldUsername)) {
                            //Se viene aggiornata solo la password non serve rimuovere e rimettere l'utente
                            registeredUsers.remove(oldUsername);
                            registeredUsers.put(currentUser.getUsername(), currentUser);

                            currentUsers.remove(oldUsername);
                            currentUsers.put(currentUser.getUsername(), this);
                            this.username = currentUser.getUsername();
                        }

                        System.out.println("DEBUG: Credenziali aggiornate");
                        ResponseMessage response = new ResponseMessage("updateCredentialsResponse", "200", "Credenziali aggiornate");
                        new SendTCPResponse(response, outputStream);
                        break;
                    }

                    case "login": {
                        this.username = messageJson.has("username") ? messageJson.get("username").getAsString() : "";
                        String psw = messageJson.has("psw") ? messageJson.get("psw").getAsString() : "";

                        // 400: Parametri mancanti
                        if (this.username.isEmpty() || psw.isEmpty()) {
                            ResponseMessage response = new ResponseMessage("loginResponse", "400", "Username e password obbligatori");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        // 401: Utente inesistente
                        if (!registeredUsers.containsKey(username)) {
                            ResponseMessage response = new ResponseMessage("loginResponse", "401", "Utente inesistente");
                            new SendTCPResponse(response, outputStream);
                            System.out.println("DEBUG: Utente non registrato, login fallito");
                            break;
                        }

                        // 409: Conflitto - sessione già attiva
                        if (currentUsers.containsKey(username)) {
                            ResponseMessage response = new ResponseMessage("loginResponse", "409", "Utente già loggato");
                            new SendTCPResponse(response, outputStream);
                            System.out.println("DEBUG: Login già fatto: " + this.username);
                            break;
                        }

                        // 401: Password non corretta
                        if (!registeredUsers.get(username).getPassword().equals(psw)) {
                            ResponseMessage response = new ResponseMessage("loginResponse", "401", "Password errata");
                            new SendTCPResponse(response, outputStream);
                            System.out.println("DEBUG: Password errata");
                            break;
                        }

                        // 200: Login riuscito
                        currentUsers.put(username, this);
                        this.clientUDPport = messageJson.has("udpPort") ? messageJson.get("udpPort").getAsInt() : 0;

                        if (this.activePlayers.containsKey(username)) {
                            // Riconnessione: recupera la sessione parziale dell'utente
                            this.playerStatus = this.activePlayers.get(username);
                            this.playerStatus.setOnline(true);
                            System.out.println("DEBUG: Utente " + username + " rientrato. Parole rimaste: " + this.playerStatus.getWordsLeft().size());
                        } else {
                            // Prima connessione in questo round
                            this.playerStatus = new PlayerStatus(username, true);
                            GameSupport currentGame = this.gameManager.getCurrentGame();
                            if (currentGame != null) {
                                this.playerStatus.setWordsLeft(currentGame.getShuffledWords());
                                this.playerStatus.setGroupWords(currentGame.getGroupWords());
                            }
                            System.out.println("DEBUG: Login: " + this.username);
                            this.activePlayers.put(username, this.playerStatus);
                        }

                        GameInfoResponse response = new GameInfoResponse("loginResponse", "200", "Utente loggato");
                        response.setWon(this.playerStatus.isWon());
                        response.setWordsLeft(this.playerStatus.getWordsLeft());
                        new SendTCPResponse(response, outputStream);
                        System.out.println("DEBUG: Login effettuato");
                        break;
                    }

                    case "logout": {
                        System.out.println("DEBUG: Logout: " + this.username);
                        if (this.playerStatus != null) this.playerStatus.setOnline(false);
                        if (this.username != null) currentUsers.remove(this.username, this);

                        // 200: Logout completato
                        ResponseMessage response = new ResponseMessage("logoutResponse", "200", "Logout effettuato");
                        new SendTCPResponse(response, outputStream);
                        isRunning = false;
                        break;
                    }

                    case "submitProposal": {
                        // 401: Tentativo di gioco senza sessione attiva
                        if (this.playerStatus == null || !this.playerStatus.isOnline()) {
                            ResponseMessage response = new ResponseMessage("submitProposalResponse", "401", "Accesso non autorizzato: effettua prima il login");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        // 403: Partita già terminata
                        if (this.playerStatus.isGameEnded()) {
                            ResponseMessage response = new ResponseMessage("submitProposalResponse", "403", "Partita già conclusa: vite esaurite");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }

                        Gson gson = new Gson();
                        ProposalMessage proposalMessage = gson.fromJson(messageString, ProposalMessage.class);
                        ArrayList<String> proposal = proposalMessage.getProposal();
                        String result = this.playerStatus.removeWordsGuessed(proposal);

                        if (result == null) {
                            // 422: Proposta formata regolarmente ma errata (-1 vita, -4 punti)
                            ResponseMessage response = new ResponseMessage(
                                    "submitProposalResponse",
                                    "422",
                                    "Proposta errata: combinazione non valida. Vite rimaste: " + (4 - this.playerStatus.getMistakes())
                            );
                            new SendTCPResponse(response, outputStream);
                            break;
                        } else if (result.equals("MALFORMED")) {
                            // 400: Proposta malformata (non sono 4 parole o parole non presenti)
                            ResponseMessage response = new ResponseMessage(
                                    "submitProposalResponse",
                                    "400",
                                    "Proposta malformata: inviare 4 parole valide ancora in gioco (nessuna penalità applicata)"
                            );
                            new SendTCPResponse(response, outputStream);
                            break;
                        } else if (result.equals("ENDGAME")) {
                            // 403 / Fine vite: notifica esaurimento e fine partita
                            this.registeredUsers.get(username).resetStreak();
                            ResponseMessage response = new ResponseMessage(
                                    "submitProposalResponse",
                                    "403",
                                    "Vite esaurite: partita terminata"
                            );
                            new SendTCPResponse(response, outputStream);
                            this.MatchEnd();
                            break;
                        } else if (result.equals("WIN")) {
                            // 200: Vittoria partita
                            this.registeredUsers.get(username).addStreak();
                            ResponseMessage response = new ResponseMessage("submitProposalResponse", "200", "Complimenti! Hai completato tutti i gruppi e vinto la partita!");
                            new SendTCPResponse(response, outputStream);
                            this.MatchEnd();
                            break;
                        }
                        // 200: Gruppo indovinato
                        ResponseMessage response = new ResponseMessage("submitProposalResponse", "200", "Gruppo corretto: " + result);
                        new SendTCPResponse(response, outputStream);
                        break;
                    }

                    case "requestGameInfo": {
                        // 401: Non autenticato
                        if (this.playerStatus == null || !this.playerStatus.isOnline()) {
                            ResponseMessage response = new ResponseMessage("requestGameInfoResponse", "401", "Utente non loggato");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        String askedGameID = messageJson.has("gameID") ? messageJson.get("gameID").getAsString() : "";
                        // 400: Non ha scritto il gameID
                        if(askedGameID.equals("")) {
                            ResponseMessage response = new ResponseMessage("requestGameInfoResponse", "400", "Devi scrivere l'ID di una partita");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        // 404: Ha scritto un gameID non esistente
                        if(!askedGameID.equals("current") && (Integer.parseInt(askedGameID) > this.gameManager.getGameID())) {
                            ResponseMessage response = new ResponseMessage("requestGameInfoResponse", "404", "Questo ID non esiste");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        // 200: Invio stato della partita
                        GameInfoResponse response = createGameInfoResponse(askedGameID);
                        new SendTCPResponse(response, outputStream);
                        break;
                    }

                    case "requestGameStats": {
                        // 401 se non autenticato
                        if (this.username == null || !currentUsers.containsKey(this.username)) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "401", "Accesso non autorizzato");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        // 200 quando restituisci i dati richiesti
                        String gameID = messageJson.has("gameID") ? messageJson.get("gameID").getAsString() : "";
                        if(gameID.equals("current")) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "200", "Dati recuperati con successo");
                            new SendTCPResponse(response, outputStream);
                            break;
                        } else if (!gameID.isEmpty()) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "200", "Dati recuperati con successo");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        break;
                    }
                    case "requestLeaderboard":{
                        // 401 se non autenticato
                        if (this.username == null || !currentUsers.containsKey(this.username)) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "401", "Accesso non autorizzato");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        // 200 quando restituisci i dati richiesti
                        String gameID = messageJson.has("gameID") ? messageJson.get("gameID").getAsString() : "";
                        if(gameID.equals("current")) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "200", "Dati recuperati con successo");
                            new SendTCPResponse(response, outputStream);
                            break;
                        } else if (!gameID.isEmpty()) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "200", "Dati recuperati con successo");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        break;
                    }
                    case "requestPlayerStats": {
                        // 401 se non autenticato
                        if (this.username == null || !currentUsers.containsKey(this.username)) {
                            ResponseMessage response = new ResponseMessage(operation + "Response", "401", "Accesso non autorizzato");
                            new SendTCPResponse(response, outputStream);
                            break;
                        }
                        // 200 quando restituisci i dati richiesti
                        ResponseMessage response = new ResponseMessage(operation + "Response", "200", "Dati recuperati con successo");
                        new SendTCPResponse(response, outputStream);
                        break;
                    }

                    default: {
                        // 400: Comando non riconosciuto
                        ResponseMessage response = new ResponseMessage("unknownResponse", "400", "Operazione non supportata");
                        new SendTCPResponse(response, outputStream);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Errore: client disconnesso in maniera anomala");
        } finally {
            if (this.playerStatus != null) this.playerStatus.setOnline(false);
            if (this.username != null) currentUsers.remove(this.username, this);
        }
    }

    private GameInfoResponse createGameInfoResponse(String askedGameID) {
        GameInfoResponse response = new GameInfoResponse("requestGameInfoResponse", "200", "Informazioni partita recuperate");
        int currentGameID = this.gameManager.getGameID();
        if(askedGameID.equals("current")) {
            response.setGameID(this.gameManager.getGameID());
            response.setWon(this.playerStatus.isWon());
            response.setTimeLeft(this.gameManager.getRemainingTimeSeconds());
            response.setPoints(this.playerStatus.getScore());
            response.setTimeLeft(this.gameManager.getRemainingTimeSeconds());
            response.setWordsLeft(this.playerStatus.getWordsLeft());
            response.setMistakes(this.playerStatus.getMistakes());
            response.setWordsGuessed(this.playerStatus.getWordsGuessed());
            return response;

        }
        int gameID = Integer.parseInt(askedGameID);
        UserMatchResult matchResult = registeredUsers.get(this.username).getMatchResult(gameID);
        response.setGameID(this.gameManager.getGameID());
        response.setWon(matchResult.isWon());
        response.setPoints(matchResult.getScore());
        response.setMistakes(matchResult.getMistakes());
        response.setNumberCorrectGuesses(matchResult.getCorrectGroups());
        return response;
    }

    //AGGIUNGERE LA POSSIBILITà PER L'UTENTE DI SCEGLIERE SE RIMANERE LOGGATO O MENO
    public void MatchEnd() {
        this.playerStatus.setScore();
        this.registeredUsers.get(username).setPoints(this.playerStatus.getScore());
        UserMatchResult matchResult = new UserMatchResult(
                gameManager.getGameID(),
                playerStatus.getScore(),
                playerStatus.getMistakes(),
                playerStatus.getWordsGuessed().size(),
                playerStatus.isWon()
        );
        registeredUsers.get(this.username).addMatchResult(matchResult);
        UserManager.saveUsers(registeredUsers);
        // Notifiche UDP di fine partita con codice 500
        Message endGameMessage = new Message("EndGame");
        this.sendUDPMessage(endGameMessage);
        ResponseMessage endGameStats = new ResponseMessage("EndGameStats", "500", "Partita finita");
        this.sendUDPMessage(endGameStats);
    }

    public void sendUDPMessage(Message message) {
        if (this.clientUDPport <= 0) return;
        Gson gson = new GsonBuilder().create();
        String jsonMessage = gson.toJson(message);
        byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
        InetAddress clientAddr = this.clientChannel.getInetAddress();
        DatagramPacket packet = new DatagramPacket(jsonMessageBytes, jsonMessageBytes.length, clientAddr, this.clientUDPport);
        try (DatagramSocket socket = new DatagramSocket()) {
            System.out.println("DEBUG: Inviata notifica UDP a porta: " + this.clientUDPport);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Errore invio UDP: " + e.getMessage());
        }
    }

    public void setShuffledListOfWords(ArrayList<String> shuffledListOfWords) {
        if (this.playerStatus != null) this.playerStatus.setWordsLeft(shuffledListOfWords);
    }

    public void setGroupOfWords(ConcurrentHashMap<String, ArrayList<String>> groupOfWords) {
        if (this.playerStatus != null) this.playerStatus.setGroupWords(groupOfWords);
    }
}