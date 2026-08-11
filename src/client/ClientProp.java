package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class ClientProp {
    //leggo configurazione Client di ClientConfig (#porta,indirizzi,valori timeout ecc...)
    private String host;
    private String port;
    public String matchTime;
    public ClientProp() {
        try (FileReader reader = new FileReader( new File("src/client/ClientConfig.properties"))){
            Properties prop = new Properties();
            prop.load(reader);
            this.host = prop.getProperty("host");
            this.port = prop.getProperty("port");
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
    public int getPort() {
        return Integer.parseInt(port);
    }
    public int getMatchTime() {
        return Integer.parseInt(matchTime);
    }
}

