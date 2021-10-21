package com.geekbrains.client;

import com.geekbrains.common.Command;
import com.geekbrains.common.CommandType;
import com.geekbrains.common.commands.ErrorCommandData;
import com.geekbrains.common.commands.FileInfoCommandData;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.Socket;

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
            dis = new ObjectDecoderInputStream(socket.getInputStream(), 2000000000);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to establish connection");
        }
    }

    public  void sendFileInfo(String fileName, long fileSize, byte[] bytes) throws IOException {
        sendCommand(Command.fileInfoCommand(fileName, fileSize, bytes));
    }

    public String readMessage() throws IOException {
        Command command = readCommand();
        if (command.getType() == CommandType.FILE_INFO) {
            FileInfoCommandData data = (FileInfoCommandData) command.getData();
            return data.getFileName() + " downloaded.";
        }else if (command.getType() == CommandType.ERROR){
            ErrorCommandData data = (ErrorCommandData) command.getData();
            return data.getErrorMessage();
        }
        return "Unrecognized message";
    }

    private Command readCommand() throws IOException {
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
}