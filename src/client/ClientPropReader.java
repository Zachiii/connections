package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class ClientPropReader {
    //leggo configurazione Client di ClientConfig
    private String host;
    private String serverPort;
    private String clientPort;
    public String matchTime;
    public ClientPropReader() {
        File configFile = new File("ClientConfig.properties");
        // Se non lo trova qui (es. quando lanci da dentro l'IDE), cerca in src/client/
        if (!configFile.exists()) {
            configFile = new File("src/client/ClientConfig.properties");
        }

        try (FileReader reader = new FileReader(configFile)) {
            Properties prop = new Properties();
            prop.load(reader);
            this.host = prop.getProperty("host");
            this.serverPort = prop.getProperty("serverPort");
            this.clientPort = prop.getProperty("clientPort");
            this.matchTime = prop.getProperty("matchTime");
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
    public int getServerPort() {
        return Integer.parseInt(serverPort);
    }
    public int getClientPort() {return Integer.parseInt(clientPort);}
    public int getMatchTime() {
        return Integer.parseInt(matchTime);
    }
}

