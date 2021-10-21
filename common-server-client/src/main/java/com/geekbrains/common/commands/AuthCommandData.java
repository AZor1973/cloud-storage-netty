package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
@AllArgsConstructor
@Getter
public class AuthCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 5990269751251616948L;
    private final String login;
    private final String password;
}
