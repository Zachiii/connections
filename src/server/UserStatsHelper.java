package server;

import common.PlayerStatsResponse;
import common.ResponseMessage;

import java.io.DataOutputStream;
import java.io.IOException;
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

    /*
    public static void handleLeaderboardRequest(String username,
                                                ConcurrentHashMap<String, ClientHandler> currentUsers,
                                                ConcurrentHashMap<String, User> registeredUsers,
                                                DataOutputStream out) throws IOException {
        // 401 se non autenticato
        if (username == null || !currentUsers.containsKey(username)) {
            new SendTCPResponse(new ResponseMessage("requestLeaderboardResponse", "401", "Accesso non autorizzato. Effettua il login."), out);
            return;
        }

        // Ordinamento degli utenti in RAM per punteggio decrescente
        List<User> sortedUsers = registeredUsers.values().stream()
                .sorted((u1, u2) -> Integer.compare(u2.getPoints(), u1.getPoints()))
                .toList();

        // Costruzione della lista di entry (es. username e punti) e recupero posizione
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        int userRank = -1;

        for (int i = 0; i < sortedUsers.size(); i++) {
            User u = sortedUsers.get(i);
            int rank = i + 1;
            leaderboard.add(new LeaderboardEntry(rank, u.getUsername(), u.getPoints()));
            if (u.getUsername().equals(username)) {
                userRank = rank;
            }
        }

        LeaderboardResponse response = new LeaderboardResponse("requestLeaderboardResponse", "200", "Classifica recuperata con successo");
        response.setLeaderboard(leaderboard);
        response.setUserRank(userRank);

        new SendTCPResponse(response, out);
    }
    */
}
