package server;

import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements Runnable {
    ConcurrentHashMap<String,ClientHandler> currentUsers;
    int timeRemaining;
    int gameID;
    boolean isRunning;
    public GameManager(ConcurrentHashMap<String,ClientHandler> currentUsers, int timeRemaining, int gameID, boolean isRunning) {
        this.currentUsers = currentUsers;
        this.timeRemaining = timeRemaining;
        this.gameID = gameID;
        this.isRunning = isRunning;

    }
    @Override
    public void run() {
        System.out.println("DEBUG: Timer partito");
        while (isRunning && gameID < 900){
            //inizia partita
            try {
                new WordReader(currentUsers,gameID);
                Thread.sleep(timeRemaining);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            //manda segnale UDP fine partita
            System.out.println("DEBUG: Tempo partita: " + gameID + " finito");
            for(ClientHandler clientHandler : currentUsers.values()){
                clientHandler.MatchEnd();
            }
            gameID++;
        }
    }
}
