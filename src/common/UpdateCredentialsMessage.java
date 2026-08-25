package common;

public class UpdateCredentialsMessage extends Message{
    private String oldUsername;
    private String newUsername;
    private String oldPsw;
    private String newPsw;
    public UpdateCredentialsMessage(String message, String oldUsername, String newUsername, String oldPsw, String newPsw){
        super(message);
        this.oldUsername = oldUsername;
        this.newUsername = newUsername;
        this.oldPsw = oldPsw;
        this.newPsw = newPsw;
    }
}
