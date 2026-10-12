package common;

import java.util.List;

public class GameMessage extends Message{
    private int gameID;
    public GameMessage(String operation, String gameID){
        super(operation);
        this.gameID = Integer.parseInt(gameID);
    }
}
