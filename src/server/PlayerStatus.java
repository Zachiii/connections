package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatus {
    //Dati dell'utente riguardanti la partita in corso
    private String username;
    private Boolean online;
    private Boolean gameEnded;
    private int score;
    private int livesLeft;
    private int correctMatches;
    private int groupsLeft;
    private ArrayList<String> wordsLeft;
    private ArrayList<String> listWordsGuessed;
    private ConcurrentHashMap<String, ArrayList<String>> groupWords;
    public PlayerStatus(String username, Boolean online) {
        this.username = username;
        this.online = online;
        this.gameEnded = false;
        this.score = 0;
        this.livesLeft = 4;
        this.correctMatches = 0;
        this.groupsLeft = 4;
        this.wordsLeft = new ArrayList<>();
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
    public boolean isOnline() {return this.online;}
    public boolean isGameEnded() {return this.gameEnded;}

    //Setta la lista di parole da indovinare
    public void setWordsLeft(ArrayList<String> wordsLeft) {
        this.wordsLeft = new ArrayList<>(wordsLeft);
        System.out.println("DEBUG: wordsLeft: " + this.wordsLeft);
    }

    public void setScore() {
        int bonus = 0;
        if (this.correctMatches == 1) bonus = 6;
        else if (this.correctMatches == 2) bonus = 12;
        else if (this.correctMatches >= 3) bonus = 18;

        int erroriFatti = 4 - this.livesLeft;
        int penalita = erroriFatti * 4;

        this.score = bonus - penalita;
    }

    public void setGroupWords(ConcurrentHashMap<String,ArrayList<String>> groupWords) {
        this.groupWords = groupWords;
    }

    public int getScore() {return this.score;}

    public int getLivesLeft() {return this.livesLeft;}

    public ArrayList<String> getListWordsGuessed() {return this.listWordsGuessed;}

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
                this.listWordsGuessed.addAll(wordsGuessed);
                this.correctMatches++;
                this.groupsLeft--;
                this.setScore();
                if(this.groupsLeft == 1) return "WIN";
                return group;
            }
        }
        //risposta valida ma errata
        this.livesLeft--;
        this.setScore();
        if(this.livesLeft == 0) return "ENDGAME";
        return null;
    }
}
