package Client.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientServer implements Runnable {

    public final int port;
    public String receivedTranslation;
    public boolean done = false;
    public ServerSocket listeningSocket;
    public String error;
    Socket dictionarySocket = null;
    BufferedReader dictionaryInput = null;

    public ClientServer(int port) {
        this.port = port;
    }

    public void run(){
        dictionarySocket = null;
        this.error = null;
        try {
            this.waitForConnectionFromDictionary();
            this.readTranslationFromDictionary();
        } catch (Exception e) {
            e.printStackTrace();
            this.error = e.getMessage();
        } finally {
            this.closeDictionarySocket();
            this.closeListeningSocket();
            this.notifyProxyClient();
        }
    }

    private void notifyProxyClient() {
        System.out.println("Trying to notify proxy client");
        synchronized (this) {
            this.done = true;
            this.notify();
            System.out.println("Notified proxy client");
        }
    }

    private void closeListeningSocket() {
        if(listeningSocket != null) {
            System.out.println("Attempting to close receiver server socket");
            try {
                listeningSocket.close();
                System.out.println("Closed receiver server socket");
            } catch (IOException e) {
                e.printStackTrace();
                this.error = e.getMessage();
            }
        }
    }

    private void closeDictionarySocket() {
        if(dictionarySocket != null) {
            System.out.println("Attempting to close receiver socket");
            try {
                dictionarySocket.close();
                System.out.println("Closed  receiver socket");
            } catch (IOException e) {
                e.printStackTrace();
                this.error = e.getMessage();
            }
        }
    }

    private void readTranslationFromDictionary() throws IOException {
        this.dictionaryInput = new BufferedReader(new InputStreamReader(dictionarySocket.getInputStream()));
        this.receivedTranslation = dictionaryInput.readLine();
        System.out.println(receivedTranslation);
    }

    private void waitForConnectionFromDictionary() throws IOException {
        System.out.println("\nStarting client server");
        this.listeningSocket = new ServerSocket(this.port);
        this.dictionarySocket = listeningSocket.accept();
    }
}
