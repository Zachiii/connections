package common;

import java.util.ArrayList;

public class ResponseMessage extends Message {
    String code;
    String response;
    ArrayList<String> wordsLeft;
    private int gamePoints;
    public ResponseMessage(String operation,String code, String response) {
        super(operation);
        this.code = code;
        this.response = response;
    }
    public void setGamePoints(int gamePoints){
        this.gamePoints = gamePoints;
    }
    public void setWordsLeft(ArrayList<String> wordsLeft){this.wordsLeft = wordsLeft;}
    public int getGamePoints(){
        return this.gamePoints;
    }
    public void printGamResponse(){
        if(gamePoints > 0){
            System.out.println("Congratulazioni ha");
        }
    }
}
