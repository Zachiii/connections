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

// Serializzazione di gameStats

public class GameStatsSerializer {
    private static final String TARGET_PATH = "src/server/gamestats.json";
    private static final String TEMP_PATH   = "src/server/gamestats_temp.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Caricamento all'avvio del server
    public static ConcurrentHashMap<String, GameStats> loadGameStats() {
        File file = new File(TARGET_PATH);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<String, GameStats>>() {}.getType();
            ConcurrentHashMap<String, GameStats> stats = gson.fromJson(reader, type);
            return (stats != null) ? stats : new ConcurrentHashMap<>();
        } catch (IOException e) {
            System.err.println("Errore caricamento statistiche partite: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    // Salvataggio atomico e sincronizzato
    public static synchronized void saveGameStats(ConcurrentHashMap<String, GameStats> stats) {
        Path tempFile = Paths.get(TEMP_PATH);
        Path targetFile = Paths.get(TARGET_PATH);

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            gson.toJson(stats, writer);
        } catch (IOException e) {
            System.err.println("Errore scrittura file temporaneo game stats: " + e.getMessage());
            return;
        }

        try {
            Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.err.println("Errore sostituzione atomica gamestats.json: " + ex.getMessage());
            }
        }
    }
}
