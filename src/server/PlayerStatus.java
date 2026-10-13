package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatus {
    //Dati dell'utente riguardanti la partita in corso
    private String username;
    private Boolean online;
    private int score;
    private int livesLeft;
    private int correctMatches;
    private int groupsLeft;
    private ArrayList<String> wordsLeft;
    private ConcurrentHashMap<String, ArrayList<String>> groupWords;
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
        this.wordsLeft = new ArrayList<>(wordsLeft);
        System.out.println("DEBUG: wordsLeft: " + this.wordsLeft);
    }
    public void setScore(int score) {
        if (this.correctMatches == 0 && this.livesLeft == 3) this.score = 0;
        if (this.correctMatches == 1) this.score += 6;
        if (this.correctMatches == 2) this.score += 12;
        if (this.correctMatches == 3) this.score += 18;
    }
    public void setGroupWords(ConcurrentHashMap<String,ArrayList<String>> groupWords) {
        this.groupWords = groupWords;
    }
    public int getScore() {return this.score;}
    public ArrayList<String> getWordsLeft() {
        return this.wordsLeft;
    }

    //Controllo delle proposte fatte dall'utente
    public String removeWordsGuessed(List<String> wordsGuessed) {
        System.out.println("DEBUG: wordsGuessed: " + wordsGuessed);
        if (wordsGuessed == null || wordsGuessed.size() != 4 || !this.wordsLeft.containsAll(wordsGuessed)) {
            return "MALFORMED"; // Proposta non valida: non toglie vite né punti
        }
        for(String group : groupWords.keySet()) {
            if(groupWords.get(group).containsAll(wordsGuessed)) {
                //risposta valida e corretta
                this.wordsLeft.removeAll(wordsGuessed);
                this.correctMatches++;
                return group;
            }
        }
        //risposta valida ma errata
        this.livesLeft--;
        this.score -= 4;
        return null;
    }
}
