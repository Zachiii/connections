package common;

public class GameStatsResponse extends ResponseMessage {
    public GameStatsResponse(String operation, String code, String response) {
        super(operation, code, response);
    }
    // -------------- METODI PER gameStats --------------
    public int getPlayersInProgress() {
        return 0;
    }

    public int getPlayersCompleted() {
        return 0;
    }

    public int getPlayersWon() {
        return 0;
    }

    public int getTotalPlayers() {
        return 0;
    }

    public double getAverageScore() {
        return 0.0;
    }

    public void printGameStats() {
        // TODO: stampa statistiche aggregate della partita (in corso o conclusa)
    }
}
