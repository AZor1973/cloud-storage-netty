package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class DownloadFileCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = -2401117997612892486L;
}
