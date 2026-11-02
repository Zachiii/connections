package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.GameInfoResponse;
import common.GameStatsResponse;
import common.ResponseMessage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class StatsHelper {

    /*
    * validateID == -2 --> ID non corretto
    * validateID == -1 --> ID partita corrente
    * else ID corretto di una partita passata
    * */

    public static void handleGameInfoRequest(String jsonMessage, String username,
                                             ConcurrentHashMap<String, ClientHandler> currentUsers,
                                             ConcurrentHashMap<String, User> registeredUsers,
                                             PlayerStatus status,
                                             GameTimeManager gameManager,
                                             DataOutputStream out) throws IOException {
        JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();
        int validatedId = validateGameRequest(jsonObject, "requestGameInfoResponse", username, currentUsers, gameManager, out);
        if (validatedId == -2) {
            return;} // Controllo fallito: errore già inviato via TCP
        GameInfoResponse response = createGameInfoResponse(validatedId, username, registeredUsers, status, gameManager);
        new SendTCPResponse(response, out);
    }

    public static void handleGameStatsRequest(String jsonMessage, String username,
                                              ConcurrentHashMap<String, ClientHandler> currentUsers,
                                              ConcurrentHashMap<String, GameStats> gameStatsMap,
                                              GameTimeManager gameManager,
                                              DataOutputStream out) throws IOException {
        JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();
        int validatedId = validateGameRequest(jsonObject, "requestGameStatsResponse", username, currentUsers, gameManager, out);
        if (validatedId == -2) return; // Controllo fallito: errore già inviato via TCP

        GameStatsResponse response = createGameStatsResponse(validatedId, gameStatsMap, gameManager);
        new SendTCPResponse(response, out);
    }

    private static int validateGameRequest(JsonObject messageJson, String responseOp, String username,
                                           ConcurrentHashMap<String, ClientHandler> currentUsers,
                                           GameTimeManager gameManager, DataOutputStream out) throws IOException {
        if (username == null || !currentUsers.containsKey(username)) {
            new SendTCPResponse(new ResponseMessage(responseOp, "401", "Accesso non autorizzato. Effettua il login."), out);
            return -2;
        }

        String askedGameID = messageJson.has("gameID") ? messageJson.get("gameID").getAsString().trim() : "";

        if (askedGameID.isEmpty()) {
            new SendTCPResponse(new ResponseMessage(responseOp, "400", "Parametro gameID mancante. Specifica 'current' o un numero."), out);
            return -2;
        }

        if (askedGameID.equalsIgnoreCase("current")) {
            return -1;
        }

        try {
            int parsedId = Integer.parseInt(askedGameID);
            int currentGameID = (gameManager != null) ? gameManager.getGameID() : 0;

            if (parsedId < 0 || parsedId > currentGameID) {
                new SendTCPResponse(new ResponseMessage(responseOp, "404", "Questo ID partita non esiste o la partita non è ancora iniziata."), out);
                return -2;
            }
            return parsedId;

        } catch (NumberFormatException e) {
            new SendTCPResponse(new ResponseMessage(responseOp, "400", "L'ID della partita deve essere un numero intero positivo o la stringa 'current'."), out);
            return -2;
        }
    }

    private static GameInfoResponse createGameInfoResponse(int targetId, String username,
                                                           ConcurrentHashMap<String, User> registeredUsers,
                                                           PlayerStatus status, GameTimeManager gameManager) {
        if (targetId == -1) {
            GameInfoResponse response = new GameInfoResponse("requestGameInfoResponse", "200", "Informazioni partita recuperate con successo");
            response.setGameID(gameManager.getGameID());

            if (status != null) {
                response.setWon(status.isWon());
                response.setPoints(status.getScore());
                response.setMistakes(status.getMistakes());
                response.setWordsLeft(status.getWordsLeft());
                response.setWordsGuessed(status.getWordsGuessed());
                response.setTimeLeft(gameManager.getRemainingTimeSeconds());
            }
            return response;
        }

        User currentUser = registeredUsers.get(username);
        UserMatchResult matchResult = (currentUser != null) ? currentUser.getMatchResult(targetId) : null;

        if (matchResult == null) {
            return new GameInfoResponse("requestGameInfoResponse", "404", "Non hai partecipato alla partita " + targetId + " oppure i dati non sono disponibili.");
        }

        GameInfoResponse response = new GameInfoResponse("requestGameInfoResponse", "200", "Storico partita recuperato con successo");
        response.setGameID(targetId);
        response.setWon(matchResult.isWon());
        response.setPoints(matchResult.getScore());
        response.setMistakes(matchResult.getMistakes());
        response.setNumberCorrectGuesses(matchResult.getCorrectGroups());
        return response;
    }

    private static GameStatsResponse createGameStatsResponse(int targetId, ConcurrentHashMap<String, GameStats> gameStatsMap, GameTimeManager gameManager) {
        boolean isCurrent = (targetId == -1);
        int realGameId = isCurrent ? gameManager.getGameID() : targetId;

        GameStats stats = (gameStatsMap != null) ? gameStatsMap.get(String.valueOf(realGameId)) : null;

        if (stats == null) {
            return new GameStatsResponse("requestGameStatsResponse", "404", "Statistiche non trovate per la partita " + realGameId);
        }

        GameStatsResponse response = new GameStatsResponse("requestGameStatsResponse", "200", "Statistiche recuperate con successo");
        response.setGameID(realGameId);
        response.setGameEnded(!isCurrent);
        response.setGamesWon(stats.getGamesWon());
        response.setGamesLost(stats.getGamesLost());
        response.setNumberOfPlayers(stats.getNumberOfPlayers());
        response.setTimeLeft(gameManager.getRemainingTimeSeconds());

        if (!isCurrent) {
            int players = stats.getNumberOfPlayers();
            response.setAveragePoints(players > 0 ? ((double) stats.getTotalPoints() / players) : 0.0);
        }

        return response;
    }
}