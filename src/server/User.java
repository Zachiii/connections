package server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    //Contiene tutti i dati relativi all'utente, viene poi serializzato all'interno di users.json
    private String username;
    private String password;
    private int gamesWon;
    private int gamesLost;
    private int points;
    private int puzzlesCompleted;       //per definizione qualsiasi puzzle giocato
    private int winRate;
    private int lossRate;
    private int currentStreak;
    private int maxStreak;
    private int perfectPuzzles;
    //Storico delle partite: chiave = gameID
    private final ConcurrentHashMap<Integer,UserMatchResult> playerHistory;
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.puzzlesCompleted = 0;
        this.winRate = 0;
        this.lossRate = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.perfectPuzzles = 0;
        this.playerHistory = new ConcurrentHashMap<>();
    }

    // --------- METODI PER LA GESTIONE DELLA CRONOLOGIA DELLE PARTITE ---------
    public  void addMatchResult(UserMatchResult result) {
        if (result != null) {
            playerHistory.put(result.getGameId(), result);
        }
    }
    public UserMatchResult getMatchResult(int gameId) {
        return playerHistory.get(gameId);
    }
    public ConcurrentHashMap<Integer, UserMatchResult> getMatchHistory() {
        return playerHistory;
    }

    // --------- METODI GENERALI PER LA GESTIONE DELL'UTENTE ---------
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public void updateCredentials(String username,String password) {
        if(!username.isEmpty())this.username = username;
        if(!password.isEmpty())this.password = password;
    }
    public void setPoints(int points) {
        this.puzzlesCompleted++;
        this.points += points;
        if(points == 18) perfectPuzzles++;
        if(this.gamesWon != 0)this.winRate = this.puzzlesCompleted / this.gamesWon;
        if(this.gamesLost != 0)this.lossRate = this.winRate / this.gamesLost;
    }
    public void addStreak(){
        this.currentStreak++;
        this.gamesWon++;
        if(this.currentStreak > this.maxStreak) this.maxStreak = this.currentStreak;
    }
    public void resetStreak(){
        this.currentStreak = 0;
        this.gamesLost++;
    }
    public int getPoints() {return this.points;}
}
