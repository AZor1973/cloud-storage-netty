package com.geekbrains.server;

import com.geekbrains.common.Command;
import com.geekbrains.common.commands.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

@Slf4j
public class FileDownloadHandler extends SimpleChannelInboundHandler<Command> {
    private final DatabaseService ds = new DatabaseService();
    private String username;
    private Path pathDir;
    private FileInputStream fis;
    private byte[] fileBytes;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        switch (msg.getType()) {
            case FILE_UPLOAD -> fileUpload(ctx, msg);
            case AUTH -> authentication(ctx, msg);
            case REG -> registrationNewUser(ctx, msg);
            case FILE_REQUEST -> fileDownload(ctx, msg);
            case UP_REQUEST -> toFolderAbove(ctx);
            case DELETE_REQUEST -> deleteFile(ctx, msg);
            case CREATE_DIR_REQUEST -> createDirectory(ctx, msg);
        }
    }

    private void createDirectory(ChannelHandlerContext ctx, Command msg) {

    }

    private void deleteFile(ChannelHandlerContext ctx, Command msg) throws IOException {
        DeleteRequestCommandData data = (DeleteRequestCommandData) msg.getData();
        String fileNameToDelete = data.getFileName();
        Path path = pathDir.resolve(fileNameToDelete);
        if (!Files.exists(path)) {
            ctx.writeAndFlush(Command.errorCommand(fileNameToDelete + " does not exist"));
        } else {
            if (Files.isDirectory(path)) {
                FileUtils.forceDelete(new File(String.valueOf(path)));
            } else {
                Files.delete(path);
                ctx.writeAndFlush(Command.infoCommand(fileNameToDelete + " deleted."));
                log.debug(fileNameToDelete + " deleted.");
            }
        }
        updateFileList(ctx, pathDir);
    }

    private void toFolderAbove(ChannelHandlerContext ctx) throws IOException {
        if (!pathDir.getParent().equals(Server.getRoot())) {
            pathDir = pathDir.getParent();
            updateFileList(ctx, pathDir);
        }
    }

    private void fileDownload(ChannelHandlerContext ctx, Command msg) throws IOException {
        FileRequestCommandData data = (FileRequestCommandData) msg.getData();
        String fileNameToDownload = data.getFileName();
        Path path = pathDir.resolve(fileNameToDownload);
        if (!Files.exists(path)) {
            ctx.writeAndFlush(Command.errorCommand(fileNameToDownload + " does not exist"));
        } else {
            if (Files.isDirectory(path)) {
                pathDir = path;
                updateFileList(ctx, path);
            } else {
                long fileSize = Files.size(path);
                fis = new FileInputStream(String.valueOf(path));
                fileBytes = new byte[(int) fileSize];
                fis.read(fileBytes);
                ctx.writeAndFlush(Command.uploadFileCommand(fileNameToDownload, fileSize, fileBytes));
            }
        }
    }

    private void registrationNewUser(ChannelHandlerContext ctx, Command msg) throws IOException {
        RegCommandData data = (RegCommandData) msg.getData();
        String login = data.getLogin();
        String password = data.getPassword();
        String username = data.getUsername();
        int result = ds.addNewUser(username, login, password);
        System.out.println(result);
        if (result == 0) {
            ctx.writeAndFlush(Command.errorCommand("This user is already registered!"));
        } else if (Server.isUsernameBusy(username)) {
            ctx.writeAndFlush(Command.errorCommand("This user is already signed in!"));
        } else {
            this.username = username;
            Server.addClient(username);
            pathDir = Server.getRoot().resolve(username);
            if (!Files.exists(pathDir)) {
                Files.createDirectory(pathDir);
            }
            ctx.writeAndFlush(Command.authOkCommand(username));
            updateFileList(ctx, pathDir);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Server.removeClient(username);
    }

    private void authentication(ChannelHandlerContext ctx, Command msg) throws IOException {
        AuthCommandData data = (AuthCommandData) msg.getData();
        String login = data.getLogin();
        String password = data.getPassword();
        String username = ds.getUsernameByLoginAndPassword(login, password);
        if (username == null) {
            ctx.writeAndFlush(Command.errorCommand("Incorrect login or password!"));
        } else if (Server.isUsernameBusy(username)) {
            ctx.writeAndFlush(Command.errorCommand("This user is already signed in!"));
        } else {
            this.username = username;
            Server.addClient(username);
            pathDir = Server.getRoot().resolve(username);
            if (!Files.exists(pathDir)) {
                Files.createDirectory(pathDir);
            }
            ctx.writeAndFlush(Command.authOkCommand(username));
            updateFileList(ctx, pathDir);
        }
    }

    private void fileUpload(ChannelHandlerContext ctx, Command msg) throws IOException {
        UploadFileCommandData data = (UploadFileCommandData) msg.getData();
        String fileName = data.getFileName();
        long fileSize = data.getFileSize();
        Path path = pathDir.resolve(fileName);
        if (!Files.exists(path)) {
            Files.createFile(path);
            Files.write(path, data.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            log.debug("received: {}", fileName);
            if (Files.size(path) == fileSize) {
                log.debug("wrote: {}", fileName);
                ctx.writeAndFlush(Command.infoCommand(fileName + " uploaded."));
                updateFileList(ctx, pathDir);
            } else {
                Files.delete(path);
                log.error("wrong size: {},rec.: {},wr.: {}", fileName, fileSize, Files.size(path));
                ctx.writeAndFlush(Command.errorCommand("File upload error."));
            }
        } else {
            log.debug(fileName + " is already exists.");
            ctx.writeAndFlush(Command.errorCommand(fileName + " is already exists."));
        }
    }

    private void updateFileList(ChannelHandlerContext ctx, Path path) throws IOException {
        ctx.writeAndFlush(Command.updateFileListCommand(Files.list(path)
                .map(this::toStringWithDir)
                .collect(Collectors.toList())));
    }

    public String toStringWithDir(Path path) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString() + " [DIR]";
        }
        return path.getFileName().toString();
    }
}