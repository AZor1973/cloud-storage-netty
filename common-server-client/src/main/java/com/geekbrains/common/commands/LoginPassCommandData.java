package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
@AllArgsConstructor
@Getter
public class LoginPassCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 3326702167241249638L;
    private final String username;
}
