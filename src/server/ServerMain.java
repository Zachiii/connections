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
        ServerProp serverProp = new ServerProp();
        ConcurrentHashMap<String,ClientHandler> currentUsers = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
        connectionStart(serverProp,registeredUsers,currentUsers);
    }
    //creazione connesione
    public static void connectionStart(ServerProp serverProp,ConcurrentHashMap<String, User> registeredUsers, ConcurrentHashMap<String, ClientHandler> currentUsers)   {
        int port = serverProp.getPort();
        InetAddress inetAddress = serverProp.getHost();
        //creazione socket
        try(ServerSocket serverSocket = new ServerSocket(port,10,inetAddress);){
            System.out.println("Server in ascolto su questo indirizzo: " + serverSocket.getLocalSocketAddress());
            try(ExecutorService pool = Executors.newCachedThreadPool()){
                while(true){
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connessione da: " + clientSocket.getRemoteSocketAddress());
                    pool.execute(new ClientHandler(clientSocket,registeredUsers,currentUsers));
                }
            }
        } catch (IOException e) {
            System.out.println("Errore in ServerMain: " + e.getMessage());
        }
    }
}
