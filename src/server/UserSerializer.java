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

// Serializzazione di registeredUsers

public class UserSerializer {

    private static final String TARGET_PATH = "src/server/users.json";
    private static final String TEMP_PATH   = "src/server/users_temp.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Caricamento all'avvio del server
    public static ConcurrentHashMap<String, User> loadUsers() {
        File file = new File(TARGET_PATH);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
            ConcurrentHashMap<String, User> users = gson.fromJson(reader, type);
            return (users != null) ? users : new ConcurrentHashMap<>();
        } catch (IOException e) {
            System.err.println("[SERVER] Errore durante il caricamento di users.json: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    public static synchronized void saveUsers(ConcurrentHashMap<String, User> users) {
        Path tempFile = Paths.get(TEMP_PATH);
        Path targetFile = Paths.get(TARGET_PATH);

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("[SERVER] Errore durante la scrittura del file temporaneo users temp: " + e.getMessage());
            return;
        }

        // 2. Sostituzione atomica del file originale
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Fallback: se il file system non supporta ATOMIC_MOVE (es. tra partizioni diverse), usa la copia standard
            try {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.err.println("[SERVER] Errore critico durante la sostituzione atomica di users.json: " + ex.getMessage());
            }
        }
    }
}