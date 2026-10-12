package common;

public class LoginMessage extends Message {
    private String username;
    private String psw;
    private int udpPort;
    public LoginMessage(String message,String username, String password, int udpPort) {
        super(message);
        this.username = username;
        this.psw = password;
        this.udpPort = udpPort;
    }
}
