package common;

import java.util.ArrayList;

public class ProposalMessage extends Message {
    private ArrayList<String> proposal;
    public ProposalMessage(String operation,ArrayList<String> words) {
        super(operation);
        this.proposal = words;
    }
    public ArrayList<String> getProposal() {
        return this.proposal;
    }
}
