package com.geekbrains.client;

import com.geekbrains.client.dialogs.Dialogs;
import com.geekbrains.common.Command;
import com.geekbrains.common.CommandType;
import com.geekbrains.common.commands.AuthOkCommandData;
import com.geekbrains.common.commands.ErrorCommandData;
import com.geekbrains.common.commands.InfoCommandData;
import com.geekbrains.common.commands.UpdateFileListCommandData;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

@Slf4j
public class Network {

    private static final int SERVER_PORT = 8189;
    private static final String SERVER_HOST = "localhost";
    private static Network INSTANCE;
    private final String host;
    private final int port;
    private ObjectDecoderInputStream dis;
    private ObjectEncoderOutputStream dos;
    private boolean connected;

    public static Network getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Network();
        }
        return INSTANCE;
    }

    private Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Network() {
        this(SERVER_HOST, SERVER_PORT);
    }

    public void connect() {
        try {
            Socket socket = new Socket(host, port);
            dos = new ObjectEncoderOutputStream(socket.getOutputStream());
            dis = new ObjectDecoderInputStream(socket.getInputStream(), Integer.MAX_VALUE);
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to establish connection");
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendFile(String fileName, long fileSize, byte[] bytes) throws IOException {
        sendCommand(Command.uploadFileCommand(fileName, fileSize, bytes));
    }

    public String readMessage() throws IOException {
        Command command = readCommand();
        if (command.getType() == CommandType.INFO) {
            InfoCommandData data = (InfoCommandData) command.getData();
            return data.getMessage();
        } else if (command.getType() == CommandType.ERROR) {
            ErrorCommandData data = (ErrorCommandData) command.getData();
            String error = data.getErrorMessage();
            if (error.trim().equals("Incorrect login or password!")){
                Platform.runLater(Dialogs.AuthError.INVALID_CREDENTIALS::show);
            }else if (error.trim().equals("This user is already signed in!")){
                Platform.runLater(Dialogs.AuthError.USERNAME_BUSY::show);
            }
            return error;
        } else if (command.getType() == CommandType.AUTH_OK) {
            AuthOkCommandData data = (AuthOkCommandData) command.getData();
            String message = data.getUsername();
            Platform.runLater(() -> App.INSTANCE.switchToMainChatWindow(message));
            return "You are logged in as a " + message;
        }else if (command.getType() == CommandType.UPDATE_FILE_LIST){
            UpdateFileListCommandData data = (UpdateFileListCommandData) command.getData();
            List<String> files = data.getFiles();
            Platform.runLater(() -> App.INSTANCE.getMainController().updateServerListView(files));
            return "";
        }
        return "Unrecognized message";
    }

    public Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) dis.readObject();
        } catch (ClassNotFoundException e) {
            log.error("Failed to read Command class");
            e.printStackTrace();
        }
        return command;
    }

    private void sendCommand(Command command) throws IOException {
        try {
            dos.writeObject(command);
            dos.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message to server");
            throw e;
        }
    }

    public void sendAuthMessage(String login, String password) throws IOException {
        sendCommand(Command.authCommand(login, password));
    }
}