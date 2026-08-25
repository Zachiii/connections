package common;

import java.util.ArrayList;

public class ProposalMessage extends Message {
    private ArrayList<String> words;
    public ProposalMessage(String operation,ArrayList<String> words) {
        super(operation);
        this.words = words;
    }
}
