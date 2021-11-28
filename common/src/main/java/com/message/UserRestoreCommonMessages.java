package com.message;

import java.io.Serializable;
import java.util.List;

public record UserRestoreCommonMessages(List<UserSendCommonMessage> commonMessages) implements Serializable {
}
