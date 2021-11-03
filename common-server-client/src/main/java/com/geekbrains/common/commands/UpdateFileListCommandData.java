package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class UpdateFileListCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = -5123678701220477249L;
    private List<String> files;
}
