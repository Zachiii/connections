package common;

public class LeaderboardMessage extends Message {
    String username;
    int topPlayers;
    public LeaderboardMessage(String operation,String username, String topPlayers) {
        super(operation);
        this.username = username;
        this.topPlayers = Integer.parseInt(topPlayers);
    }
}
