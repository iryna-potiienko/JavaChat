package com.message;


import java.io.Serializable;

public record UserConnectedMessage(String userName) implements Serializable {
}
