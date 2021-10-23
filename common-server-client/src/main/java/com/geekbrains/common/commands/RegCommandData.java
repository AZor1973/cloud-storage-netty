package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
@AllArgsConstructor
@Getter
public class RegCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 6484461838698096469L;
    private   String username;
    private  String login;
    private  String password;
}
