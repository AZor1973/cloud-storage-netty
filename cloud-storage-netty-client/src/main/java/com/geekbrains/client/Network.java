package com.geekbrains.client;

import com.geekbrains.common.Command;
import com.geekbrains.common.CommandType;
import com.geekbrains.common.commands.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.List;

@Slf4j
public class Network {

    private static final int SERVER_PORT = 8189;
    private static final String SERVER_HOST = "localhost";
    private static Network INSTANCE;
    private final String host;
    private final int port;
    private SocketChannel socketChannel;

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
       Thread thread = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                socketChannel = ch;
                                ch.pipeline().addLast(
                                        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new SimpleChannelInboundHandler<Command>() {
                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, Command msg) {
                                                readMessage(msg);
                                            }
                                        });
                            }
                        });
                ChannelFuture future = bootstrap.connect(host, port).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Network error", Alert.AlertType.ERROR);
            }finally {
                workerGroup.shutdownGracefully();
            }
        });
       thread.setDaemon(true);
       thread.start();
    }

    public void readMessage(Command command) {
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
            Platform.runLater(() -> App.INSTANCE.switchToMainWindow(message));

        } else if (command.getType() == CommandType.UPDATE_FILE_LIST) {
            UpdateFileListCommandData data = (UpdateFileListCommandData) command.getData();
            List<String> files = data.getFiles();
            Platform.runLater(() -> App.INSTANCE.getMainController().updateServerListView(files));
        } else if (command.getType() == CommandType.FILE_INFO) {
            FileInfoCommandData data = (FileInfoCommandData) command.getData();
            String fileName = data.getFileName();
            long fileSize = data.getFileSize();
            Platform.runLater(() -> {
                try {
                    App.INSTANCE.getMainController().download(fileName, fileSize, data.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void sendCommand(Command command) {
        socketChannel.writeAndFlush(command);
    }

    public void sendFile(String fileName, long fileSize, byte[] bytes) {
        sendCommand(Command.fileInfoCommand(fileName, fileSize, bytes));
    }

    public void sendAuthMessage(String login, String password) {
        sendCommand(Command.authCommand(login, password));
    }

    public void sendRegMessage(String username, String login, String password) {
        sendCommand(Command.regCommand(username, login, password));
    }

    public void sendFileRequest(String fileNameToDownload) {
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

    public void close(){
        socketChannel.close();
    }
}