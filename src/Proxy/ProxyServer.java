package Proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyServer {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Map<String, ProxyJob> jobs = new ConcurrentHashMap<>();
        Map<String, String> addressBook = new ConcurrentHashMap<>();
        try (ServerSocket listeningSocket = new ServerSocket(port)){
            while(true){
                Socket socket = listeningSocket.accept();
                System.out.println("connection detected");
                ProxyJob job = new ProxyJob(socket, jobs, addressBook);
                jobs.put(socket.getRemoteSocketAddress().toString(), job);
                new Thread(job).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
