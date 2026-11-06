package server;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WordReader {
    private List<GameWordsRandomizer> allGames;
    ConcurrentHashMap<String,ClientHandler> currentUsers;
    public WordReader(){
        Gson gson = new Gson();
        Type listType = new TypeToken<List<GameWordsRandomizer>>() {}.getType();
        try (FileReader reader = new FileReader("src/server/Connections_Data.json")) {
            allGames = gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("[SERVER] Errore caricamento partite: " + e.getMessage());
        }
    }
    // Ritorna la singola partita dato il gameID
    public GameWordsRandomizer getGame(int gameId) {
        if (allGames != null && gameId >= 0 && gameId < allGames.size()) {
            return allGames.get(gameId);
        }
        return null;
    }
}
