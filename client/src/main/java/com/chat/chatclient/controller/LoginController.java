package com.chat.chatclient.controller;

import com.chat.chatclient.ClientChatStarter;
import com.chat.chatclient.handler.LoginHandlerThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    public TextField hostnameTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField usernameTextField;


    public void loginButtonAction() throws IOException {
        String hostname = hostnameTextField.getText();
        int port = Integer.parseInt(portTextField.getText());
        String username = usernameTextField.getText();

        LoginHandlerThread loginHandlerThread = new LoginHandlerThread(hostname, port, username);
        loginHandlerThread.setDaemon(true);

        loginHandlerThread.start();

        URL chatResource = ClientChatStarter.class.getResource("views/ChatView.fxml");
        FXMLLoader chatLoader = new FXMLLoader(chatResource);

        Parent window = chatLoader.load();

        Scene scene = new Scene(window);

        Platform.runLater(() -> {
            Stage stage = (Stage) hostnameTextField.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(1040);
            stage.setHeight(620);

            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(300);
        });
    }
}
