package server;

public class LeaderBoardEntry {
    private final int rank;
    private final int points;
    private final String username;

    public LeaderBoardEntry(int rank, int points, String username){
        this.rank = rank;
        this.points = points;
        this.username = username;
    }

    // -------------- GETTER --------------
    public int getRank() { return rank; }
    public int getPoints() { return points; }
    public String getUsername() { return username; }
}
