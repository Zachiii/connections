package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    static void main(String[] args) {
        boolean isRunning = true;
        ServerProp serverProp = new ServerProp();
        ConcurrentHashMap<String,ClientHandler> currentUsers = new ConcurrentHashMap<>();  //HashMap degli utenti che hanno fatto il login in questo momento
        ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();  //HashMap generale degli utenti che hanno fatto il register
        //Mando timeRemaining ad un thread che si occuperà di far scorrere il tempo e di avvisare il server quando la partita sarà terminata
        int timeRemaining = serverProp.getMatchTime();
        int gameID = serverProp.getGameID();
        Thread timerThread = new Thread(new GameManager(currentUsers,timeRemaining,gameID,isRunning));  //Faccio partire il timer
        timerThread.start();
        connectionStart(serverProp,registeredUsers,currentUsers,isRunning);  //DEBUG: Meglio metterlo su un thread a parte?
    }
    //creazione connesione
    public static void connectionStart(ServerProp serverProp,ConcurrentHashMap<String, User> registeredUsers, ConcurrentHashMap<String, ClientHandler> currentUsers, boolean isRunning)   {
        int port = serverProp.getPort();
        InetAddress inetAddress = serverProp.getHost();
        //creazione socket
        try(ServerSocket serverSocket = new ServerSocket(port,10,inetAddress);){
            System.out.println("DEBUG: Server in ascolto su questo indirizzo: " + serverSocket.getLocalSocketAddress());
            try(ExecutorService pool = Executors.newCachedThreadPool()){
                while(isRunning){
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("DEBUG: Connessione da: " + clientSocket.getRemoteSocketAddress());
                    pool.execute(new ClientHandler(clientSocket,registeredUsers,currentUsers));
                }
            }
        } catch (IOException e) {
            System.out.println("DEBUG: Errore in ServerMain: " + e.getMessage());
        }
    }
}
