package common;

import java.util.concurrent.atomic.AtomicInteger;

public class GameStatsResponse extends ResponseMessage {
    private int gameID;
    private int numberOfPlayers;
    private int gamesWon;
    private int gamesLost;
    private double averagePoints;
    private long timeLeft;
    private boolean gameEnded;
    public GameStatsResponse(String operation, String code, String response) {
        super(operation, code, response);
    }

    public void setGameID(int gameID) {this.gameID = gameID;}
    public void setNumberOfPlayers(int numberOfPlayers) {this.numberOfPlayers = numberOfPlayers;}
    public void setGamesWon(int gamesWon) {this.gamesWon = gamesWon;}
    public void setGamesLost(int gamesLost) {this.gamesLost = gamesLost;}
    public void setTimeLeft(long timeLeft) {this.timeLeft = timeLeft;}
    public void setGameEnded(boolean gameEnded) {this.gameEnded = gameEnded;}
    public void setAveragePoints(double averagePoints) {this.averagePoints = averagePoints;}

    public void printGameStats() {
        if (!this.gameEnded) {
            System.out.printf("Time left: %02d:%02d (mm:ss)%n", this.timeLeft/60, this.timeLeft%60 );
            System.out.println("Persone che stanno ancora giocando: " + (this.numberOfPlayers - this.gamesWon - this.gamesLost));
        }
        System.out.println("Persone che hanno concluso la partita: " + (this.gamesWon + this.gamesLost));
        System.out.println("Persone che hanno vinto la partita: " + this.gamesWon);
        if(this.gameEnded) {
            System.out.println("Persone che hanno partecipato" + this.numberOfPlayers);
            System.out.println("Punteggio medio: " + this.averagePoints);
        }
    }
}
