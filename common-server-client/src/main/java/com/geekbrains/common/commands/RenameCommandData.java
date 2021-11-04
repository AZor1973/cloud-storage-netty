package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@AllArgsConstructor
public class RenameCommandData implements Serializable {
    private String file;
    private String newName;
    @Serial
    private static final long serialVersionUID = 2389971417625203231L;
}
