package com.message;


import java.io.Serializable;

public record MessageWrapper<T>(MessageType messageType, T message) implements Serializable {
}
