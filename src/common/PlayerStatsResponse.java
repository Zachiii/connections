package common;

public class PlayerStatsResponse extends ResponseMessage {
    public PlayerStatsResponse(String operation, String code, String response) {
        super(operation, code, response);
    }

    // -------------- METODI PER playerStats --------------
    public int getPuzzlesCompleted() {
        return 0;
    }

    public double getWinRate() {
        return 0.0;
    }

    public double getLossRate() {
        return 0.0;
    }

    public int getCurrentStreak() {
        return 0;
    }

    public int getMaxStreak() {
        return 0;
    }

    public int getPerfectPuzzles() {
        return 0;
    }

    public int[] getMistakeHistogram() {
        return null;
    }

    public void printPlayerStats() {
        // TODO: stampa storico dell'utente (completed, win/loss rate, streak, histogram)
    }
}
