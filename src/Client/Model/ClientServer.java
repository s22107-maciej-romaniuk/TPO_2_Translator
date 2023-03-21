package Client.Model;

import Common.Constants;
import Proxy.ProxyJob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientServer implements Runnable {

    public final int port;
    public String receivedTranslation;

    public ClientServer(int port){
        this.port = port;
    }

    public void run(){
        try (ServerSocket listeningSocket = new ServerSocket(this.port)){
            Socket socket = listeningSocket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            receivedTranslation = input.readLine();
            System.out.println(receivedTranslation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (this) {
            this.notify();
        }
    }
}
