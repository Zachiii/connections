package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Message;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPNotificationManager {

    // Invia un messaggio asincrono (come l'EndGame) a un client specifico
    public static void sendNotification(InetAddress clientAddr, int clientUdpPort, Message notificationMessage) {
        if (clientUdpPort <= 0) return;

        Gson gson = new GsonBuilder().create();
        String jsonMessage = gson.toJson(notificationMessage);
        byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet = new DatagramPacket(jsonMessageBytes, jsonMessageBytes.length, clientAddr, clientUdpPort);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Errore invio UDP: " + e.getMessage());
        }
    }
}