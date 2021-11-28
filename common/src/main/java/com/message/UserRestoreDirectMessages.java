package com.message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record UserRestoreDirectMessages(Map<DirectPair, List<UserSendDirectMessage>> directMessages) implements Serializable {
}
