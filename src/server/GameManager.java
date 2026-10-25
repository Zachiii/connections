package server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements Runnable {
    ConcurrentHashMap<String,ClientHandler> currentUsers;
    ConcurrentHashMap<String,PlayerStatus> activePlayers;
    private final long matchDurationSeconds;
    private volatile long startTimeMillis;
    private int gameID;
    private volatile boolean isRunning;
    private volatile GameSupport currentGame;
    public GameManager(ConcurrentHashMap<String,ClientHandler> currentUsers,ConcurrentHashMap<String,PlayerStatus> activePlayers, Long matchDurationSeconds, int gameID, boolean isRunning) {
        this.currentUsers = currentUsers;
        this.matchDurationSeconds = matchDurationSeconds;
        this.gameID = gameID;
        this.isRunning = isRunning;
        this.activePlayers = activePlayers;
    }

    public GameSupport getCurrentGame() {
        return this.currentGame;
    }
    public int getGameID() {return this.gameID;}
    public void stop(){this.isRunning = false;}

    @Override
    public void run() {
        System.out.println("DEBUG: Timer partito");
        while (isRunning && gameID < 900){
            //inizia partita
            WordReader wordReader = new WordReader();
            this.currentGame = WordReader.getGame(gameID);
            System.out.println("DEBUG: Partita caricata: " + this.currentGame.getGroupWords());
            if(this.currentGame != null){
                for(ClientHandler client: this.currentUsers.values()){
                    client.setShuffledListOfWords(this.currentGame.getShuffledWords());
                    client.setGroupOfWords(this.currentGame.getGroupWords());
                }
            }
            this.startTimeMillis = System.currentTimeMillis();
            try {
                Thread.sleep(matchDurationSeconds * 1000L);
            } catch (InterruptedException e) {
                // Se il thread viene interrotto durante lo spegnimento, esci dal ciclo
                if (!isRunning) {
                    System.out.println("DEBUG: GameManager interrotto per spegnimento.");
                    break;
                }
            }
            if (!isRunning) break;
            //manda segnale UDP fine partita
            System.out.println("DEBUG: Tempo partita: " + gameID + " finito");
            for(ClientHandler clientHandler : currentUsers.values()){
                clientHandler.MatchEnd();
            }
            activePlayers.clear();  //Pulisco la lista di giocatori attivi
            gameID++;
        }
    }
    public long getRemainingTimeSeconds() {
        if (startTimeMillis == 0) return 0;

        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long elapsedSeconds = elapsedMillis / 1000L;
        long remaining = matchDurationSeconds - elapsedSeconds;

        // Math.max evita numeri negativi se il thread si sveglia con qualche ms di ritardo
        return Math.max(0, remaining);
    }
}
