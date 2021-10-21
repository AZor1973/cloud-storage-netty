package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
@AllArgsConstructor
@Getter
public class ErrorCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 4296257073209550530L;
    private final String errorMessage;
}
