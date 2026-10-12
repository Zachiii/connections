package server;

import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements Runnable {
    ConcurrentHashMap<String,ClientHandler> currentUsers;
    ConcurrentHashMap<String,PlayerStatus> activePlayers;
    int timeRemaining;
    int gameID;
    boolean isRunning;
    private volatile GameSupport currentGame;
    public GameManager(ConcurrentHashMap<String,ClientHandler> currentUsers,ConcurrentHashMap<String,PlayerStatus> activePlayers, int timeRemaining, int gameID, boolean isRunning) {
        this.currentUsers = currentUsers;
        this.timeRemaining = timeRemaining;
        this.gameID = gameID;
        this.isRunning = isRunning;
        this.activePlayers = activePlayers;
    }

    public GameSupport getCurrentGame() {
        return this.currentGame;
    }

    @Override
    public void run() {
        System.out.println("DEBUG: Timer partito");
        while (isRunning && gameID < 900){
            //inizia partita
            WordReader wordReader = new WordReader();
            this.currentGame = WordReader.getGame(gameID);
            if(this.currentGame != null){
                for(ClientHandler client: this.currentUsers.values()){
                    client.setListOfWords(this.currentGame.getShuffledWords());
                }
            }
            try {
                Thread.sleep(timeRemaining);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            //manda segnale UDP fine partita
            System.out.println("DEBUG: Tempo partita: " + gameID + " finito");
            for(ClientHandler clientHandler : currentUsers.values()){
                clientHandler.MatchEnd();
            }
            activePlayers.clear();  //Pulisco la lista di giocatori attivi
            gameID++;
        }
    }
}
