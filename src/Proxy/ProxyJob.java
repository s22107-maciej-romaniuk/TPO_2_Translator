package Proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import Common.Constants;
import Common.CustomException;

public class ProxyJob implements Runnable {
    Socket clientSocket;
    Map<String, ProxyJob> jobs;
    Map<String, String> addressBook;
    public ProxyJob(Socket clientSocket, Map<String, ProxyJob> jobs,
                    Map<String, String> addressBook) {
        this.clientSocket = clientSocket;
        this.jobs = jobs;
        this.addressBook = addressBook;
    }
    PrintWriter outputClient;
    BufferedReader inputClient;
    String initialMessage;

    @Override
    public void run() {
        //zidentyfikuj czy rejestruje się serwer językowy czy klient dzwoni
        try {
            getStreamsFromClientSocket();
            try {
                System.out.println("Job started");

                readMessageFromClient();
                actBasedOnMessage();
            } catch (IOException e) {
                e.printStackTrace();
                sendCallbackMessageToClient("SERVER ERROR");
            } catch (Common.CustomException e) {
                e.printStackTrace();
                sendCallbackMessageToClient(e.getMessage());
            }
            finally{
                this.jobs.remove(this.clientSocket.getRemoteSocketAddress().toString());
                this.clientSocket.close();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void sendCallbackMessageToClient(String s) {
        outputClient.println(s);
    }

    private void actBasedOnMessage() throws IOException, CustomException {
        if (initialMessage.equals(Constants.TRANSLATION_SERVER_REGISTRATION)) {
            this.registerNewTranslator(inputClient);
        } else {
            this.passToTranslator(initialMessage);
            sendCallbackMessageToClient("OK");
        }
    }

    private void readMessageFromClient() throws IOException {
        initialMessage = inputClient.readLine();
        System.out.println(initialMessage);
    }

    private void getStreamsFromClientSocket() throws IOException {
        outputClient = new PrintWriter(clientSocket.getOutputStream(), true);
        inputClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    private void registerNewTranslator(BufferedReader input) throws IOException {
        String languageCode = input.readLine();
        String port = input.readLine();
        this.addressBook.put(languageCode, clientSocket.getRemoteSocketAddress().toString().split(":")[0] + ":" + port);
        this.jobs.remove(clientSocket.getRemoteSocketAddress().toString());
    }

    private void passToTranslator(String requestString) throws IOException, CustomException {
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

        if(!addressBook.containsKey(languageCode)) throw new CustomException("Unknown language key");

        String address = addressBook.get(languageCode).split(":")[0].substring(1);
        int port = Integer.parseInt(addressBook.get(languageCode).split(":")[1]);

        Socket dictionarySocket = new Socket(address, port);
        PrintWriter outputDictionary = new PrintWriter(dictionarySocket.getOutputStream(), true);

        outputDictionary.println(String.format("{%s, %s, %s}",
                                     wordToTranslate,
                                     (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", ""),
                                     clientListenPort));

        BufferedReader inputDictionary = new BufferedReader(new InputStreamReader(dictionarySocket.getInputStream()));
        String response = inputDictionary.readLine();

        dictionarySocket.close();

        if(!response.equals("OK")) throw new CustomException(response);
    }
}
