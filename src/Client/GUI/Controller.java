package Client.GUI;

import Client.Model.Client;
import Client.Model.ClientServer;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import java.io.IOException;

public class Controller {

    public TextField polishWordField;
    public TextField languageCodeField;
    public TextField translationField;



    public void setServer(Client server) {
        this.server = server;
    }

    Client server = null;

    public void sendRequest(ActionEvent event) throws IOException, InterruptedException {
        System.out.println("request fired");
        translationField.setText(server.requestTranslation(polishWordField.getText(), languageCodeField.getText()));
    }
}
