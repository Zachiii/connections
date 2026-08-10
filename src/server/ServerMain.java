package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    static void main(String[] args) {
        ServerProp serverProp = new ServerProp();
        System.out.println(serverProp.getHost());
        connectionStart(serverProp);
    }
    //creazione connesione
    public static void connectionStart(ServerProp serverProp)   {
        int port = serverProp.getPort();
        InetAddress inetAddress = serverProp.getHost();
        try(ServerSocket serverSocket = new ServerSocket(port,5,inetAddress);){
            System.out.println(serverSocket.getLocalSocketAddress());
            //questa versione dell'accettamento connesione non va bene
            while(true) {
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
