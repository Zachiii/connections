package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;


public class ClientMain {
    static void main(String[] args){
        ClientPropReader clientPropReader = new ClientPropReader();
        InetAddress serverHost = clientPropReader.getHost();
        int serverPort = clientPropReader.getServerPort();
        SocketChannel channel = connectToHost(serverHost, serverPort);
        try (DatagramSocket socket = new DatagramSocket();){
            if(channel == null){
                System.out.println("Il server è spento");
                return;
            };
            printWelcomeBanner();
            //Faccio partire il thread che aspetta la fine della partita
            Thread endGameHandler = new Thread(new EndGameHandler(socket));
            endGameHandler.start();
            new InputHandler(channel,socket);
        } catch (IOException e) {
            System.err.println("Errore apertura socket UDP client: " + e.getMessage());
        } finally {
            try {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
            } catch (IOException ignored) {}
            System.out.println("Client terminato con successo.");
        }
    }

    //creazione connessione usando NIO
    public static SocketChannel connectToHost(InetAddress serverHost, int serverPort){
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
            return clientChannel;
        } catch (IOException e) {
            return null;
        }
    }

    //Banner di inizio gioco
    public static void printWelcomeBanner() {
        String banner = """
       \s
         ██████╗ ██████╗ ███╗   ██╗███╗   ██╗███████╗ ██████╗████████╗██╗ ██████╗ ███╗   ██╗███████╗
        ██╔════╝██╔═══██╗████╗  ██║████╗  ██║██╔════╝██╔════╝╚══██╔══╝██║██╔═══██╗████╗  ██║██╔════╝
        ██║     ██║   ██║██╔██╗ ██║██╔██╗ ██║█████╗  ██║        ██║   ██║██║   ██║██╔██╗ ██║███████╗
        ██║     ██║   ██║██║╚██╗██║██║╚██╗██║██╔══╝  ██║        ██║   ██║██║   ██║██║╚██╗██║╚════██║
        ╚██████╗╚██████╔╝██║ ╚████║██║ ╚████║███████╗╚██████╗   ██║   ██║╚██████╔╝██║ ╚████║███████║
         ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═══╝╚══════╝ ╚═════╝   ╚═╝   ╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝

        =============================================================================================
                                       BENVENUTI IN CONNECTIONS!
        =============================================================================================
      \s""";
        System.out.println(banner);
    }
}
