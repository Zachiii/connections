package client;

import common.*;
import server.SendTCPResponse;

import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class InputHandler {
    private SocketChannel channel;
    private DatagramSocket socket;
    public InputHandler(SocketChannel channel, DatagramSocket socket) {
        this.channel = channel;
        this.socket = socket;
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        while(!done){
            System.out.print("Cosa vuoi fare? (Scrivere help per avere una lista dei comandi) ");
            String operation = scanner.nextLine().trim();
            switch (operation) {
                case "register": {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    RegisterMessage message = new RegisterMessage(operation, username, password);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "updateCredentials": {
                    System.out.println("Se non si vuole aggiornare la password o lo username premere semplicemente invio sul campo che non si vuole aggiornare");
                    System.out.print("Username vecchio: ");
                    String oldUsrname = scanner.nextLine().trim();
                    System.out.print("Password vecchio: ");
                    String oldPsw = scanner.nextLine().trim();
                    System.out.print("Username nuovo: ");
                    String newUsername = scanner.nextLine().trim();
                    System.out.print("Password nuova: ");
                    String newPsw = scanner.nextLine().trim();
                    UpdateCredentialsMessage message = new UpdateCredentialsMessage(operation, oldUsrname, newUsername, oldPsw, newPsw);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "login": {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    int udpPort = this.socket.getLocalPort();
                    LoginMessage message = new LoginMessage(operation, username, password,udpPort);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "logout": {
                    Message message = new Message(operation);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    done = true;
                    break;
                }
                case "submitProposal": {
                    ArrayList<String> proposal = new ArrayList<>();
                    Boolean confirmed = false;
                    while (!confirmed) {
                        System.out.println("Elenca le quattro parole:");
                        System.out.print("Prima parola: ");
                        proposal.add(scanner.nextLine().trim().toUpperCase());
                        System.out.print("Seconda parola: ");
                        proposal.add(scanner.nextLine().trim().toUpperCase());
                        System.out.print("Terza parola: ");
                        proposal.add(scanner.nextLine().trim().toUpperCase());
                        System.out.print("Quarta parola: ");
                        proposal.add(scanner.nextLine().trim().toUpperCase());
                        System.out.println("L'elenco è questo " + proposal);
                        System.out.print("Confermi questo elenco? (si/no): ");
                        String input;
                        while (true) {
                            input = scanner.nextLine().trim();
                            if (input.equals("si")) {
                                confirmed = true;
                                ProposalMessage message = new ProposalMessage(operation, proposal);
                                new SendMessage(this.channel,message);
                                new ResponseHandler(this.channel);
                                break;
                            } else if (input.equals("no")) {
                                System.out.println("Rifare l'elenco");
                                proposal.clear();
                                break;
                            } else System.out.print("Per confermare o meno l'invio del messaggio scrivere si/no: ");
                        }
                    }
                    break;
                }
                case "requestGameInfo": {
                    System.out.print("Se vuoi informazioni sulla partita corrente scrivi current sennò scrivi l'ID della partita che ti interessa: ");
                    String gameID = scanner.nextLine().trim();
                    GameMessage message = new GameMessage(operation, gameID);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "requestGameStats": {
                    System.out.print("Specificare il gameID: ");
                    String gameID = scanner.nextLine().trim();
                    GameMessage message = new GameMessage(operation, gameID);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "requestLeaderboard": {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Se vuoi la lista completa dei giocatori scrivi -1, se vuoi il ranking relativo scrivi 0, se vuoi la top K utenti scrivi un numero maggiore di 0: ");
                    String topPlayers = scanner.nextLine().trim();
                    LeaderboardMessage message = new LeaderboardMessage(operation,username, topPlayers);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "requestPlayerStats": {
                    Message message = new Message(operation);
                    new SendMessage(this.channel,message);
                    new ResponseHandler(this.channel);
                    break;
                }
                case "help": {
                    System.out.println("\nLista dei comandi:\n" +
                            " - Scrivere register per registrarsi\n" +
                            " - Scrivere login per fare il login\n" +
                            " - Scrivere logout per fare il logout\n" +
                            " - Scrivere updateCredentials per modificare username o password\n" +
                            " - Scrivere submitProposal per inviare una proposta di gioco (4 parole)\n" +
                            " - Scrivere requestGameInfo per ricevere lo stato e le parole della partita in corso\n" +
                            " - Scrivere requestGameStat per ricevere le statistiche di una partita (in corso o conclusa per ID) (Da implementare)\n" +
                            " - Scrivere requestPlayerStats per consultare le proprie statistiche storiche di gioco (Da implementare)\n" +
                            " - Scrivere requestLeaderboard per ricevere la classifica globale, la top-K o la propria posizione (Da implementare)\n");
                    break;
                }
                default: {
                    System.out.println("Comando non riconosciuto");
                    break;
                }
            }
        }
    }
}
