package com.geekbrains.common;

import java.io.Serial;
import java.io.Serializable;

public class AbstractMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = -7833540817991867581L;
    private String message;

    public AbstractMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
