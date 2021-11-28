package com.message;

import java.io.Serializable;

public record UserSendCommonMessage(String time, String userName, String message) implements Serializable {
}
