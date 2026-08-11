package common;

import server.User;

import java.util.concurrent.ConcurrentHashMap;

public class RegisterMessage extends Message {
    private String username;
    private String psw;
    public RegisterMessage(String message,String username, String psw) {
        super(message);
        this.username = username;
        this.psw = psw;
    }
}
