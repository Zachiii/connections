package server;
import java.net.InetAddress;
import java.util.Properties; import java.io.*;
public class ServerPropReader {
    //leggo configurazione Server di ServerConfig (#porta,indirizzi,valori timeout ecc...)
    private String host;
    private String port;
    private String backlog;
    public String matchTime;
    public int gameID;
    public ServerPropReader() {
        try (FileReader reader = new FileReader( new File("src/server/ServerConfig.properties"))){
            Properties prop = new Properties();
            prop.load(reader);
            this.host = prop.getProperty("host");
            this.port = prop.getProperty("port");
            this.backlog = prop.getProperty("backlog");
            this.matchTime = prop.getProperty("matchTime");
            this.gameID = Integer.parseInt(prop.getProperty("gameID"));
        } catch (FileNotFoundException e) {
            System.err.println("[SERVER] File not found");
        } catch (IOException e) {
            System.err.println("[SERVER] Errore di I/O: " + e.getMessage());
        }
    }
    public InetAddress getHost() {
        try{
            return InetAddress.getByName(host);
        }catch (Exception e){
            System.err.println("[SERVER] Host not found");
        }
        return null;
    }
    public void updateGameID() {
        File configFile = new File("src/server/ServerConfig.properties");
        Properties prop = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            prop.load(reader);
        } catch (IOException e) {
            System.err.println("[SERVER] Errore nella lettura del file properties: " + e.getMessage());
            return;
        }
        int gameID = Integer.parseInt(prop.getProperty("gameID", "0"));
        gameID++;
        prop.setProperty("gameID", Integer.toString(gameID));
        this.gameID = gameID;
        try (FileWriter writer = new FileWriter(configFile)) {
            prop.store(writer, null);
        } catch (IOException e) {
            System.err.println("[SERVER] Errore nella scrittura del file properties: " + e.getMessage());
        }
    }
    public int getPort() {
        return Integer.parseInt(port);
    }
    public Long getMatchTime() {
        return Long.parseLong(matchTime);
    }
    public int getGameID () {return gameID;}
}
