package com.geekbrains.client;

import com.geekbrains.common.Command;
import com.geekbrains.common.CommandType;
import com.geekbrains.common.commands.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Network error", Alert.AlertType.ERROR);
        }
    }

    public void sendFile(String fileName, long fileSize, byte[] bytes) throws IOException {
        sendCommand(Command.uploadFileCommand(fileName, fileSize, bytes));
    }

    public void readMessage() throws IOException {
        Command command = readCommand();
        if (command.getType() == CommandType.INFO) {
            InfoCommandData data = (InfoCommandData) command.getData();
            Platform.runLater(() -> showAlert(data.getMessage(), Alert.AlertType.INFORMATION));
        } else if (command.getType() == CommandType.ERROR) {
            ErrorCommandData data = (ErrorCommandData) command.getData();
            String error = data.getErrorMessage();
                Platform.runLater(() -> showAlert(error, Alert.AlertType.ERROR));
        } else if (command.getType() == CommandType.AUTH_OK) {
            AuthOkCommandData data = (AuthOkCommandData) command.getData();
            String message = data.getUsername();
            Platform.runLater(() -> App.INSTANCE.switchToMainChatWindow(message));

        }else if (command.getType() == CommandType.UPDATE_FILE_LIST){
            UpdateFileListCommandData data = (UpdateFileListCommandData) command.getData();
            List<String> files = data.getFiles();
            Platform.runLater(() -> App.INSTANCE.getMainController().updateServerListView(files));
        } else if (command.getType() == CommandType.FILE_UPLOAD) {
            UploadFileCommandData data = (UploadFileCommandData) command.getData();
            String fileName = data.getFileName();
            long fileSize = data.getFileSize();
            Path path = App.INSTANCE.getMainController().getCurrentPath().resolve(fileName);
            if (!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, data.getBytes(), StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                if (Files.size(path) == fileSize) {
                    Platform.runLater(() -> App.INSTANCE.getMainController().updateClientListViewStatic());
                    Platform.runLater(() -> showAlert(fileName + " downloaded.", Alert.AlertType.INFORMATION));
                }else {
                    Files.delete(path);
                    Platform.runLater(() -> showAlert("File download error.", Alert.AlertType.ERROR));
                }
            }else {
                Platform.runLater(() -> showAlert(fileName + " is already exists.", Alert.AlertType.ERROR));
            }
        }
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

    public void sendRegMessage(String username, String login, String password) throws IOException {
        sendCommand(Command.regCommand(username, login, password));
    }

    public void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void sendFileRequest(String fileNameToDownload) throws IOException {
        sendCommand(Command.fileRequestCommand(fileNameToDownload));
    }

    public void sendUpRequest() throws IOException {
        sendCommand(Command.upRequestCommand());
    }

    public void sendDeleteRequest(String fileName) throws IOException {
        sendCommand(Command.deleteRequestCommand(fileName));
    }

    public void sendCreateDirRequest(String name) throws IOException {
        sendCommand(Command.createDirRequestCommand(name));
    }
}