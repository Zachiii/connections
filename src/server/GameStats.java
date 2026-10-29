package server;

import java.util.concurrent.atomic.AtomicInteger;

public class GameStats {
    //Statistiche per singola partita
    private GameManager gameManager;
    private final int gameID;
    private final AtomicInteger numberOfPlayers = new AtomicInteger(0);
    private final AtomicInteger totalPoints = new AtomicInteger(0);
    private final AtomicInteger gamesWon = new AtomicInteger(0);
    private final AtomicInteger gamesLost = new AtomicInteger(0);

    public GameStats(GameManager gameManager) {
        this.gameManager = gameManager;
        this.gameID = this.gameManager.getGameID();
    }

    public void setNumberOfPlayers() {this.numberOfPlayers.incrementAndGet();}
    public void setTotalPoints(int totalPoints) {this.totalPoints.addAndGet(totalPoints);}
    public void setGamesWon() {this.gamesWon.incrementAndGet();}
    public void setGamesLost() {this.gamesLost.incrementAndGet();}

    public int getNumberOfPlayers() { return this.numberOfPlayers.get(); }
    public int getTotalPoints() { return this.totalPoints.get(); }
    public int getGamesWon() { return this.gamesWon.get(); }
    public int getGamesLost() { return this.gamesLost.get(); }
    public int getGameID() { return this.gameID; }
}
