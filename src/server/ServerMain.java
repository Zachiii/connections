package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    static void main(String[] args) {
        ServerPropReader serverPropReader = new ServerPropReader();
        //Coda che contiene tutte le connessioni tra ClientHandler ed il relativo client
        ConcurrentLinkedQueue<ClientHandler> allClients = new ConcurrentLinkedQueue<>();
        //HashMap generale degli utenti che hanno fatto la registrazione
        ConcurrentHashMap<String, User> registeredUsers = UserSerializer.loadUsers();
        //HashMap degli utenti che hanno fatto il login
        ConcurrentHashMap<String,ClientHandler> currentUsers = new ConcurrentHashMap<>();
        //HashMap degli utenti che stanno giocando
        ConcurrentHashMap<String,PlayerStatus> activePlayers = ActiveMatchManager.loadActiveMatch();
        //HashMap di tutte le partite giocate
        ConcurrentHashMap<String,GameStats>  gameStats = GameStatsSerializer.loadGameStats();
        //Mando matchDurationSeconds a un thread che si occuperà di far scorrere il tempo e di avvisare il server quando la partita sarà terminata
        Long matchDurationSeconds = serverPropReader.getMatchTime();
        int gameID = serverPropReader.getGameID();

        //AVVIO GameManager
        GameTimeManager gameTimeManager = new GameTimeManager(currentUsers,activePlayers,gameStats, serverPropReader,matchDurationSeconds,gameID,true);
        Thread timerThread = new Thread(gameTimeManager);
        timerThread.start();

        connectionStart(serverPropReader, allClients, registeredUsers, currentUsers, activePlayers, gameStats, gameTimeManager, timerThread);
    }
    //creazione connesione
    public static void connectionStart(ServerPropReader serverPropReader,
                                       ConcurrentLinkedQueue<ClientHandler> allClients,
                                       ConcurrentHashMap<String, User> registeredUsers,
                                       ConcurrentHashMap<String, ClientHandler> currentUsers,
                                       ConcurrentHashMap<String, PlayerStatus> activePlayers,
                                       ConcurrentHashMap<String,GameStats> gameStats,
                                       GameTimeManager gameTimeManager,
                                       Thread timerThread) {
        int port = serverPropReader.getPort();
        InetAddress inetAddress = serverPropReader.getHost();

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port, 10, inetAddress)) {
            System.out.println("[SERVER] Server in ascolto su questo indirizzo: " + serverSocket.getLocalSocketAddress());

            // AVVIO DEL THREAD DI SPEGNIMENTO DEDICATO
            CloseServer closeServerTask = new CloseServer(serverSocket, pool, gameTimeManager, gameStats, timerThread, allClients, registeredUsers, currentUsers, activePlayers);
            Thread closeThread = new Thread(closeServerTask, "CloseServer-Thread");
            closeThread.start();

            //AVVIO SOCKET ASCOLTO CLIENT
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] Connessione da: " + clientSocket.getRemoteSocketAddress());
                    pool.execute(new ClientHandler(clientSocket, allClients, registeredUsers, currentUsers, activePlayers,gameStats, gameTimeManager));
                } catch (SocketException e) {
                    // La ServerSocket è stata chiusa da CloseServer: uscita normale dal loop
                    System.out.println("[SERVER] ServerSocket chiusa, terminazione ciclo di accept.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Errore in ServerMain: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}
