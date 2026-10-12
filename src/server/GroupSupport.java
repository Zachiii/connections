package server;

import java.util.List;
//Classe di supporto che mi serve per leggere i groups in Connections_Data.json
public class GroupSupport {
    private String theme;
    private List<String> words;

    public String getTheme() {
        return theme;
    }

    public List<String> getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "Group{theme='" + theme + "', words=" + words + "}";
    }
}