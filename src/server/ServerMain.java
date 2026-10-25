package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    static void main(String[] args) {
        ServerProp serverProp = new ServerProp();
        //HashMap generale degli utenti che hanno fatto la registrazione
        ConcurrentHashMap<String, User> registeredUsers = UserManager.loadUsers();
        //HashMap degli utenti che hanno fatto il login
        ConcurrentHashMap<String,ClientHandler> currentUsers = new ConcurrentHashMap<>();
        //HashMap degli utenti che stanno giocando
        ConcurrentHashMap<String,PlayerStatus> activePlayers = new ConcurrentHashMap<>();
        //Mando matchDurationSeconds ad un thread che si occuperà di far scorrere il tempo e di avvisare il server quando la partita sarà terminata
        Long matchDurationSeconds = serverProp.getMatchTime();
        int gameID = serverProp.getGameID();

        //AVVIO GameManager
        GameManager gameManager = new GameManager(currentUsers,activePlayers, matchDurationSeconds,gameID,true);
        Thread timerThread = new Thread(gameManager);
        timerThread.start();

        connectionStart(serverProp, registeredUsers, currentUsers, activePlayers, gameManager, timerThread);
    }
    //creazione connesione
    public static void connectionStart(ServerProp serverProp,
                                       ConcurrentHashMap<String, User> registeredUsers,
                                       ConcurrentHashMap<String, ClientHandler> currentUsers,
                                       ConcurrentHashMap<String, PlayerStatus> activePlayers,
                                       GameManager gameManager,
                                       Thread timerThread) {
        int port = serverProp.getPort();
        InetAddress inetAddress = serverProp.getHost();

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port, 10, inetAddress)) {
            System.out.println("DEBUG: Server in ascolto su questo indirizzo: " + serverSocket.getLocalSocketAddress());

            // AVVIO DEL THREAD DI SPEGNIMENTO DEDICATO
            CloseServer closeServerTask = new CloseServer(serverSocket, pool, gameManager, timerThread, registeredUsers);
            Thread closeThread = new Thread(closeServerTask, "CloseServer-Thread");
            closeThread.start();

            //AVVIO SOCKET ASCOLTO CLIENT
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("DEBUG: Connessione da: " + clientSocket.getRemoteSocketAddress());
                    pool.execute(new ClientHandler(clientSocket, registeredUsers, currentUsers, activePlayers, gameManager));
                } catch (SocketException e) {
                    // La ServerSocket è stata chiusa da CloseServer: uscita normale dal loop
                    System.out.println("DEBUG: ServerSocket chiusa, terminazione ciclo di accept.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("DEBUG: Errore in ServerMain: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}
