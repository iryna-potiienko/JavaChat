package com.chat.chatclient;

import com.chat.chatclient.handler.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ClientChatStarter extends Application {



    @Override
    public void start(Stage stage) throws IOException {
        URL resource = ClientChatStarter.class.getResource("views/LoginView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Scene scene = new Scene(fxmlLoader.load(), 350, 420);

        stage.setScene(scene);

        stage.setTitle("Socket Chat");

        stage.getIcons().add(new Image(Objects.requireNonNull(ClientChatStarter.class.getResource("clientImages/plug.png")).toString()));

        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(e -> {

            try {
                Client client = Client.getClient();
                if (client == null) {
                    return;
                }

                client.disconnectUser();

                Thread.sleep(1000);

                client.getClientSocket().close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}