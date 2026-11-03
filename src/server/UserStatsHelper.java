package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.LeaderboardResponse;
import common.PlayerStatsResponse;
import common.ResponseMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Gestione di requestPlayerStats e requestLeaderboard

public class UserStatsHelper {
    public static void handlePlayerStatsRequest(String username,
                                                int currentGameID,
                                                ConcurrentHashMap<String, ClientHandler> currentUsers,
                                                ConcurrentHashMap<String, User> registeredUsers,
                                                DataOutputStream out) throws IOException {
        // 401 se non autenticato o non presente tra gli utenti connessi
        if (username == null || !currentUsers.containsKey(username)) {
            new SendTCPResponse(new ResponseMessage("requestPlayerStatsResponse", "401", "Accesso non autorizzato. Effettua il login."), out);
            return;
        }

        User user = registeredUsers.get(username);
        if (user == null) {
            new SendTCPResponse(new ResponseMessage("requestPlayerStatsResponse", "404", "Dati utente non trovati."), out);
            return;
        }

        // Creazione della risposta con i dati da memoria (User.java)
        PlayerStatsResponse response = new PlayerStatsResponse("requestPlayerStatsResponse", "200", "Statistiche utente recuperate con successo");
        response.setPuzzlesCompleted(user.getPuzzlesCompleted());
        response.setGamesWon(user.getGamesWon());
        response.setGamesLost(user.getGamesLost());
        response.setWinRate(user.getWinRate());
        response.setLossRate(user.getLossRate());
        response.setCurrentStreak(user.getCurrentStreak());
        response.setMaxStreak(user.getMaxStreak());
        response.setPerfectPuzzles(user.getPerfectPuzzles());

        // Calcolo dinamico dell'istogramma degli errori dai match giocati
        int[] mistakeHistogram = new int[6];
        int gamesTimedOut = 0;

        for (UserMatchResult match : user.getMatchHistory().values()) {
            if (match.isWon() == 1) {
                int mistakes = match.getMistakes();
                if (mistakes >= 0 && mistakes <= 3) {
                    mistakeHistogram[mistakes]++;
                }
            } else if (match.isWon() == -1) {
                mistakeHistogram[4]++;
            } else if (match.isWon() == 0) {
                // Ignora la partita corrente se presente nello storico
                if (match.getGameId() == currentGameID) continue;
                gamesTimedOut++;
                mistakeHistogram[5]++;
            }
        }
        response.setMistakeHistogram(mistakeHistogram);
        response.setGamesTimedOut(gamesTimedOut);
        new SendTCPResponse(response, out);
    }

    public static void handleLeaderboardRequest(String messageString,
                                                String username,
                                                ConcurrentHashMap<String, ClientHandler> currentUsers,
                                                ConcurrentHashMap<String, User> registeredUsers,
                                                DataOutputStream out) throws IOException {
        if (username == null || !currentUsers.containsKey(username)) {
            new SendTCPResponse(new ResponseMessage("requestLeaderboardResponse", "401", "Accesso non autorizzato. Effettua il login."), out);
            return;
        }

        // Parsing del parametro metodoDistampa
        JsonObject jsonObject = JsonParser.parseString(messageString).getAsJsonObject();
        String metodoDistampa = jsonObject.has("metodoDistampa") ? jsonObject.get("metodoDistampa").getAsString() : "total";

        List<User> sortedUsers = registeredUsers.values().stream()
                .sorted((u1, u2) -> Integer.compare(u2.getPoints(), u1.getPoints()))
                .toList();

        List<LeaderBoardEntry> leaderboard = new ArrayList<>();
        int userRank = -1;

        // Calcolo preliminare della posizione dell'utente richiedente (serve sempre)
        for (int i = 0; i < sortedUsers.size(); i++) {
            if (sortedUsers.get(i).getUsername().equals(username)) {
                userRank = i + 1;
                break;
            }
        }

        if (metodoDistampa.equalsIgnoreCase("total")) {
            for (int i = 0; i < sortedUsers.size(); i++) {
                User u = sortedUsers.get(i);
                leaderboard.add(new LeaderBoardEntry(i + 1, u.getPoints(), u.getUsername()));
            }
        } else {
            try {
                // Caso Top K
                int k = Integer.parseInt(metodoDistampa);
                for (int i = 0; i < Math.min(k, sortedUsers.size()); i++) {
                    User u = sortedUsers.get(i);
                    leaderboard.add(new LeaderBoardEntry(i + 1, u.getPoints(), u.getUsername()));
                }
            } catch (NumberFormatException e) {
                // Caso utente specifico
                for (int i = 0; i < sortedUsers.size(); i++) {
                    User u = sortedUsers.get(i);
                    if (u.getUsername().equals(metodoDistampa)) {
                        leaderboard.add(new LeaderBoardEntry(i + 1, u.getPoints(), u.getUsername()));
                        break;
                    }
                }
            }
        }

        LeaderboardResponse response = new LeaderboardResponse("requestLeaderboardResponse", "200", "Classifica recuperata con successo");
        response.setLeaderboard(leaderboard);
        response.setUserRank(userRank);

        new SendTCPResponse(response, out);
    }
}
