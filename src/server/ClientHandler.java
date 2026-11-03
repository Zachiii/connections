package server;

import com.google.gson.*;
import common.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// Thread che connette il client al server, gestisce i comandi fatti dal client (register, login, ...)

public class ClientHandler implements Runnable {
    private final Socket clientChannel;
    private final ConcurrentLinkedQueue<ClientHandler> allClients;
    private final ConcurrentHashMap<String, User> registeredUsers;
    private final ConcurrentHashMap<String, ClientHandler> currentUsers;
    private final ConcurrentHashMap<String, PlayerStatus> activePlayers;
    private final ConcurrentHashMap<String, GameStats> gameStats;
    PlayerStatus playerStatus = null;
    private String username;
    private int clientUDPport;
    private volatile boolean isRunning = true;
    private final GameTimeManager gameTimeManager;

    public ClientHandler(Socket clientChannel,
                         ConcurrentLinkedQueue<ClientHandler> allClients,
                         ConcurrentHashMap<String, User> registeredUsers,
                         ConcurrentHashMap<String, ClientHandler> currentUsers,
                         ConcurrentHashMap<String, PlayerStatus> activePlayers,
                         ConcurrentHashMap<String, GameStats> gameStats,
                         GameTimeManager gameTimeManager) {
        this.clientChannel = clientChannel;
        this.allClients = allClients;
        this.allClients.add(this);
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
        this.activePlayers = activePlayers;
        this.gameStats = gameStats;
        this.gameTimeManager = gameTimeManager;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(this.clientChannel.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(this.clientChannel.getOutputStream())) {

            while (this.isRunning) {
                // Lettura lunghezza prefissa e messaggio JSON
                int messageLength = inputStream.readInt();
                byte[] byteMessage = new byte[messageLength];
                inputStream.readFully(byteMessage);
                String messageString = new String(byteMessage, StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(messageString).getAsJsonObject();
                String operation = jsonObject.get("operation").getAsString();

                switch (operation) {
                    case "register": {
                        AuthHelper.handleRegister(messageString, this,this.registeredUsers, outputStream);
                        break;
                    }
                    case "updateCredentials": {
                        AuthHelper.handleUpdateCredentials(messageString,this,this.registeredUsers,this.currentUsers,outputStream);
                    }
                    case "login": {
                        AuthHelper.handleLogin(messageString, this, this.registeredUsers, this.currentUsers, this.activePlayers, this.gameStats,this.gameTimeManager,outputStream);
                        break;
                    }
                    case "logout": {
                        System.out.println("DEBUG: Logout: " + this.username);
                        if (this.playerStatus != null) {
                            this.playerStatus.setOnline(false);}
                        if (this.username != null) currentUsers.remove(this.username, this);

                        // 200: Logout completato
                        ResponseMessage response = new ResponseMessage("logoutResponse", "200", "Logout effettuato");
                        new SendTCPResponse(response, outputStream);
                        this.isRunning = false;
                        break;
                    }
                    case "submitProposal": {
                        GameProposalHelper session = new GameProposalHelper(this.playerStatus, this.clientChannel.getInetAddress(), this.clientUDPport);
                        session.handleProposalRequest(messageString,this,outputStream);
                        break;
                    }
                    case "requestGameInfo": {
                        StatsRequestHelper.handleGameInfoRequest(messageString, this.username, this.currentUsers,
                                this.registeredUsers, this.playerStatus, this.gameTimeManager, outputStream);
                        break;
                    }
                    case "requestGameStats": {
                        StatsRequestHelper.handleGameStatsRequest(messageString, this.username, this.currentUsers,
                                this.gameStats, this.gameTimeManager, outputStream);
                        break;
                    }
                    case "requestLeaderboard":{
                        UserStatsHelper.handleLeaderboardRequest(messageString, this.username, this.currentUsers, this.registeredUsers, outputStream);
                        break;
                    }
                    case "requestPlayerStats": {
                        UserStatsHelper.handlePlayerStatsRequest(this.username,this.gameTimeManager.getGameID(),this.currentUsers, this.registeredUsers, outputStream);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Errore: client disconnesso in maniera anomala");
        } finally {
            if (this.playerStatus != null) this.playerStatus.setOnline(false);
            if (this.username != null) currentUsers.remove(this.username, this);
            this.allClients.remove(this);
        }
    }

    public void MatchEnd() {
        int gameID = this.gameTimeManager.getGameID();

        // Salvataggio statistiche utente
        saveUserStats(gameID);

        // Aggiornamento statistiche partita
        updateGameStats(gameID);

        //Creazione messaggio di EndGame
        System.out.println("DEBUG: MatchEnd: " + this.username);
        GameEndResponse endGameMessage = new GameEndResponse("EndGame","200","GiocoFinito");
        endGameMessage.setGameID(gameID);
        endGameMessage.setTimeLeft(this.gameTimeManager.getRemainingTimeSeconds());
        endGameMessage.setWordsGuessed(this.playerStatus.getWordsGuessed());
        endGameMessage.setWordsLeft(this.playerStatus.getWordsLeft());
        endGameMessage.setMistakes(this.playerStatus.getMistakes());
        endGameMessage.setPlayerPoints(this.playerStatus.getScore());
        endGameMessage.setWon(this.playerStatus.isWon());
        endGameMessage.setWordsLeft(this.playerStatus.getWordsLeft());
        endGameMessage.setAveragePoints(this.gameStats.get(String.valueOf(gameID)).getAveragePoints());
        endGameMessage.setNumberOfPlayers(this.gameStats.get(String.valueOf(gameID)).getNumberOfPlayers());
        endGameMessage.setGamesLost(this.gameStats.get(String.valueOf(gameID)).getGamesLost());
        endGameMessage.setGamesWon(this.gameStats.get(String.valueOf(gameID)).getGamesWon());
        InetAddress clientAddr = this.clientChannel.getInetAddress();
        UDPNotificationManager.sendNotification(this.clientChannel.getInetAddress(),this.clientUDPport, endGameMessage);
    }

    public void saveUserStats(int gameID){
        this.playerStatus.setScore();
        User user = this.registeredUsers.get(this.username);
        user.setPoints(this.playerStatus.getScore());
        UserMatchResult matchResult = new UserMatchResult(
                gameID,
                this.playerStatus.getScore(),
                this.playerStatus.getMistakes(),
                this.playerStatus.getWordsGuessed().size(),
                this.playerStatus.isWon()
        );
        user.addMatchResult(matchResult);
        if(this.playerStatus.isWon() == 1){
            user.addStreak();
        } else if (this.playerStatus.isWon() == -1) {
            user.resetStreak();
        }
        System.out.println("DEBUG: User aggiornato: " + this.username);
        UserSerializer.saveUsers(registeredUsers);
    }

    public void updateGameStats(int gameID){
        this.gameStats.get(String.valueOf(gameID)).setTotalPoints(this.playerStatus.getScore());
        if(this.playerStatus.isWon() == -1){this.gameStats.get(String.valueOf(gameID)).setGamesLost();}
        else if(this.playerStatus.isWon() == 1){this.gameStats.get(String.valueOf(gameID)).setGamesWon();}
    }

    public void closeConnection() {
        System.out.println("DEBUG: statistiche: " + this.username + " aggiornate");
        this.isRunning = false;
        try {
            if (this.clientChannel != null && !this.clientChannel.isClosed()) {
                this.clientChannel.close(); // La chiusura del socket sblocca e fa fallire inputStream.readInt()
            }
        } catch (IOException ignored) {}
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setClientUDPport(int clientUDPport) {
        this.clientUDPport = clientUDPport;
    }
    public String getUsername(){return this.username;}
}