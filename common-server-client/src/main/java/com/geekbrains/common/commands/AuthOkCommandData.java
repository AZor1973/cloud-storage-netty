package com.geekbrains.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
@AllArgsConstructor
@Getter
public class AuthOkCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = -90080607359957848L;
    private final String username;
}
