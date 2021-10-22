package com.geekbrains.common;

import com.geekbrains.common.commands.*;

import java.io.Serial;
import java.io.Serializable;

public class Command implements Serializable {
    @Serial
    private static final long serialVersionUID = 4527858572263852177L;
    private Object data;
    private CommandType type;

    public Object getData() {
        return data;
    }

    public CommandType getType() {
        return type;
    }

    public static Command authCommand(String login, String password) {
        Command command = new Command();
        command.data = new AuthCommandData(login, password);
        command.type = CommandType.AUTH;
        return command;
    }

    public static Command authOkCommand(String username) {
        Command command = new Command();
        command.data = new AuthOkCommandData(username);
        command.type = CommandType.AUTH_OK;
        return command;
    }

    public static Command errorCommand(String errorMessage) {
        Command command = new Command();
        command.type = CommandType.ERROR;
        command.data = new ErrorCommandData(errorMessage);
        return command;
    }

    public static Command fileUploadCommand(String fileName, long fileSize, byte[] bytes){
        Command command = new Command();
        command.type = CommandType.FILE_UPLOAD;
        command.data = new FileUploadCommandData(fileName, fileSize, bytes);
        return command;
    }

    public static Command infoCommand(String message){
        Command command = new Command();
        command.type = CommandType.INFO;
        command.data = new InfoCommandData(message);
        return command;
    }
}
