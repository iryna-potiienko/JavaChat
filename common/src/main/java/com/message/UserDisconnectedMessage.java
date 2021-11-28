package com.message;


import java.io.Serializable;

public record UserDisconnectedMessage(String userName) implements Serializable {
}
