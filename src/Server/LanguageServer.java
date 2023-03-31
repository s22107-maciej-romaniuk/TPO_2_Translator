package Server;

import Common.Constants;
import Common.CustomException;
import Proxy.ProxyJob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageServer {
    static Integer serverPort;
    static String language;
    static Map<String, String> dictionary = new HashMap<>();
//    static Map<String, String> dictionary = Stream.of(new String[][] {
//            { "kaczka", "duck" },
//            { "pies", "dog" },
//    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    static Socket socketProxy;
    static BufferedReader inputProxy;
    static PrintWriter outputProxy;
    static String wordToTranslate;
    static String clientAddress;
    static String clientPort;
    static Socket clientSocket;
    static PrintWriter outputClient;
    static Socket proxyServerSocket;
    static int proxyPort;

    public static void main(String[] args) throws IOException {
        proxyPort = Integer.parseInt(args[0]);
        serverPort = Integer.parseInt(args[1]);
        language = args[2];
        for(int i = 3; i< args.length; i = i + 2){
            dictionary.put(args[i], args[i+1]);
        }
        registerDictionaryInProxy();
        try (ServerSocket listeningSocket = new ServerSocket(serverPort)){
            while(true){
                waitForConnectionFromProxy(listeningSocket);
                try {
                    readMessageFromProxy();
                    openConnectionToClient();
                    sendTranslatedWordToClient();
                    sendCallbackMessageToProxy("OK");
                }
                catch(Exception ex){
                    sendCallbackMessageToProxy(ex.getMessage());
                }
                finally {
                    socketProxy.close();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerDictionaryInProxy() throws IOException {
        proxyServerSocket = new Socket("localhost", proxyPort);
        outputProxy = new PrintWriter(proxyServerSocket.getOutputStream(), true);
        outputProxy.println(Constants.TRANSLATION_SERVER_REGISTRATION);
        outputProxy.println(language);
        outputProxy.println(serverPort);
        proxyServerSocket.close();
    }

    private static void sendCallbackMessageToProxy(String message) {
        LanguageServer.outputProxy.println(message);
    }

    private static void sendTranslatedWordToClient() throws CustomException {
        if (dictionary.containsKey(wordToTranslate)) {
            outputClient.println(dictionary.get(wordToTranslate));
        } else {
            throw new CustomException("Unknown word");
        }
    }

    private static void openConnectionToClient() throws IOException {
        clientSocket = new Socket(clientAddress, Integer.parseInt(clientPort));
        outputClient = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    private static void readMessageFromProxy() throws IOException {
        String initialMessage = inputProxy.readLine();
        System.out.println(initialMessage);

        String[] data = initialMessage.substring(1, initialMessage.length() - 1).split(", ");

        wordToTranslate = data[0];
        clientAddress = data[1];
        clientPort = data[2];
    }

    private static void waitForConnectionFromProxy(ServerSocket listeningSocket) throws IOException {
        socketProxy = listeningSocket.accept();
        System.out.println("connection detected");
        inputProxy = new BufferedReader(new InputStreamReader(socketProxy.getInputStream()));
        outputProxy = new PrintWriter(socketProxy.getOutputStream(), true);
    }

}
