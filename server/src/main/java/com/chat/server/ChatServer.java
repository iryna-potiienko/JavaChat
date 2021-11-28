package com.chat.server;


import com.message.DirectPair;
import com.message.UserSendCommonMessage;
import com.message.UserSendDirectMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {

    private volatile boolean isRunning = true;

    private final Map<String, UserHandler> userThreadsMap = new HashMap<>();

    private final List<String> connectedUsers = new ArrayList<>();

    private final List<UserSendCommonMessage> commonUserMessages = new ArrayList<>();

    private final Map<DirectPair, List<UserSendDirectMessage>> directMessagesPerUserPair = new HashMap<>();


    public void startServer() throws IOException {

        ServerSocket serverSocket = new ServerSocket(7777);

        while (isRunning) {
            Socket clientSocket = serverSocket.accept();

            UserHandler userHandler = new UserHandler(clientSocket, userThreadsMap, connectedUsers, commonUserMessages, directMessagesPerUserPair);
            userHandler.start();
        }

        serverSocket.close();
    }

    @SuppressWarnings("unused")
    private void stopServer() {
        isRunning = false;
    }
}
