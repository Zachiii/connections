package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;

// Serializzazione di activePlayers

public class ActiveMatchSerializer {
    private static final String TARGET_PATH = "src/server/active_match.json";
    private static final String TEMP_PATH = "src/server/active_match_temp.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Carica la partita lasciata in sospeso al riavvio del server
    public static ConcurrentHashMap<String, PlayerStatus> loadActiveMatch() {
        File file = new File(TARGET_PATH);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<String, PlayerStatus>>() {}.getType();
            ConcurrentHashMap<String, PlayerStatus> activePlayers = gson.fromJson(reader, type);
            return (activePlayers != null) ? activePlayers : new ConcurrentHashMap<>();
        } catch (IOException e) {
            System.err.println("[SERVER] Errore caricamento stato partita in corso: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    // Salva lo stato temporaneo della partita in corso allo spegnimento
    public static synchronized void saveActiveMatch(ConcurrentHashMap<String, PlayerStatus> activePlayers) {
        if (activePlayers == null || activePlayers.isEmpty()) {
            clearActiveMatch();
            return;
        }

        Path tempFile = Paths.get(TEMP_PATH);
        Path targetFile = Paths.get(TARGET_PATH);

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            gson.toJson(activePlayers, writer);
        } catch (IOException e) {
            System.err.println("[SERVER] Errore scrittura file temporaneo active_match: " + e.getMessage());
            return;
        }

        try {
            Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.err.println("[SERVER] Errore sostituzione atomica active_match.json: " + ex.getMessage());
            }
        }
    }

    // Pulisce il file a fine round (chiamato da GameManager)
    public static synchronized void clearActiveMatch() {
        File file = new File(TARGET_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}