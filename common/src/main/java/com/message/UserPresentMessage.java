package com.message;

import java.io.Serializable;
import java.util.List;

public record UserPresentMessage(List<String> users) implements Serializable {
}
