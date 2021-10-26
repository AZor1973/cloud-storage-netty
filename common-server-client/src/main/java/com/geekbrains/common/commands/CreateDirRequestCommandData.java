package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
@Getter
@AllArgsConstructor
public class CreateDirRequestCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = -7745466602287660770L;
    private String name;
}
