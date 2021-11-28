package com.chat.server;

import java.io.IOException;

public class ServerStarter {


    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer();

        chatServer.startServer();
    }
}
