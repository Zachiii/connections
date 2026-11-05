package server;

// Salvata come HashMap in User, serve per avere uno storico dei match dell'utente
public class UserMatchResult {
    private final int gameId;
    private final int score;
    private final int mistakes;
    private final int correctGroups;
    private final int won;

    public UserMatchResult(int gameId, int score, int mistakes, int correctGroups, int won) {
        this.gameId = gameId;
        this.score = score;
        this.mistakes = mistakes;
        this.correctGroups = correctGroups;
        this.won = won;
    }

    public int getGameId() { return gameId; }
    public int getScore() { return score; }
    public int getMistakes() { return mistakes; }
    public int getCorrectGroups() { return correctGroups; }
    public int isWon() { return won; }
}