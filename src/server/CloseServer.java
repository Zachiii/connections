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

    public CloseServer(ServerSocket serverSocket,
                       ExecutorService pool,
                       GameManager gameManager,
                       Thread timerThread,
                       ConcurrentHashMap<String, User> registeredUsers) {
        this.serverSocket = serverSocket;
        this.pool = pool;
        this.gameManager = gameManager;
        this.timerThread = timerThread;
        this.registeredUsers = registeredUsers;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("DEBUG: Digita 'exit' o 'stop' per arrestare il server.");

        while (true) {
            String line = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(line) || "stop".equalsIgnoreCase(line)) {
                System.out.println("[SERVER] Procedura di spegnimento avviata...");

                // 1. Ferma il loop di GameManager e svegliao dal Thread.sleep
                if (gameManager != null) {
                    gameManager.stop();
                }
                if (timerThread != null && timerThread.isAlive()) {
                    timerThread.interrupt();
                }

                // 2. Persistenza finale su disco (users.json ed eventuali altri file)
                System.out.println("[SERVER] Salvataggio dati in corso...");
                UserManager.saveUsers(registeredUsers);
                // Se hai altri file di persistenza:
                // HistoryManager.saveMatches(...);

                // 3. Chiusura del thread pool
                if (pool != null) {
                    pool.shutdown();
                }

                // 4. Chiude la ServerSocket: questo sblocca immediatamente la accept()
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Errore chiusura ServerSocket: " + e.getMessage());
                }

                System.out.println("[SERVER] Arresto completato.");
                break;
            }
        }
    }
}