package Proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import Common.Constants;

public class ProxyJob implements Runnable {
    Socket socket;
    Map<String, ProxyJob> jobs;
    Map<String, String> addressBook;
    public ProxyJob(Socket socket, Map<String, ProxyJob> jobs,
                    Map<String, String> addressBook) {
        this.socket = socket;
        this.jobs = jobs;
        this.addressBook = addressBook;
    }

    @Override
    public void run() {
        //zidentyfikuj czy rejestruje się serwer językowy czy klient dzwoni
        try {
            System.out.println("Job started");
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String initialMessage = input.readLine();
            System.out.println(initialMessage);
            if(initialMessage.equals(Constants.TRANSLATION_SERVER_REGISTRATION)){
                this.registerNewTranslator(input);
            }
            else{
                this.passToTranslator(initialMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerNewTranslator(BufferedReader input) throws IOException {
        String languageCode = input.readLine();
        String port = input.readLine();
        this.addressBook.put(languageCode, socket.getRemoteSocketAddress().toString().split(":")[0]+":"+port);
        this.jobs.remove(socket.getRemoteSocketAddress().toString());
    }

    private void passToTranslator(String requestString) throws IOException {
        System.out.println("passing to translator:");
        String[] request = requestString.substring(1, requestString.length()-1).split(", ");
        String wordToTranslate = request[0].substring(1, request[0].length()-1);
        String languageCode = request[1].substring(1, request[1].length()-1);
        String clientListenPort = request[2];

        System.out.println(wordToTranslate);
        System.out.println(languageCode);
        System.out.println(clientListenPort);
        System.out.println(addressBook);
        System.out.println(addressBook.get(languageCode));
        String address = addressBook.get(languageCode).split(":")[0].substring(1);
        int port = Integer.parseInt(addressBook.get(languageCode).split(":")[1]);
        Socket translatorSocket = new Socket(address, port);
        PrintWriter output = new PrintWriter(translatorSocket.getOutputStream(), true);
        output.println(String.format("{%s, %s, %s}",
                                     wordToTranslate,
                                     (((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/", ""),
                                     clientListenPort));
        translatorSocket.close();
    }
}
