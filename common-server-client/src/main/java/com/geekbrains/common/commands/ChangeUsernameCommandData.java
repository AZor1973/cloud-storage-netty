package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@AllArgsConstructor
public class ChangeUsernameCommandData implements Serializable {
    private String newName;
    @Serial
    private static final long serialVersionUID = -4200670006894188966L;
}
