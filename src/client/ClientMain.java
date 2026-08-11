package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.RegisterMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientMain {
    static void main(String[] args){
        ClientProp clientProp = new ClientProp();
        InetAddress serverHost = clientProp.getHost();
        int serverPort = clientProp.getPort();
        SocketChannel channel = connectToHost(serverHost, serverPort);
        if(channel == null){
            System.out.println("Il server è spento");
            return;
        };
        InputHandler inputHandler = new InputHandler(channel);
        //temporaneo
    }
    //creazione connessione usando NIO
    public static SocketChannel connectToHost(InetAddress serverHost, int serverPort){
        try (SocketChannel clientChannel = SocketChannel.open();){
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
