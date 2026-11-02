package common;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameInfoResponse  extends ResponseMessage {
    private int won;
    private int gameID;
    private int points;
    private int mistakes;
    private int numberCorrectGuesses;
    private Long timeLeft;
    private ConcurrentHashMap<String,ArrayList<String>> wordsGuessed;
    private ArrayList<String> wordsLeft;
    public GameInfoResponse(String operation,String code, String response) {
        super(operation, code, response);
    }

    // -------------- SETTER --------------
    public void setWon(int won) {this.won = won;}
    public void setGameID(int gameID) {this.gameID = gameID;}
    public void setPoints(int points) {this.points = points;}
    public void setMistakes(int mistakes) {this.mistakes = mistakes;}
    public void setNumberCorrectGuesses(int numberCorrectGuesses) {this.numberCorrectGuesses = numberCorrectGuesses;}
    public void setTimeLeft(Long timeLeft) {this.timeLeft = timeLeft;}
    public void setWordsGuessed(ConcurrentHashMap<String,ArrayList<String>> wordsGuessed) {this.wordsGuessed = new ConcurrentHashMap<>(wordsGuessed);}
    public void setWordsLeft(ArrayList<String> wordsLeft) {this.wordsLeft = new ArrayList<>(wordsLeft);}

    // -------------- METODI PER gameInfo --------------

    public void printGameInfo() {
        System.out.println("Game ID: " + this.gameID);
        if(this.won == 0){
            System.out.printf("Time left: %02d:%02d (mm:ss)%n", this.timeLeft/60, this.timeLeft%60 );
            System.out.println("Words guessed: " + this.wordsGuessed);
            printWordsLeft();
        }
        System.out.println("Points: " + this.points);
        System.out.println("Mistakes made: " + this.mistakes);
        if(this.won == 1){
            System.out.println("Corrects guesses: " + this.numberCorrectGuesses);
            System.out.println("Complimenti, hai vinto la partita");
            return;
        } else if (this.won == -1){
            System.out.println("Corrects guesses: " + this.wordsGuessed.size());
            System.out.println("Vite esaurite: hai perso");
            return;
        }
    }

    public void printWordsLeft(){
        if(!this.wordsLeft.isEmpty()){
            System.out.println("\n--- PAROLE DELLA PARTITA IN CORSO ---\n");
            for (int i = 0; i < this.wordsLeft.size(); i++) {
                System.out.print(this.wordsLeft.get(i) + "\t");
                if ((i + 1) % 4 == 0) {
                    System.out.println();
                }
            }
        }
        System.out.println("\n");
    }
}
