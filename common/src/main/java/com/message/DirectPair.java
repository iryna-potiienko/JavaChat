package com.message;

import java.io.Serializable;
import java.util.Objects;

public record DirectPair(String from, String to) implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectPair that = (DirectPair) o;
        return (Objects.equals(from, that.from) || Objects.equals(from, that.to)) && (Objects.equals(to, that.to) || Objects.equals(to, that.from));
    }

    @Override
    public int hashCode() {
        return Objects.hash(from) + Objects.hash(to);
    }
}
