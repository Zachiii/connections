package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameWordsRandomizer {
    // Crea la lista di parole randomizzate per ogni giocatore
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

    public ConcurrentHashMap<String, ArrayList<String>> getGroupWords() {
        ConcurrentHashMap<String, ArrayList<String>> groupWords = new ConcurrentHashMap<>();
        for (GroupSupport group : groups) {
            groupWords.put(group.getTheme(), (ArrayList<String>) group.getWords());
        }
        return groupWords;
    }
    @Override
    public String toString() {
        return "Game{gameId=" + gameId + ", groups=" + groups + "}";
    }
}
