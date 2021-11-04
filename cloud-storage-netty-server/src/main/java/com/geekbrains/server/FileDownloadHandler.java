package com.geekbrains.server;

import com.geekbrains.common.Command;
import com.geekbrains.common.commands.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileDownloadHandler extends SimpleChannelInboundHandler<Command> {
    private static final int BUFFER_SIZE = 8192;
    private DatabaseService ds;
    private String username;
    private Path pathDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ds = new DatabaseService();
        log.debug("Client connected");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Server.removeClient(username);
        ds.closeConnection();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        switch (msg.getType()) {
            case REG -> registrationNewUser(ctx, msg);
            case AUTH -> authentication(ctx, msg);
            case FILE_INFO -> fileUpload(ctx, msg);
            case FILE_REQUEST -> fileDownload(ctx, msg);
            case UP_REQUEST -> toParentDir(ctx);
            case DELETE_REQUEST -> deleteFile(ctx, msg);
            case CREATE_DIR_REQUEST -> createDirectory(ctx, msg);
            case RENAME_REQUEST -> renameFile(ctx, msg);
            case CHANGE_USERNAME -> changeUsername(ctx, msg);
        }
    }

    private void registrationNewUser(ChannelHandlerContext ctx, Command msg) throws IOException {
        RegCommandData data = (RegCommandData) msg.getData();
        String login = data.getLogin();
        String password = data.getPassword();
        String username = data.getUsername();
        if (!ds.addNewUser(username, login, password)) {
            ctx.writeAndFlush(Command.errorCommand("This user is already registered!"));
        } else if (Server.isUsernameBusy(username)) {
            ctx.writeAndFlush(Command.errorCommand("This user is already signed in!"));
        } else {
            this.username = username;
            log.debug(username + " registered");
            Server.addClient(username);
            pathDir = Server.getRoot().resolve(username);
            if (!Files.exists(pathDir)) {
                Files.createDirectory(pathDir);
            }
            ctx.writeAndFlush(Command.authOkCommand(username));
            updateFileList(ctx, pathDir);
        }
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
            ctx.writeAndFlush(Command.authOkCommand(username));
            updateFileList(ctx, pathDir);
        }
    }

    private void fileUpload(ChannelHandlerContext ctx, Command msg) throws IOException {
        FileInfoCommandData data = (FileInfoCommandData) msg.getData();
        String fileName = data.getFileName();
        long fileSize = data.getFileSize();
        Path path = pathDir.resolve(fileName);
        if (data.isStart()) {
            Files.deleteIfExists(path);
        }
        FileOutputStream fos = new FileOutputStream(path.toString(), true);
        fos.write(data.getBytes(), 0, data.getEndPos());
        if (Files.size(path) == fileSize) {
            ctx.writeAndFlush(Command.infoCommand(fileName + " uploaded."));
            updateFileList(ctx, pathDir);
            log.debug("wrote: {}", fileName);
        }
        fos.close();
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
                updateFileList(ctx, pathDir);
            } else {
                long fileSize = Files.size(path);
                FileInputStream fis = new FileInputStream(path.toString());
                byte[] buffer = new byte[BUFFER_SIZE];
                int readBytes;
                boolean start = true;
                while ((readBytes = fis.read(buffer)) != -1) {
                    ctx.writeAndFlush(Command.fileInfoCommand(fileNameToDownload, fileSize, buffer, start, readBytes));
                    start = false;
                }
                fis.close();
            }
        }
    }

    private void toParentDir(ChannelHandlerContext ctx) throws IOException {
        if (!pathDir.getParent().equals(Server.getRoot())) {
            pathDir = pathDir.getParent();
            updateFileList(ctx, pathDir);
        }
    }

    private void deleteFile(ChannelHandlerContext ctx, Command msg) throws IOException {
        DeleteCommandData data = (DeleteCommandData) msg.getData();
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

    private void createDirectory(ChannelHandlerContext ctx, Command msg) throws IOException {
        CreateDirCommandData data = (CreateDirCommandData) msg.getData();
        String dirName = data.getName();
        Path path = pathDir.resolve(dirName);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        updateFileList(ctx, pathDir);
        log.debug(dirName + " created");
        ctx.writeAndFlush(Command.infoCommand(dirName + " created"));
    }

    private void renameFile(ChannelHandlerContext ctx, Command msg) throws IOException {
        RenameCommandData data = (RenameCommandData) msg.getData();
        String file = data.getFile();
        String newName = data.getNewName();
        Path path = pathDir.resolve(file);
        Path newPath = pathDir.resolve(newName);
        if (!Files.exists(newPath)) {
            if (Files.isDirectory(path)) {
                renameDir(path, newPath);
            } else {
                Files.move(path, path.resolveSibling(newName));
            }
            ctx.writeAndFlush(Command.infoCommand(file + " renamed to " + newName));
            log.debug(file + " renamed to " + newName);
            updateFileList(ctx, pathDir);
        } else {
            ctx.writeAndFlush(Command.errorCommand(newName + " is already exists!"));
        }
    }

    private void renameDir(Path path, Path newPath) throws IOException {
        Files.createDirectory(newPath);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = newPath.resolve(path.relativize(dir));
                try {
                    Files.createDirectory(targetDir);
                } catch (FileAlreadyExistsException e) {
                    if (!Files.isDirectory(targetDir)) throw e;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.move(file, newPath.resolve(path.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
        FileUtils.forceDelete(new File(String.valueOf(path)));
    }

    private void changeUsername(ChannelHandlerContext ctx, Command msg) throws IOException {
        ChangeUsernameCommandData data = (ChangeUsernameCommandData) msg.getData();
        String newName = data.getNewName();
        if (Server.isUsernameBusy(newName)) {
            ctx.writeAndFlush(Command.errorCommand("This user is already signed in!"));
        } else if (ds.changeUsername(newName, username)) {
            Path newPath = Server.getRoot().resolve(newName);
            renameDir(pathDir, newPath);
            pathDir = newPath;
            Server.removeClient(username);
            Server.addClient(newName);
            log.debug(username + " changed nick to " + newName);
            username = newName;
            ctx.writeAndFlush(Command.authOkCommand(username));
            updateFileList(ctx, pathDir);
        } else {
            ctx.writeAndFlush(Command.errorCommand("This user is already registered!"));
        }
    }

    private void updateFileList(ChannelHandlerContext ctx, Path path) throws IOException {
        List<String> fileList;
        fileList = Files.list(path)
                .map(this::toStringWithDir)
                .collect(Collectors.toList());
        fileList.add(0, pathDir.toString().substring(5));
        ctx.writeAndFlush(Command.updateFileListCommand(fileList));
    }
    // Метка для папок - [DIR]

    public String toStringWithDir(Path path) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString() + " [DIR]";
        }
        return path.getFileName().toString();
    }
}