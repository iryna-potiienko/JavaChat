package com.chat.server;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserHandler extends Thread {

    private final Socket socket;

    private final Map<String, UserHandler> userThreadsMap;

    private final List<String> connectedUsers;

    private final List<UserSendCommonMessage> commonUserMessages;

    private final Map<DirectPair, List<UserSendDirectMessage>> directMessagesPerUserPair;

    private ObjectOutputStream out;

    private ObjectInputStream in;

    private String userName;


    public UserHandler(Socket socket, Map<String, UserHandler> userThreadsMap, List<String> connectedUsers, List<UserSendCommonMessage> commonUserMessages, Map<DirectPair, List<UserSendDirectMessage>> directMessagesPerUserPair) {
        this.socket = socket;
        this.userThreadsMap = userThreadsMap;
        this.connectedUsers = connectedUsers;
        this.commonUserMessages = commonUserMessages;
        this.directMessagesPerUserPair = directMessagesPerUserPair;
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        while (socket.isConnected()) {

            try {
                @SuppressWarnings("unchecked") MessageWrapper<Object> messageWrapper = (MessageWrapper<Object>) in.readObject();

                switch (messageWrapper.messageType()) {
                    case USER_CONNECTED -> {

                        UserConnectedMessage message = (UserConnectedMessage) messageWrapper.message();
                        String userName = message.userName();

                        this.userName = userName;

                        List<UserHandler> userHandlers = resolveRecipients();

                        for (UserHandler userHandler : userHandlers) {
                            sendUserConnectedMessage(userHandler, userName);
                        }

                        connectedUsers.add(userName);
                        userThreadsMap.put(userName, this);

                        List<String> presentUsers = resolvePresentUsersExceptConnected(message.userName());
                        if (!presentUsers.isEmpty()) {
                            sendPresentUsers(presentUsers);
                        }

                        restoreCommonMessages();
                        restoreDirectMessages();
                    }
                    case USER_DISCONNECTED -> {
                        UserDisconnectedMessage message = (UserDisconnectedMessage) messageWrapper.message();

                        String disconnectedUser = message.userName();

                        List<UserHandler> userHandlers = resolveRecipients();

                        for (UserHandler userHandler : userHandlers) {
                            sendUserDisconnect(userHandler, disconnectedUser);
                        }

                        userThreadsMap.remove(disconnectedUser);
                        connectedUsers.remove(disconnectedUser);

                        socket.close();
                    }
                    case USER_SEND_COMMON_MESSAGE -> {
                        UserSendCommonMessage message = (UserSendCommonMessage) messageWrapper.message();

                        List<UserHandler> userHandlers = resolveRecipients();

                        for (UserHandler userHandler : userHandlers) {
                            sendCommonMessage(userHandler, message.time(), message.userName(), message.message());
                        }

                        commonUserMessages.add(message);
                    }
                    case USER_SEND_DIRECT_MESSAGE -> {
                        UserSendDirectMessage message = (UserSendDirectMessage) messageWrapper.message();

                        UserHandler userHandler = resolveRecipient(message.directPair().to());

                        sendDirectMessage(userHandler, message.time(), message.directPair(), message.message());

                        List<UserSendDirectMessage> userSendDirectMessages = directMessagesPerUserPair.get(message.directPair());
                        if (userSendDirectMessages == null) {
                            userSendDirectMessages = new ArrayList<>();

                            userSendDirectMessages.add(message);
                            directMessagesPerUserPair.put(message.directPair(), userSendDirectMessages);
                        } else {
                            userSendDirectMessages.add(message);
                        }
                    }
                }

            } catch (Exception e) {
                if (e instanceof SocketException) {
                    return;
                }
                e.printStackTrace();
            }
        }

        try {
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private List<UserHandler> resolveRecipients() {
        return userThreadsMap.entrySet().stream()
                .filter(stringUserHandlerEntry -> !userName.equals(stringUserHandlerEntry.getKey()))
                .map((Map.Entry::getValue))
                .collect(Collectors.toList());
    }

    private List<String> resolvePresentUsersExceptConnected(String userName) {
        return connectedUsers.stream()
                .filter(name -> !userName.equals(name))
                .collect(Collectors.toList());
    }

    private UserHandler resolveRecipient(String to) {
        UserHandler userHandler = userThreadsMap.get(to);
        if (userHandler == null) {
            throw new RuntimeException();
        }

        return userHandler;
    }


    public ObjectOutputStream getOut() {
        return out;
    }

    private void sendUserConnectedMessage(UserHandler userHandler, String userName) throws IOException {
        MessageWrapper<UserConnectedMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_CONNECTED, new UserConnectedMessage(userName));

        userHandler.getOut().writeObject(messageWrapper);
    }

    private void sendPresentUsers(List<String> presentUsers) throws IOException {
        MessageWrapper<UserPresentMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_PRESENT, new UserPresentMessage(presentUsers));

        out.writeObject(messageWrapper);
    }

    private void sendUserDisconnect(UserHandler userHandler, String userName) throws IOException {
        MessageWrapper<UserDisconnectedMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_DISCONNECTED, new UserDisconnectedMessage(userName));

        userHandler.getOut().writeObject(messageWrapper);
    }

    private void sendCommonMessage(UserHandler userHandler, String time, String userName, String message) throws IOException {
        MessageWrapper<UserSendCommonMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_SEND_COMMON_MESSAGE, new UserSendCommonMessage(time, userName, message));

        userHandler.getOut().writeObject(messageWrapper);
    }

    private void sendDirectMessage(UserHandler userHandler, String time, DirectPair directPair, String message) throws IOException {
        MessageWrapper<UserSendDirectMessage> messageWrapper = new MessageWrapper<>(MessageType.USER_SEND_DIRECT_MESSAGE, new UserSendDirectMessage(time, directPair, message));

        userHandler.getOut().writeObject(messageWrapper);
    }

    private void restoreCommonMessages() throws IOException {
        MessageWrapper<UserRestoreCommonMessages> messageWrapper = new MessageWrapper<>(MessageType.USER_RESTORE_COMMON_MESSAGES, new UserRestoreCommonMessages(commonUserMessages));

        out.writeObject(messageWrapper);
    }

    private void restoreDirectMessages() throws IOException {
        if (directMessagesPerUserPair.isEmpty()) {
            return;
        }

        MessageWrapper<UserRestoreDirectMessages> messageWrapper = new MessageWrapper<>(MessageType.RESTORE_DIRECT_MESSAGES, new UserRestoreDirectMessages(directMessagesPerUserPair));

        out.writeObject(messageWrapper);
    }
}
