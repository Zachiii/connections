package server;
import java.net.InetAddress;
import java.util.Properties; import java.io.*;
public class ServerProp {
    //leggo configurazione Server di ServerConfig (#porta,indirizzi,valori timeout ecc...)
    private String host;
    private String port;
    private String backlog;
    public String matchTime;
    public int gameID;
    public ServerProp() {
        try (FileReader reader = new FileReader( new File("src/server/ServerConfig.properties"))){
            Properties prop = new Properties();
            prop.load(reader);
            this.host = prop.getProperty("host");
            this.port = prop.getProperty("port");
            this.backlog = prop.getProperty("backlog");
            this.matchTime = prop.getProperty("matchTime");
            this.gameID = Integer.parseInt(prop.getProperty("gameID"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public InetAddress getHost() {
        try{
            return InetAddress.getByName(host);
        }catch (Exception e){
            System.out.println("Host not found");
        }
        return null;
    }
    public void updateGameID(){
        try (FileReader reader = new FileReader( new File("src/server/ServerConfig.properties"))){
            Properties prop = new Properties();
            prop.load(reader);
            int gameID = Integer.parseInt(prop.getProperty("gameID"));
            gameID++;
            prop.setProperty("gameID", Integer.toString(gameID));
            this.gameID = gameID;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
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
