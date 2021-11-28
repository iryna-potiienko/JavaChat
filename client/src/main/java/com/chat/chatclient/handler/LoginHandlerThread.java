package com.chat.chatclient.handler;

import java.net.Socket;

public class LoginHandlerThread extends Thread {

    private final String host;

    private final int port;

    private final String USER_NAME;


    public LoginHandlerThread(String host, int port, String userName) {
        this.host = host;
        this.port = port;
        this.USER_NAME = userName;
    }

    @Override
    public void run() {

        try {
            Socket clientSocket = new Socket(host, port);
            Client client = new Client(clientSocket, USER_NAME);

            try {
                client.start();

            } catch (Exception ignored) {
            }

            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
