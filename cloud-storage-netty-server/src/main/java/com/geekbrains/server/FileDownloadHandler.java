package com.geekbrains.server;

import com.geekbrains.common.Command;
import com.geekbrains.common.commands.AuthCommandData;
import com.geekbrains.common.commands.RegCommandData;
import com.geekbrains.common.commands.UploadFileCommandData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        switch (msg.getType()) {
            case FILE_UPLOAD -> fileUpload(ctx, msg);
            case AUTH -> authentication(ctx, msg);
            case REG -> registrationNewUser(ctx, msg);
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
            updateFileList(ctx);
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
            updateFileList(ctx);
        }
    }

    private void fileUpload(ChannelHandlerContext ctx, Command msg) throws IOException {
        UploadFileCommandData data = (UploadFileCommandData) msg.getData();
        String fileName = data.getFileName();
        long fileSize = data.getFileSize();
        Path path = Server.getRoot().resolve(username).resolve(fileName);
        if (!Files.exists(path)) {
            Files.createFile(path);
            Files.write(path, data.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            log.debug("received: {}", fileName);
            if (Files.size(path) == fileSize) {
                log.debug("wrote: {}", fileName);
                ctx.writeAndFlush(Command.infoCommand(fileName + " uploaded."));
                updateFileList(ctx);
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

    private void updateFileList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(Command.updateFileListCommand(Files.list(pathDir).map(p -> p.getFileName().toString()).collect(Collectors.toList())));
    }
}