package com.message;

import java.io.Serializable;

public record UserSendDirectMessage(String time, DirectPair directPair, String message) implements Serializable {

}
