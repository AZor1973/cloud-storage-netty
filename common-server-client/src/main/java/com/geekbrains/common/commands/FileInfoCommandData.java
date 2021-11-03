package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class FileInfoCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 8128138402141926015L;
    private String fileName;
    private long fileSize;
    private byte[] bytes;
    private boolean isStart;
    private int endPos;
}
