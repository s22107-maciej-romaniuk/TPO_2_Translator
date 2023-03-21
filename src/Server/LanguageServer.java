package Server;

import Common.Constants;
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
    static Integer serverPort = 7779;
    static String language = "EN";
    static Map<String, String> dictionary = Stream.of(new String[][] {
        { "kaczka", "duck" },
        { "pies", "dog" },
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public static void main(String[] args) throws IOException {
        Socket proxyServerSocket = new Socket("localhost", 7777);
        PrintWriter output = new PrintWriter(proxyServerSocket.getOutputStream(), true);
        output.println(Constants.TRANSLATION_SERVER_REGISTRATION);
        output.println(language);
        output.println(serverPort);
        try (ServerSocket listeningSocket = new ServerSocket(serverPort)){
            while(true){
                Socket socket = listeningSocket.accept();
                System.out.println("connection detected");
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String initialMessage = input.readLine();
                System.out.println(initialMessage);

                String[] data = initialMessage.substring(1, initialMessage.length()-1).split(", ");
                String wordToTranslate = data[0];
                String clientAddress = data[1];
                String clientPort = data[2];
                Socket clientSocket = new Socket(clientAddress, Integer.parseInt(clientPort));
                output = new PrintWriter(clientSocket.getOutputStream(), true);
                output.println(dictionary.get(wordToTranslate));
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
