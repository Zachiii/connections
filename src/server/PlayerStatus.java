package server;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatus {
    //Dati dell'utente riguardanti la partita in corso
    private String username;
    private Boolean online;
    private int score;
    private int livesLeft;
    private int correctMatches;
    private int groupsLeft;
    private ArrayList<String> wordsLeft;
    public PlayerStatus(String username, Boolean online) {
        this.username = username;
        this.online = online;
        this.score = 0;
        this.livesLeft = 3;
        this.correctMatches = 0;
        this.groupsLeft = 4;
    }
    public void setOnline(Boolean online) {
        this.online = online;
    }
    public boolean isOnline() {return this.online;}
    public void setWordsLeft(ArrayList<String> wordsLeft) {
        this.wordsLeft.addAll(wordsLeft);
        System.out.println("DEBUG: wordsLeft: " + this.wordsLeft);
    }
    public void setScore(int score) {
        if (this.correctMatches == 0 && this.livesLeft == 3) this.score = 0;
        if (this.correctMatches == 1) this.score += 6;
        if (this.correctMatches == 2) this.score += 12;
        if (this.correctMatches == 3) this.score += 18;
    }
    public int getScore() {return this.score;}
    public ArrayList<String> getWordsLeft() {
        return this.wordsLeft;
    }
    public boolean removeWordsGuessed(List<String> wordsGuessed) {
        for (String word : wordsGuessed) {
            if (wordsLeft.contains(word)) {
                this.wordsLeft.remove(word);
            } else {
                this.livesLeft -= 1;
                this.score -= 4;
                return false;
            }
        }
        this.groupsLeft -= 1;
        this.correctMatches += 1;
        return true;
    }
}
