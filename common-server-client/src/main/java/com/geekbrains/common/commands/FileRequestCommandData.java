package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class FileRequestCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 878500430225365157L;
    private String fileName;
}
