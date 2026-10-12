package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;


public class ClientMain {
    static void main(String[] args){
        ClientProp clientProp = new ClientProp();
        InetAddress serverHost = clientProp.getHost();
        int serverPort = clientProp.getServerPort();
        SocketChannel channel = connectToHost(serverHost, serverPort);
        try {
            DatagramSocket socket = new DatagramSocket();
            if(channel == null){
                System.out.println("Il server è spento");
                return;
            };
            //Faccio partire il thread che aspetta la fine della partita
            Thread endGameHandler = new Thread(new EndGameHandler(socket));
            endGameHandler.start();
            new InputHandler(channel,socket);
        } catch (IOException e) {
            e.getMessage();
        }
    }
    //creazione connessione usando NIO
    public static SocketChannel connectToHost(InetAddress serverHost, int serverPort){
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
            while (!clientChannel.finishConnect()){ Thread.sleep(10);}
            System.out.println("Mi sono connesso?: " + clientChannel.isConnected());
            return clientChannel;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
