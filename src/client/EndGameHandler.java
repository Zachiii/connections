package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.GameEndResponse;
import common.ResponseMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

// Gestisce le risposte UDP del server

public class EndGameHandler implements Runnable {
    private final DatagramSocket socket;
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
                Gson gson = new Gson();
                GameEndResponse responseMessage = gson.fromJson(message, GameEndResponse.class);
                responseMessage.printEndGameResponse();
                if(responseMessage.getTimeLeft() != 0) System.out.print("\nLa partita è finita, se non vuoi rimanere loggato per aspettare la prossima scrivi 'logout': ");
                else System.out.print("\n La partita è finita, se vuoi sapere le nuove parole scrivi 'requestGameInfo' se vuoi uscire scrivi 'logout': ");
            }
        } catch (SocketException e) {
            // Terminazione normale: la socket UDP è stata chiusa dal client in fase di logout/uscita
            System.out.println("Ricezione UDP arrestata regolarmente.");
        } catch (IOException e) {
            System.err.println("Errore di I/O nella ricezione UDP: " + e.getMessage());
        }
    }
}
