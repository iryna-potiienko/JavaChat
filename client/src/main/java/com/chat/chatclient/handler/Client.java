package com.chat.chatclient.handler;


import com.chat.chatclient.controller.ChatController;
import com.message.DirectPair;
import com.message.MessageType;
import com.message.MessageWrapper;
import com.message.UserConnectedMessage;
import com.message.UserDisconnectedMessage;
import com.message.UserPresentMessage;
import com.message.UserRestoreCommonMessages;
import com.message.UserRestoreDirectMessages;
import com.message.UserSendCommonMessage;
import com.message.UserSendDirectMessage;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Client {

    private final Socket clientSocket;

    private ObjectOutputStream out;

    private ObjectInputStream in;

    private final String userName;

    private ChatController chatController;

    private static Client self;

    public static Client getClient() {
        return self;
    }


    public Client(Socket clientSocket, String userName) {
        this.clientSocket = clientSocket;
        this.userName = userName;

        self = this;
    }

    public void start() throws IOException, ClassNotFoundException {
        init();

        sendUserConnectedMessage();

        while (clientSocket.isConnected()) {
            @SuppressWarnings("unchecked") MessageWrapper<Object> messageWrapper = (MessageWrapper<Object>) in.readObject();


            switch (messageWrapper.messageType()) {
                case USER_CONNECTED -> {
                    UserConnectedMessage message = (UserConnectedMessage) messageWrapper.message();
                    String connectedUserName = message.userName();

                    Platform.runLater(() -> chatController.addConnectedUser(connectedUserName));
                }
                case USER_PRESENT -> {
                    UserPresentMessage message = (UserPresentMessage) messageWrapper.message();

                    List<String> presentUsers = message.users();

                    Platform.runLater(() -> presentUsers.forEach(userName -> chatController.addConnectedUser(userName)));

                }
                case USER_DISCONNECTED -> {
                    UserDisconnectedMessage message = (UserDisconnectedMessage) messageWrapper.message();

                    String disconnectedUser = message.userName();

                    Platform.runLater(() -> chatController.removeDisconnectedUser(disconnectedUser));
                }
                case USER_SEND_COMMON_MESSAGE -> {
                    UserSendCommonMessage message = (UserSendCommonMessage) messageWrapper.message();

                    String time = message.time();
                    String commonMessage = message.message();
                    String userName = message.userName();

                    String messageContent = userName + " : " + time + " : " + commonMessage;

                    if (chatController.isCommonMode()) {
                        Platform.runLater(() -> chatController.addCommonMessage(time, userName, messageContent));
                    }
                }
                case USER_RESTORE_COMMON_MESSAGES -> {
                    UserRestoreCommonMessages message = (UserRestoreCommonMessages) messageWrapper.message();

                    List<UserSendCommonMessage> userSendCommonMessages = message.commonMessages();
                    if (userSendCommonMessages.isEmpty()) {
                        break;
                    }

                    for (UserSendCommonMessage userSendCommonMessage : userSendCommonMessages) {
                        String messageContent = userSendCommonMessage.time() + " : " + userSendCommonMessage.message();

                        if (userName.equals(userSendCommonMessage.userName())) {
                            Platform.runLater(() -> chatController.addSelfCommonMessage(userSendCommonMessage.time(), userSendCommonMessage.userName(), messageContent));
                        } else {
                            Platform.runLater(() -> chatController.addCommonMessage(userSendCommonMessage.time(), userSendCommonMessage.userName(), messageContent));
                        }
                    }

                    chatController.setUserSendCommonMessages(userSendCommonMessages);
                }
                case RESTORE_DIRECT_MESSAGES -> {
                    UserRestoreDirectMessages message = (UserRestoreDirectMessages) messageWrapper.message();

                    Platform.runLater(() -> chatController.addDirectMessagesPerUserPair(message.directMessages()));
                }
                case USER_SEND_DIRECT_MESSAGE -> {
                    UserSendDirectMessage message = (UserSendDirectMessage) messageWrapper.message();

                    if (!chatController.isCommonMode()) {
                        Platform.runLater(() -> chatController.addDirectMessage(message.time(), message.directPair(), message.message()));
                    } else {
                        Platform.runLater(() -> chatController.addDirectMessageWithoutShowing(message.time(), message.directPair(), message.message()));
                    }
                }

            }
        }


        in.close();
        out.close();
    }

    private void init() throws IOException {
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());

        chatController = ChatController.getChatController();
    }

    private void sendUserConnectedMessage() throws IOException {
        MessageWrapper<UserConnectedMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_CONNECTED, new UserConnectedMessage(userName));

        out.writeObject(messageWrapper);
    }

    public void disconnectUser() throws IOException {
        MessageWrapper<UserDisconnectedMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_DISCONNECTED, new UserDisconnectedMessage(userName));

        out.writeObject(messageWrapper);
    }

    public void sendCommonMessage(String time, String message) throws IOException {
        MessageWrapper<UserSendCommonMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_SEND_COMMON_MESSAGE, new UserSendCommonMessage(time, userName, message));

        out.writeObject(messageWrapper);
    }

    public void sendDirectMessage(String time, String message, DirectPair directPair) throws IOException {
        MessageWrapper<UserSendDirectMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_SEND_DIRECT_MESSAGE, new UserSendDirectMessage(time, directPair, message));

        out.writeObject(messageWrapper);
    }

    public String getUserName() {
        return userName;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
