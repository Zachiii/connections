package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameSupport {
    private int gameId;
    private List<GroupSupport> groups;

    public int getGameId() {
        return gameId;
    }

    public List<GroupSupport> getGroups() {
        return groups;
    }

    public ArrayList<String> getShuffledWords() {
        ArrayList<String> allWords = new ArrayList<>();
        for (GroupSupport group : groups) {
            allWords.addAll(group.getWords());
        }
        Collections.shuffle(allWords); // Il testo specifica che le parole vanno inviate in ordine casuale
        return allWords;
    }

    @Override
    public String toString() {
        return "Game{gameId=" + gameId + ", groups=" + groups + "}";
    }
}
