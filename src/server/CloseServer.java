package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class CloseServer implements Runnable {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private final GameManager gameManager;
    private final Thread timerThread;
    private final ConcurrentHashMap<String, User> registeredUsers;
    private final ConcurrentHashMap<String, ClientHandler> currentUsers;
    private final ConcurrentHashMap<String, GameStats> gameStats;

    public CloseServer(ServerSocket serverSocket,
                       ExecutorService pool,
                       GameManager gameManager,
                       ConcurrentHashMap<String,GameStats> gameStats,
                       Thread timerThread,
                       ConcurrentHashMap<String, User> registeredUsers,
                       ConcurrentHashMap<String, ClientHandler> currentUsers) {
        this.serverSocket = serverSocket;
        this.pool = pool;
        this.gameManager = gameManager;
        this.timerThread = timerThread;
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
        this.gameStats = gameStats;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("DEBUG: Digita 'exit' o 'stop' per arrestare il server.");

        while (true) {
            String line = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(line) || "stop".equalsIgnoreCase(line)) {
                System.out.println("[SERVER] Procedura di spegnimento avviata...");

                //Ferma il loop di GameManager e sveglialo dal Thread.sleep
                if (gameManager != null) {
                    gameManager.stop();
                }
                if (timerThread != null && timerThread.isAlive()) {
                    timerThread.interrupt();
                }

                //Chiude tutte le connessioni con i client attive
                for (ClientHandler client : currentUsers.values()) {
                    client.closeConnection();
                }
                currentUsers.clear();

                //Persistenza finale su disco
                System.out.println("[SERVER] Salvataggio dati in corso...");
                UserManager.saveUsers(registeredUsers);
                GameStatsManager.saveGameStats(gameStats);

                //Chiude la ServerSocket: questo sblocca immediatamente la accept()
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Errore chiusura ServerSocket: " + e.getMessage());
                }

                //Chiusura del thread pool
                if (pool != null) {
                    pool.shutdown();
                }

                System.out.println("[SERVER] Arresto completato.");
                break;
            }
        }
    }
}