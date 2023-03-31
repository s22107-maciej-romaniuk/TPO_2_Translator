package Client.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Client.Model.Client;

import java.io.IOException;

public class Main extends Application {
    Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        int port = Integer.parseInt(getParameters().getUnnamed().get(0));
        int proxyPort = Integer.parseInt(getParameters().getUnnamed().get(1));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        try {
            controller = loader.getController();
            controller.setServer(new Client(port, proxyPort));
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }
    }

    @Override
    public void stop() {
        if(controller.server.serverThread != null && controller.server.serverThread.isAlive() && !controller.server.clientServer.listeningSocket.isClosed()) {
            System.out.println("Interrupting server thread");
            try {
                controller.server.clientServer.listeningSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
