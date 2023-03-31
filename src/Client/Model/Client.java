package Client.Model;

import Common.CustomException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public final ClientServer clientServer;
    final int proxyPort;
    public Thread serverThread;
    Socket proxySocket;
    PrintWriter proxyOutput;
    BufferedReader proxyInput;
    String reply = "";

    public Client(int port, int proxyPort){
        this.clientServer = new ClientServer(port);
        this.proxyPort = proxyPort;
    }

    public String requestTranslation(String word, String language)
            throws IOException, InterruptedException, CustomException {
        this.startReceiverThread();

        this.openProxyConnection();
        this.sendQueryToProxy(word, language);
        this.readReplyFromProxy();
        this.closeProxyConnection();

        this.waitForResponseFromDictionary();
        this.checkForErrorInReceiverThread();

        return clientServer.receivedTranslation;
    }

    private void startReceiverThread() {
        if(this.serverThread == null || !this.serverThread.isAlive()) {
            this.serverThread = new Thread(clientServer);
            this.serverThread.start();
        }
    }

    private void checkForErrorInReceiverThread() throws CustomException {
        if(clientServer.error != null) {
            System.out.println("Error returned from server thread\n");
            throw new CustomException(clientServer.error);
        }
    }

    void openProxyConnection() throws IOException {
        proxySocket = new Socket("localhost", this.proxyPort);
        proxyOutput = new PrintWriter(proxySocket.getOutputStream(), true);
        proxyInput = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()));
    }

    void closeProxyConnection() throws IOException {
        proxySocket.close();
    }

    void waitForResponseFromDictionary() throws InterruptedException {
        System.out.println("Waiting for notification from receiver server");
        synchronized (clientServer) {
            while (!clientServer.done) {
                clientServer.wait();
            }
            System.out.println("Got notified by receiver server");
        }
    }

    void sendQueryToProxy(String word, String language){
        proxyOutput.println(String.format("{\"%s\", \"%s\", %s}",
                                          word,
                                          language,
                                          this.clientServer.port));
    }

    void readReplyFromProxy() throws IOException, CustomException {
        reply = proxyInput.readLine();
        System.out.println("Reply: " + reply);
        if (!reply.equals("OK")) {
            throw new Common.CustomException(reply);
        }
    }
}
