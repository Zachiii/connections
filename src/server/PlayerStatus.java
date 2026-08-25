package server;

public class PlayerStatus {
    private String username;
    private Boolean online;
    private int score;
    private int livesLeft;
    private int correctMatches;
    private int matchesLeft;
    public PlayerStatus(String username, Boolean online) {
        this.username = username;
        this.online = online;
        this.score = 0;
        this.livesLeft = 3;
        this.correctMatches = 0;
        this.matchesLeft = 4;
    }
    public void setOnline(Boolean online) {
        this.online = online;
    }
    public boolean perfectMatch() {
        return this.livesLeft == 3;
    }
}
