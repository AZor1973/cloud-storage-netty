package com.geekbrains.common;

import com.geekbrains.common.commands.*;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@Getter
public class Command implements Serializable {
    @Serial
    private static final long serialVersionUID = 4527858572263852177L;
    private Object data;
    private CommandType type;

    public static Command regCommand(String username, String login, String password) {
        Command command = new Command();
        command.data = new RegCommandData(username, login, password);
        command.type = CommandType.REG;
        return command;
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

    public static Command fileRequestCommand(String fileName){
        Command command = new Command();
        command.data = new FileRequestCommandData(fileName);
        command.type = CommandType.FILE_REQUEST;
        return command;
    }

    public static Command errorCommand(String errorMessage) {
        Command command = new Command();
        command.type = CommandType.ERROR;
        command.data = new ErrorCommandData(errorMessage);
        return command;
    }

    public static Command uploadFileCommand(String fileName, long fileSize, byte[] bytes){
        Command command = new Command();
        command.type = CommandType.FILE_UPLOAD;
        command.data = new UploadFileCommandData(fileName, fileSize, bytes);
        return command;
    }

    public static Command infoCommand(String message){
        Command command = new Command();
        command.type = CommandType.INFO;
        command.data = new InfoCommandData(message);
        return command;
    }

    public static Command updateFileListCommand(List<String> files){
        Command command = new Command();
        command.type = CommandType.UPDATE_FILE_LIST;
        command.data = new UpdateFileListCommandData(files);
        return command;
    }

    public static Command upRequestCommand() {
        Command command = new Command();
        command.type = CommandType.UP_REQUEST;
        return command;
    }
}
