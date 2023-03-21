package Client.Model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    final ClientServer clientServer;

    public Client(int port){
        clientServer = new ClientServer(port);
    }

    public String requestTranslation(String word, String language) throws IOException, InterruptedException {
        new Thread(clientServer).start();
        Socket proxySocket = new Socket("localhost", 7777);
        PrintWriter output = new PrintWriter(proxySocket.getOutputStream(), true);
        output.println(String.format("{\"%s\", \"%s\", %s}",
                                     word,
                                     language,
                                     this.clientServer.port));
        proxySocket.close();
        synchronized (clientServer) {
            clientServer.wait();
        }
        return clientServer.receivedTranslation;
    }
}
