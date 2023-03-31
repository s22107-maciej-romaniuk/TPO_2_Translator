package Client.GUI;

import Client.Model.Client;
import Client.Model.ClientServer;
import Common.CustomException;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class Controller {

    public TextField polishWordField;
    public TextField languageCodeField;
    public TextField translationField;
    public Label translationLabel;



    public void setServer(Client server) {
        this.server = server;
    }

    Client server = null;

    public void sendRequest(ActionEvent event){
        try {
            translationField.setText(server.requestTranslation(polishWordField.getText(), languageCodeField.getText()));
            translationLabel.setText("Translation");
        } catch (CustomException | IOException | InterruptedException e) {
            System.out.println("Client encountered problem");
            translationField.setText(e.getMessage());
            translationLabel.setText("Error message");
        }
    }
}
