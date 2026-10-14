package common;

import java.util.ArrayList;

public class ResponseMessage extends Message {
    String code;
    String response;
    ArrayList<String> wordsLeft;
    ArrayList<String> listWordsGuessed;
    private int mistakes;
    private int gamePoints;
    public ResponseMessage(String operation,String code, String response) {
        super(operation);
        this.code = code;
        this.response = response;
        this.wordsLeft = new ArrayList<>();
        this.listWordsGuessed = new ArrayList<>();
    }
    public void setGamePoints(int gamePoints){
        this.gamePoints = gamePoints;
    }
    public void setWordsLeft(ArrayList<String> wordsLeft){this.wordsLeft = wordsLeft;}
    public void setListWordsGuessed(ArrayList<String> listWordsGuessed){this.listWordsGuessed = listWordsGuessed;}
    public void setMistakes(int mistakes){this.mistakes = mistakes;}
    public int getGamePoints(){
        return this.gamePoints;
    }
    public int getMistakes(){ return this.mistakes;}
    public void printGamResponse(){
        if(gamePoints > 0){
            System.out.println("Congratulazioni ha");
        }
    }
}
