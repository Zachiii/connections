package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
public class EndGameHandler implements Runnable {
    DatagramSocket socket;
    public EndGameHandler(DatagramSocket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try{
            while (!socket.isClosed()) {
                socket.receive(packet);
                String message = new String(packet.getData(),0,packet.getLength(),StandardCharsets.UTF_8);
                JsonObject messageJson = JsonParser.parseString(message).getAsJsonObject();
                String operation = messageJson.get("operation").getAsString();
                if(operation.equals("EndGame")){
                    System.out.println("Partita finita");
                    //chiedere all'utente se vuole rimanere loggato
                }
            }
        } catch (SocketException e) {
            // Terminazione normale: la socket UDP è stata chiusa dal client in fase di logout/uscita
            System.out.println("DEBUG: Ricezione UDP arrestata regolarmente.");
        } catch (IOException e) {
            System.err.println("Errore di I/O nella ricezione UDP: " + e.getMessage());
        }
    }
}
