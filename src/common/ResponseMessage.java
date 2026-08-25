package common;

public class ResponseMessage extends Message {
    String code;
    String response;
    public ResponseMessage(String operation,String code, String response) {
        super(operation);
        this.code = code;
        this.response = response;
    }
}
