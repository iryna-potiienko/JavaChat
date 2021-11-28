package com.chat.chatclient.model;

public record UserWrapper(String name) {

    @Override
    public String toString() {
        return "User: " + name;
    }
}
