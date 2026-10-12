package server;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WordReader {
    ConcurrentHashMap<String,ClientHandler> currentUsers;
    int gameID;

    public static List<GameSupport> loadGames(String filePath) {
        Gson gson = new Gson();
        TypeToken<List<GameSupport>> listType = new TypeToken<List<GameSupport>>() {};

        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
            return null;
        }
    }

    //Legge da Connections_Data.json randomizza le parole per ogni utente
    public WordReader(ConcurrentHashMap<String,ClientHandler> currentUsers, int gameID) {
        this.currentUsers = currentUsers;
        this.gameID = gameID;
        List<GameSupport> games = loadGames("src/server/Connections_Data.json");

        if (games != null && !games.isEmpty()) {
            GameSupport game = games.get(this.gameID);

            System.out.println("DEBUG: Partita caricata con ID: " + game.getGameId());
            System.out.println("DEBUG: Numero di gruppi: " + game.getGroups().size());

            // Stampa di riepilogo dei gruppi della partita
            for (GroupSupport gruppo : game.getGroups()) {
                System.out.println("DEBUG: Tema: " + gruppo.getTheme() + " -> Parole: " + gruppo.getWords());
            }

            //Setta la lista di parole da indovinare ad ogni utente loggato
            for (ClientHandler user:  currentUsers.values()) {
                user.setListOfWords(game.getShuffledWords());
            }
        }
    }

}
