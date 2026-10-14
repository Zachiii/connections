package server;

public class User {
    //Dati dell'utente generali
    private String username;
    private String password;
    private int gamesWon;
    private int gamesLost;
    private int points;
    private int puzzlesCompleted;
    private int winRate;
    private int lossRate;
    private int currentStreak;
    private int maxStreak;
    private int perfectPuzzles;
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
    }
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
        this.winRate = this.puzzlesCompleted / this.gamesWon;
        this.lossRate = this.winRate / this.gamesLost;
    }
    public void addStreak(){
        this.currentStreak++;
        this.gamesWon++;
        if(this.currentStreak > this.maxStreak) this.maxStreak = this.currentStreak;
    }
    public void resetStreak(){
        this.currentStreak = 0;
        this.gamesWon++;
    }
    public int getPoints() {return this.points;}
}
