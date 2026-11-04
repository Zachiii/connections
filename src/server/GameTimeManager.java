package server;

import java.util.concurrent.ConcurrentHashMap;

public class GameTimeManager implements Runnable {
    ConcurrentHashMap<String,ClientHandler> currentUsers;
    ConcurrentHashMap<String,PlayerStatus> activePlayers;
    ConcurrentHashMap<String,GameStats> gameStats;
    private final long matchDurationSeconds;
    private volatile long startTimeMillis;
    private int gameID;
    private volatile boolean isRunning;
    private volatile GameWordsRandomizer currentGame;
    ServerPropReader serverPropReader;
    public GameTimeManager(
            ConcurrentHashMap<String,ClientHandler> currentUsers,
            ConcurrentHashMap<String,PlayerStatus> activePlayers,
            ConcurrentHashMap<String,GameStats> gameStats,
            ServerPropReader serverPropReader,
            Long matchDurationSeconds,
            int gameID,
            boolean isRunning) {
        this.currentUsers = currentUsers;
        this.matchDurationSeconds = matchDurationSeconds;
        this.gameID = gameID;
        this.isRunning = isRunning;
        this.activePlayers = activePlayers;
        this.serverPropReader = serverPropReader;
        this.gameStats = gameStats;
    }

    public GameWordsRandomizer getCurrentGame() {
        return this.currentGame;
    }
    public int getGameID() {return this.gameID;}
    public void stop(){this.isRunning = false;}

    @Override
    public void run() {
        System.out.println("[SERVER] Timer partito");
        while (isRunning && gameID < 900){
            //inizia partita
            GameStats currentGame = new GameStats(this);
            this.gameStats.putIfAbsent(String.valueOf(gameID), currentGame);
            WordReader wordReader = new WordReader();
            this.currentGame = WordReader.getGame(gameID);
            System.out.println("[SERVER] Partita caricata: " + this.currentGame.getGroupWords());
            if(this.currentGame != null){
                for(ClientHandler client: this.currentUsers.values()){
                    String username = client.getUsername();

                    PlayerStatus newStatus = new PlayerStatus(username,true);
                    newStatus.setGroupWords(this.currentGame.getGroupWords());
                    newStatus.setWordsLeft(this.currentGame.getShuffledWords());
                    client.playerStatus = newStatus;
                    this.activePlayers.put(username,newStatus);
                    GameStats currentStats = this.gameStats.get(String.valueOf(this.gameID));
                    if (currentStats != null) {
                        currentStats.setNumberOfPlayers();
                    }
                }
                GameStatsSerializer.saveGameStats(this.gameStats);
            }
            this.startTimeMillis = System.currentTimeMillis();
            try {
                Thread.sleep(matchDurationSeconds * 1000L);
            } catch (InterruptedException e) {
                // Se il thread viene interrotto durante lo spegnimento, esci dal ciclo
                if (!isRunning) {
                    System.out.println("[SERVER] GameManager interrotto per spegnimento.");
                    break;
                }
            }
            if (!isRunning) break;
            //manda segnale UDP fine partita
            GameStatsSerializer.saveGameStats(this.gameStats);
            System.out.println("[SERVER] Tempo partita: " + gameID + " finito");
            for(ClientHandler clientHandler : currentUsers.values()){
                clientHandler.MatchEnd(true);
            }
            activePlayers.clear();  //Pulisco la lista di giocatori attivi
            ActiveMatchManager.clearActiveMatch();
            gameID++;
            this.serverPropReader.updateGameID();
        }
    }
    public long getRemainingTimeSeconds() {
        if (startTimeMillis == 0) return 0;

        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long elapsedSeconds = elapsedMillis / 1000L;
        long remaining = matchDurationSeconds - elapsedSeconds;

        // Math.max evita numeri negativi se il thread si sveglia con qualche ms di ritardo
        return Math.max(0, remaining);
    }
}
