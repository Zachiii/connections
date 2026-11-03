package common;

import server.LeaderBoardEntry;

import java.util.List;

public class LeaderboardResponse extends ResponseMessage {
    private List<LeaderBoardEntry> leaderboard;
    private int userRank;

    public LeaderboardResponse(String operation, String code, String response) {
        super(operation, code, response);
    }

    // -------------- SETTER --------------
    public void setLeaderboard(List<LeaderBoardEntry> leaderboard) { this.leaderboard = leaderboard; }
    public void setUserRank(int userRank) { this.userRank = userRank; }

    public void printLeaderBoard() {
        System.out.println("\n=== RISULTATI CLASSIFICA ===");
        if (this.leaderboard != null && !this.leaderboard.isEmpty()) {
            for (LeaderBoardEntry entry : this.leaderboard) {
                System.out.printf("%d. %s - Punti: %d%n", entry.getRank(), entry.getUsername(), entry.getPoints());
            }
        } else {
            System.out.println("Nessun giocatore corrispondente trovato.");
        }
        System.out.println("--------------------------");
        if (this.userRank > 0) {
            System.out.println("La tua posizione attuale: " + this.userRank);
        } else {
            System.out.println("Non sei ancora presente in classifica.");
        }
        System.out.println("==========================\n");
    }
}