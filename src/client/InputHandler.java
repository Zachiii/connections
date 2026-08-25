package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class InputHandler {
    private SocketChannel channel;
    public InputHandler(SocketChannel channel) {
        this.channel = channel;
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        System.out.print("Enter your choice: ");
        label:
        while(!done){
            String operation = scanner.nextLine().trim();
            switch (operation) {
                case "register": {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    RegisterMessage message = new RegisterMessage(operation, username, password);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "updateCredentials": {
                    System.out.println("Se non si vuole aggiornare la password o lo username: mettere null alla richiesta di Password nuova e/o Username nuovo");
                    System.out.print("Username vecchio: ");
                    String oldUsrname = scanner.nextLine().trim();
                    System.out.print("Password vecchio: ");
                    String oldPsw = scanner.nextLine().trim();
                    System.out.print("Username nuovo: ");
                    String newUsername = scanner.nextLine().trim();
                    System.out.print("Password nuova: ");
                    String newPsw = scanner.nextLine().trim();
                    UpdateCredentialsMessage message = new UpdateCredentialsMessage(operation, oldUsrname, newUsername, oldPsw, newPsw);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "login": {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    LoginMessage message = new LoginMessage(operation, username, password);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "logout": {
                    Message message = new Message(operation);
                    SendMessage(message);
                    done = true;
                    new ResponseHandler(this.channel);
                    break;
                }
                case "submitProposal": {
                    ArrayList<String> proposals = new ArrayList<>();
                    Boolean confermed = false;
                    Boolean submit = false;
                    while (!confermed) {
                        System.out.println("Elenca le quattro parole:");
                        System.out.print("Prima parola: ");
                        proposals.add(scanner.nextLine().trim());
                        System.out.print("Seconda parola: ");
                        proposals.add(scanner.nextLine().trim());
                        System.out.print("Terza parola: ");
                        proposals.add(scanner.nextLine().trim());
                        System.out.print("Quarta parola: ");
                        proposals.add(scanner.nextLine().trim());
                        System.out.println("L'elenco è questo " + proposals);
                        System.out.print("Confermi questo elenco? (si/no): ");
                        String input;
                        while (true) {
                            input = scanner.nextLine().trim();
                            if (input.equals("si")) {
                                confermed = true;
                                ProposalMessage message = new ProposalMessage(operation, proposals);
                                SendMessage(message);
                                new ResponseHandler(this.channel);
                                break;
                            } else if (input.equals("no")) {
                                System.out.println("Rifare l'elenco");
                                proposals.clear();
                                break;
                            } else System.out.print("Per confermare o meno l'invio del messaggio scrivere si/no: ");
                        }
                    }
                    break;
                }
                case "requestGameInfo": {
                    System.out.print("Specificare il gameID: ");
                    String gameID = scanner.nextLine().trim();
                    GameMessage message = new GameMessage(operation, gameID);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "requestGameStats": {
                    System.out.print("Specificare il gameID: ");
                    String gameID = scanner.nextLine().trim();
                    GameMessage message = new GameMessage(operation, gameID);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "requestLeaderboard": {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Se vuoi la lista completa dei giocatori scrivi -1, se vuoi il ranking relativo scrivi 0, se vuoi la top K utenti scrivi un numero maggiore di 0: ");
                    String topPlayers = scanner.nextLine().trim();
                    LeaderboardMessage message = new LeaderboardMessage(operation,username, topPlayers);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "requestPlayerStats": {
                    Message message = new Message(operation);
                    SendMessage(message);
                    new ResponseHandler(this.channel);
                    break;
                }
            }
        }
    }

    private void SendMessage(Message message){
        try{
            Gson gson = new GsonBuilder().create();
            String jsonMessage =  gson.toJson(message);
            byte[] jsonMessageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
            int length = jsonMessageBytes.length;
            ByteBuffer byteBuffer = ByteBuffer.allocate(4+length);
            byteBuffer.putInt(length);
            byteBuffer.put(jsonMessageBytes);
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()){
                channel.write(byteBuffer);
            }
            System.out.println("Inviato al server il messaggio: " + jsonMessage);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
