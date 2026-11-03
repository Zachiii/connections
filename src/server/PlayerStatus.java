package server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatus {
    //Dati dell'utente riguardanti la partita in corso, verrano poi usati per aggiornare i dati in User
    private String username;
    private Boolean online;
    private Boolean gameEnded;
    private int won;
    private int score;
    private int mistakes;
    private int correctMatches;
    private int groupsLeft;
    private ArrayList<String> wordsLeft;
    private ConcurrentHashMap<String, ArrayList<String>> HashOfWordsGuessed;
    private ConcurrentHashMap<String, ArrayList<String>> groupWords;
    public PlayerStatus(String username, Boolean online) {
        this.username = username;
        this.online = online;
        this.gameEnded = false;
        this.won = 0;
        this.score = 0;
        this.mistakes = 0;
        this.correctMatches = 0;
        this.groupsLeft = 4;
        this.wordsLeft = new ArrayList<>();
        this.HashOfWordsGuessed = new ConcurrentHashMap<>();
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
    public boolean isOnline() {return this.online;}
    public boolean isGameEnded() {return this.gameEnded;}
    public int isWon() {return this.won;}

    //Setta la lista di parole da indovinare
    public void setWordsLeft(ArrayList<String> wordsLeft) {
        this.wordsLeft = new ArrayList<>(wordsLeft);
        System.out.println("[SERVER] wordsLeft: " + this.wordsLeft);
    }

    public void setScore() {
        int bonus = 0;
        if (this.correctMatches == 1) bonus = 6;
        else if (this.correctMatches == 2) bonus = 12;
        else if (this.correctMatches >= 3) bonus = 18;

        int penalita = mistakes * 4;

        this.score = bonus - penalita;
    }

    public void setGroupWords(ConcurrentHashMap<String,ArrayList<String>> groupWords) {
        this.groupWords = groupWords;
    }

    public int getScore() {return this.score;}

    public int getMistakes() {return this.mistakes;}

    public ConcurrentHashMap<String,ArrayList<String>> getWordsGuessed() {return this.HashOfWordsGuessed;}

    public ArrayList<String> getWordsLeft() {
        return this.wordsLeft;
    }

    //Controllo delle proposte fatte dall'utente
    public String removeWordsGuessed(ArrayList<String> wordsGuessed) {
        System.out.println("[SERVER] wordsGuessed: " + wordsGuessed);
        if (wordsGuessed == null || wordsGuessed.size() != 4 || !this.wordsLeft.containsAll(wordsGuessed)) {
            return "MALFORMED"; // Proposta non valida: non toglie vite né punti
        }
        for(String group : groupWords.keySet()) {
            if(groupWords.get(group).containsAll(wordsGuessed)) {
                //risposta valida e corretta
                this.wordsLeft.removeAll(wordsGuessed);
                this.HashOfWordsGuessed.put(group,wordsGuessed);
                this.correctMatches++;
                this.groupsLeft--;
                this.setScore();
                if(this.groupsLeft == 1) {
                    this.won = 1;
                    for (String lastTheme : groupWords.keySet()) {
                        if (!this.HashOfWordsGuessed.containsKey(lastTheme)) {
                            // Crea una copia indipendente delle ultime parole prima del clear()
                            this.HashOfWordsGuessed.put(lastTheme, new ArrayList<>(this.wordsLeft));
                            break;
                        }
                    }
                    System.out.println("[SERVER] HashOfWordsGuessed: " + HashOfWordsGuessed);
                    this.groupsLeft--;
                    this.wordsLeft.clear();
                    this.gameEnded = true;
                    return "WIN";
                }
                return group;
            }
        }
        //risposta valida ma errata
        this.mistakes++;
        this.setScore();
        if(this.mistakes == 4) {
            this.won = -1;
            this.gameEnded = true;
            return "ENDGAME";
        }
        return null;
    }
}
