package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

// Serve per la gestione dello spegnimento del server: chiude tutti i client attivi, chiude gameTimeManager e chiude la socket del server

public class CloseServer implements Runnable {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private final GameTimeManager gameTimeManager;
    // thread che esegue gameTimeManager
    private final Thread timerThread;
    private ConcurrentLinkedQueue<ClientHandler> allClients;
    private final ConcurrentHashMap<String, User> registeredUsers;
    private final ConcurrentHashMap<String, ClientHandler> currentUsers;
    private final ConcurrentHashMap<String, GameStats> gameStats;
    private final ConcurrentHashMap<String, PlayerStatus> activePlayers;

    public CloseServer(ServerSocket serverSocket,
                       ExecutorService pool,
                       GameTimeManager gameTimeManager,
                       ConcurrentHashMap<String,GameStats> gameStats,
                       Thread timerThread,
                       ConcurrentLinkedQueue<ClientHandler> allClients,
                       ConcurrentHashMap<String, User> registeredUsers,
                       ConcurrentHashMap<String, ClientHandler> currentUsers,
                       ConcurrentHashMap<String,PlayerStatus> activePlayers) {
        this.serverSocket = serverSocket;
        this.pool = pool;
        this.gameTimeManager = gameTimeManager;
        this.timerThread = timerThread;
        this.allClients = allClients;
        this.registeredUsers = registeredUsers;
        this.currentUsers = currentUsers;
        this.gameStats = gameStats;
        this.activePlayers = activePlayers;
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
                if (gameTimeManager != null) {
                    gameTimeManager.stop();
                }
                if (timerThread != null && timerThread.isAlive()) {
                    timerThread.interrupt();
                }

                //Chiude tutte le connessioni con i client
                System.out.println("DEBUG: allClients: " + allClients);
                for (ClientHandler client : allClients) {
                    System.out.println("DEBUG: Chiusura: " + client);
                    client.closeConnection();
                }
                allClients.clear();
                currentUsers.clear();

                //Persistenza finale su disco
                System.out.println("[SERVER] Salvataggio dati in corso...");
                UserSerializer.saveUsers(registeredUsers);
                GameStatsSerializer.saveGameStats(gameStats);
                ActiveMatchManager.saveActiveMatch(activePlayers);

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