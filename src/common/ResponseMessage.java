package common;

import java.util.ArrayList;

public class ResponseMessage extends Message {
    String code;
    String response;
    public ResponseMessage(String operation,String code, String response) {
        super(operation);
        this.code = code;
        this.response = response;
    }

    // -------------- METODI per riconoscere il tipo di risposta --------------
    public String getResponse(){return this.response;}
    public String getCode() {return this.code;}

    // -------------- METODO per il login --------------
}
