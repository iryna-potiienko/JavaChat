package com.chat.chatclient.controller;

import com.chat.chatclient.handler.Client;
import com.chat.chatclient.model.UserWrapper;
import com.message.DirectPair;
import com.message.UserSendCommonMessage;
import com.message.UserSendDirectMessage;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChatController {

    @FXML
    public Button buttonSend;

    @FXML
    private TextArea messageBox;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label onlineCountLabel;

    @FXML
    private ListView<UserWrapper> userList;

    @FXML
    private ListView<HBox> chatPane;

    @FXML
    private Button commonChatButton;

    @FXML
    private Button directChatButton;

    private String userName;

    private boolean isCommonMode = true;

    private static ChatController self;

    private final Map<DirectPair, List<UserSendDirectMessage>> directMessagesPerUserPair = new HashMap<>();

    private DirectPair currentActiveDirectPair;

    private List<UserSendCommonMessage> userSendCommonMessages = new ArrayList<>();


    public ChatController() {
        self = this;
    }

    @FXML
    public void initialize() {
        userName = Client.getClient().getUserName();

        usernameLabel.setText(userName);

        userList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            currentActiveDirectPair = new DirectPair(userName, newValue.name());
            chatPane.getItems().clear();

            switchToDirectChat();
        });

        if (isCommonMode) {
            commonChatButton.setDisable(true);
            directChatButton.setDisable(false);
        } else {
            commonChatButton.setDisable(false);
            directChatButton.setDisable(true);
        }
    }

    public static ChatController getChatController() {
        return self;
    }

    public void sendMethod() {
    }

    public void sendButtonAction() throws IOException {
        String messageText = messageBox.getText();

        String time = LocalTime.now().withNano(0).toString();
        String messageContent = time + " : " + messageText;

        if (isCommonMode) {
            addSelfCommonMessage(time, userName, messageContent);
            Client.getClient().sendCommonMessage(time, messageText);
        } else {
            addSelfDirectMessage(time, messageContent, currentActiveDirectPair);

            Client.getClient().sendDirectMessage(time, messageText, currentActiveDirectPair);
        }

        messageBox.clear();
    }

    public void switchToCommonChat() {
        commonChatButton.setDisable(true);
        directChatButton.setDisable(false);

        isCommonMode = true;

        chatPane.getItems().clear();
        updateChatCommonMessages();
    }

    public void switchToDirectChat() {
        commonChatButton.setDisable(false);
        directChatButton.setDisable(true);

        isCommonMode = false;

        updateDirectMessages();
    }

    private void updateDirectMessages() {
        List<UserSendDirectMessage> userSendCommonMessages = directMessagesPerUserPair.get(currentActiveDirectPair);
        if (userSendCommonMessages != null && !userSendCommonMessages.isEmpty()) {
            updateChatDirectMessages(userSendCommonMessages);
        }
    }

    private void updateChatCommonMessages() {
        for (UserSendCommonMessage userSendCommonMessage : userSendCommonMessages) {
            if (userName.equals(userSendCommonMessage.userName())) {
                insertSelfMessage(userSendCommonMessage.message());
            } else {
                insertMessage(userSendCommonMessage.message());
            }
        }
    }

    private void updateChatDirectMessages(List<UserSendDirectMessage> userSendCommonMessages) {
        for (UserSendDirectMessage userSendCommonMessage : userSendCommonMessages) {
            if (userName.equals(userSendCommonMessage.directPair().from())) {
                insertSelfMessage(userSendCommonMessage.message());
            } else {
                String messageContent = userSendCommonMessage.directPair().from() + " : " + userSendCommonMessage.time() + " : " + userSendCommonMessage.message();
                insertMessage(messageContent);
            }
        }
    }

    public void addConnectedUser(String userName) {
        userList.getItems().add(new UserWrapper(userName));
        updateUserCounter();
    }

    public void removeDisconnectedUser(String userName) {
        Optional<UserWrapper> userOptional = userList.getItems().stream()
                .filter(userWrapper -> userName.equals(userWrapper.name()))
                .findFirst();

        userOptional.ifPresent(userWrapper -> userList.getItems().remove(userWrapper));

        updateUserCounter();
    }

    public void updateUserCounter() {
        int size = userList.getItems().size() + 1;
        onlineCountLabel.setText(String.valueOf(size));
    }

    public void addSelfDirectMessage(String time, String message, DirectPair directPair) {
        insertSelfMessage(message);

        addDirect(time, directPair != null ? directPair : currentActiveDirectPair, message);
    }

    public void addDirectMessage(String time, DirectPair directPair, String message) {
        String messageContent = directPair.from() + " : " + time + " : " + message;
        insertMessage(messageContent);

        addDirect(time, directPair, message);
    }

    public void addDirectMessageWithoutShowing(String time, DirectPair directPair, String message) {
        addDirect(time, directPair, message);
    }

    private void insertMessage(String message) {
        HBox hBox = new HBox();
        Label label = new Label();
        label.setText(message);
        label.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE,
                null, null))
        );
        hBox.setAlignment(Pos.TOP_LEFT);
        hBox.getChildren().add(label);

        chatPane.getItems().add(hBox);
    }

    public void addSelfCommonMessage(String time, String userName, String message) {
        insertSelfMessage(message);

        userSendCommonMessages.add(new UserSendCommonMessage(time, userName, message));
    }

    private void insertSelfMessage(String message) {
        HBox hBox = new HBox();
        Label label = new Label();
        label.setText(message);
        label.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                null, null))
        );
        hBox.setAlignment(Pos.TOP_RIGHT);
        hBox.getChildren().add(label);

        chatPane.getItems().add(hBox);
    }

    private void addDirect(String time, DirectPair directPair, String message) {
        List<UserSendDirectMessage> userSendDirectMessages = directMessagesPerUserPair.get(directPair);
        if (userSendDirectMessages == null) {
            userSendDirectMessages = new ArrayList<>();
            userSendDirectMessages.add(new UserSendDirectMessage(time, directPair, message));
            directMessagesPerUserPair.put(directPair, userSendDirectMessages);
        } else {
            userSendDirectMessages.add(new UserSendDirectMessage(time, directPair, message));
        }
    }

    public void addCommonMessage(String time, String userName, String message) {
        insertMessage(message);
        userSendCommonMessages.add(new UserSendCommonMessage(time, userName, message));
    }

    public void addDirectMessagesPerUserPair(Map<DirectPair, List<UserSendDirectMessage>> directMessagesPerUserPair) {
        if (directMessagesPerUserPair.isEmpty()) {
            return;
        }

        this.directMessagesPerUserPair.putAll(directMessagesPerUserPair);

        updateDirectMessages();
    }

    public void setUserSendCommonMessages(List<UserSendCommonMessage> userSendCommonMessages) {
        this.userSendCommonMessages = userSendCommonMessages;
    }

    public boolean isCommonMode() {
        return isCommonMode;
    }
}
