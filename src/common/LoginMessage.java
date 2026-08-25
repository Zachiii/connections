package common;

public class LoginMessage extends Message {
    private String username;
    private String psw;
    public LoginMessage(String message,String username, String password) {
        super(message);
        this.username = username;
        this.psw = password;
    }
}
