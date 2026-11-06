package common;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameEndResponse extends ResponseMessage{
    private boolean gameEnded;
    private int won;
    private int numberOfPlayers;
    private int gamesWon;
    private int gamesLost;
    private int gameID;
    private int playerPoints;
    private int mistakes;
    private double averagePoints;
    private Long timeLeft;
    private ConcurrentHashMap<String, ArrayList<String>> wordsGuessed;
    private ArrayList<String> wordsLeft;

    public GameEndResponse(String operation, String code, String response) {
        super(operation, code, response);
    }

    // -------------- SETTER --------------
    public void setWon (int won){this.won = won;}
    public void setGameEnded(boolean gameEnded) {this.gameEnded = gameEnded;}
    public void setNumberOfPlayers(int numberOfPlayers) {this.numberOfPlayers = numberOfPlayers;}
    public void setGamesWon(int gamesWon) {this.gamesWon = gamesWon;}
    public void setGamesLost(int gamesLost) {this.gamesLost = gamesLost;}
    public void setGameID(int gameID) {this.gameID = gameID;}
    public void setPlayerPoints(int playerPoints) {this.playerPoints = playerPoints;}
    public void setMistakes(int mistakes) {this.mistakes = mistakes;}
    public void setAveragePoints(double averagePoints){this.averagePoints = averagePoints;}
    public void setTimeLeft(long timeLeft){this.timeLeft = timeLeft;}
    public void setWordsGuessed(ConcurrentHashMap<String,ArrayList<String>> wordsGuessed){this.wordsGuessed = wordsGuessed;}
    public void setWordsLeft(ArrayList<String> wordsLeft){this.wordsLeft = wordsLeft;}

    // -------------- Stampa risposta --------------
    public void printEndGameResponse() {
        System.out.println("\n=== RISULTATO PARTITA #" + this.gameID + " ===");

        // 1. Dati individuali del giocatore
        if (this.won == 1) {
            System.out.println("Esito: HAI VINTO! Complimenti!");
        } else if (this.won == -1) {
            System.out.println("Esito: HAI PERSO. Vite esaurite.");
        } else if (this.won == 0) {
            System.out.println("Esito: TEMPO SCADUTO.");
        }

        System.out.println("Punteggio ottenuto: " + this.playerPoints);
        System.out.println("Errori commessi: " + this.mistakes);

        if (this.wordsGuessed != null && !this.wordsGuessed.isEmpty()) {
            System.out.println("Gruppi indovinati (" + this.wordsGuessed.size() + "): " + this.wordsGuessed);
        }

        if (this.wordsLeft != null && !this.wordsLeft.isEmpty()) {
            System.out.print("Parole rimanenti: ");
            printWordsLeft();
        }

        // 2. Statistiche globali
        System.out.println("\n--- STATISTICHE GLOBALI PARTITA ---");
        System.out.println("Giocatori che hanno partecipato: " + this.numberOfPlayers);
        System.out.println("Giocatori che hanno concluso la partita: " + (this.gamesWon + this.gamesLost));
        System.out.println("Giocatori che hanno vinto la partita: " + this.gamesWon);
        System.out.printf("Punteggio medio dei partecipanti: %.2f%n", this.averagePoints);

        // 3. Tempo rimanente se la partita globale è ancora in corso (es. won == 0)
        if (!this.gameEnded && this.timeLeft != null) {
            System.out.printf("Tempo rimanente per la chiusura del round: %02d:%02d (mm:ss)%n", this.timeLeft / 60, this.timeLeft % 60);
        }

        System.out.println("===================================\n");
    }

    private void printWordsLeft() {
        if (this.wordsLeft == null || this.wordsLeft.isEmpty()) return;
        System.out.println();
        for (int i = 0; i < this.wordsLeft.size(); i++) {
            System.out.print(this.wordsLeft.get(i) + "\t");
            if ((i + 1) % 4 == 0) {
                System.out.println();
            }
        }
    }

    public long getTimeLeft(){
        return this.timeLeft;
    }
}
