package server;

public class User {
    //Dati dell'utente generali
    private String username;
    private String password;
    private int puzzlesCompleted;
    private int winRate;
    private int lossRate;
    private int curretnStreak;
    private int maxStreak;
    private int perfectPuzzles;
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.puzzlesCompleted = 0;
        this.winRate = 0;
        this.lossRate = 0;
        this.curretnStreak = 0;
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
}
