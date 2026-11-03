package common;

public class LeaderboardMessage extends Message {
    String metodoDistampa;
    public LeaderboardMessage(String operation,String metodoDistampa) {
        super(operation);
        this.metodoDistampa = metodoDistampa;
    }
}
