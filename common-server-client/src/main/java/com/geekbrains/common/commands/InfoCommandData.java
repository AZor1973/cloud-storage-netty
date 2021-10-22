package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class InfoCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 403275948505041983L;

    private String message;
}
