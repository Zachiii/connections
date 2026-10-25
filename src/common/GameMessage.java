package common;

import java.util.List;

public class GameMessage extends Message{
    private String gameID;
    public GameMessage(String operation, String gameID){
        super(operation);
        this.gameID = gameID;
    }
}
