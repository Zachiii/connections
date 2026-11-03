package common;

public class PlayerStatsResponse extends ResponseMessage {
    private int puzzlesCompleted;
    private int gamesWon;
    private int gamesLost;
    private double winRate;
    private double lossRate;
    private int currentStreak;
    private int maxStreak;
    private int perfectPuzzles;
    private int gamesTimedOut;
    private int[] mistakeHistogram;

    public PlayerStatsResponse(String operation, String code, String response) {
        super(operation, code, response);
    }

    // -------------- SETTER --------------
    public void setPuzzlesCompleted(int puzzlesCompleted) {this.puzzlesCompleted = puzzlesCompleted;}
    public void setGamesWon(int gamesWon) {this.gamesWon = gamesWon;}
    public void setGamesLost(int gamesLost) {this.gamesLost = gamesLost;}
    public void setWinRate(double winRate) {this.winRate = winRate;}
    public void setLossRate(double lossRate) {this.lossRate = lossRate;}
    public void setCurrentStreak(int currentStreak) {this.currentStreak = currentStreak;}
    public void setMaxStreak(int maxStreak) {this.maxStreak = maxStreak;}
    public void setPerfectPuzzles(int perfectPuzzles) {this.perfectPuzzles = perfectPuzzles;}
    public void setGamesTimedOut(int gamesTimedOut) {this.gamesTimedOut = gamesTimedOut;}
    public void setMistakeHistogram(int[] mistakeHistogram) {this.mistakeHistogram = mistakeHistogram;}

    // -------------- METODI PER playerStats --------------
    public void printPlayerStats() {
        System.out.println("\n=== STATISTICHE GIOCATORE ===");
        System.out.println("Puzzles completati: " + this.puzzlesCompleted);
        System.out.println("Vinte: " + this.gamesWon + " | Perse (4 errori): " + this.gamesLost + " | Tempo scaduto: " + this.gamesTimedOut);
        System.out.println("Win Rate: " + this.winRate + "% | Loss Rate: " + this.lossRate + "%");
        System.out.println("Streak attuale: " + this.currentStreak + " | Max Streak: " + this.maxStreak);
        System.out.println("Partite perfette (0 errori): " + this.perfectPuzzles);

        if (this.mistakeHistogram != null && this.mistakeHistogram.length == 6) {
            System.out.println("\n--- GUESS DISTRIBUTION ---");
            for (int i = 0; i <= 3; i++) {
                System.out.printf("%d errori  : %-4d %s%n", i, this.mistakeHistogram[i], "#".repeat(this.mistakeHistogram[i]));
            }
            System.out.printf("Perse     : %-4d %s%n", this.mistakeHistogram[4], "#".repeat(this.mistakeHistogram[4]));
            System.out.printf("Timeout   : %-4d %s%n", this.mistakeHistogram[5], "#".repeat(this.mistakeHistogram[5]));
        }
        System.out.println("=============================\n");
    }
}
