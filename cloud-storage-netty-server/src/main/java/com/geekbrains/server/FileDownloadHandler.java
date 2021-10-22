package com.geekbrains.server;

import com.geekbrains.common.Command;
import com.geekbrains.common.CommandType;
import com.geekbrains.common.commands.FileUploadCommandData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileDownloadHandler extends SimpleChannelInboundHandler<Command> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        if (msg.getType() == CommandType.FILE_UPLOAD) {
            fileUpload(ctx, msg);
        }
    }

    private void fileUpload(ChannelHandlerContext ctx, Command msg) throws IOException {
        FileUploadCommandData data = (FileUploadCommandData) msg.getData();
        String fileName = data.getFileName();
        long fileSize = data.getFileSize();
        Path path = Path.of("./root", fileName);
        if (!Files.exists(path)) {
            Files.createFile(path);
            Files.write(path, data.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            log.debug("received: {}", fileName);
            if (Files.size(path) == fileSize) {
                log.debug("wrote: {}", fileName);
                ctx.writeAndFlush(Command.infoCommand(fileName + " uploaded."));
            } else {
                Files.delete(path);
                log.error("wrong size: {},rec.: {},wr.: {}", fileName, fileSize, Files.size(path));
                ctx.writeAndFlush(Command.infoCommand("File upload error."));
            }
        } else {
            log.debug(fileName + " is already exists.");
            ctx.writeAndFlush(Command.errorCommand(fileName + " is already exists."));
        }
    }
}