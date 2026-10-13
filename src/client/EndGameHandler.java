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
            socket.receive(packet);
            String message = new String(packet.getData(),0,packet.getLength(),StandardCharsets.UTF_8);
            JsonObject messageJson = JsonParser.parseString(message).getAsJsonObject();
            String operation = messageJson.get("operation").getAsString();
            if(operation.equals("EndGame")){
                System.out.println("Partita finita");
                //serializzare punteggio
            }
        } catch (SocketException e) {
            System.out.println("Chiusura Client");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
