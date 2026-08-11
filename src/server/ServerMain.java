package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    static void main(String[] args) {
        ServerProp serverProp = new ServerProp();
        ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
        connectionStart(serverProp,registeredUsers);
    }
    //creazione connesione
    public static void connectionStart(ServerProp serverProp,ConcurrentHashMap<String, User> registeredUsers)   {
        int port = serverProp.getPort();
        InetAddress inetAddress = serverProp.getHost();
        //creazione socket
        try(ServerSocketChannel serverChannel = ServerSocketChannel.open();){
            //usiamo il selector per evitare di fare polling
            Selector selector = Selector.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(inetAddress,port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server in ascolto su questo indirizzo: " + serverChannel.socket().getLocalSocketAddress());
            //inizio ascolto
            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(!selectionKey.isValid()){
                        iterator.remove();
                    }
                    if(selectionKey.isAcceptable()){
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        if(clientChannel != null){
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println("Connessione accettata da: " + clientChannel.socket().getRemoteSocketAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
