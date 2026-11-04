package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

// Gestisce le risposte TCP mandate dal server

public class ResponseHandler {
    private final boolean success;

    public ResponseHandler(SocketChannel channel) {
        this.success = handleResponse(channel);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean handleResponse(SocketChannel channel){
        ByteBuffer byteLength = ByteBuffer.allocate(4);
        try {
            while (byteLength.hasRemaining()){
                if(channel.read(byteLength) == -1){
                    System.out.println("\n[AVVISO] Connessione chiusa: il server è stato spento o non risponde.");
                    return false;
                }
            }
            byteLength.flip();
            int length = byteLength.getInt();
            ByteBuffer byteMessage = ByteBuffer.allocate(length);
            while (byteMessage.hasRemaining()){
                if(channel.read(byteMessage) == -1){
                    System.out.println("\n[AVVISO] Connessione interrotta durante la ricezione del payload.");
                    return false;
                }
            }
            String messageString = new String(byteMessage.array(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            ResponseMessage responseMessage = gson.fromJson(messageString, ResponseMessage.class);
            System.out.println("Risposta server: [" + responseMessage.getCode() + "] " + responseMessage.getResponse());
            switch(responseMessage.getOperation()){
                case "loginResponse", "requestGameInfoResponse": {
                    GameInfoResponse response = gson.fromJson(messageString, GameInfoResponse.class);
                    if(responseMessage.getCode().equals("200"))response.printGameInfo();
                    break;
                }
                case "requestGameStatsResponse": {
                    GameStatsResponse response = gson.fromJson(messageString, GameStatsResponse.class);
                    if(responseMessage.getCode().equals("200"))response.printGameStats();
                    break;
                }
                case "requestPlayerStatsResponse": {
                    PlayerStatsResponse response = gson.fromJson(messageString,PlayerStatsResponse.class);
                    if(responseMessage.getCode().equals("200")) response.printPlayerStats();
                    break;
                }
                case "requestLeaderboardResponse":{
                    LeaderboardResponse response = gson.fromJson(messageString,LeaderboardResponse.class);
                    if(responseMessage.getCode().equals("200")) response.printLeaderBoard();
                    break;
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("\n[ERRORE] Comunicazione TCP con il server interrotta.");
            return false;
        }
    }
}
