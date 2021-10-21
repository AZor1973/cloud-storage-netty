package com.geekbrains.server;

import com.geekbrains.common.Command;
import com.geekbrains.common.CommandType;
import com.geekbrains.common.commands.FileInfoCommandData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileInfoMessageHandler extends SimpleChannelInboundHandler<Command> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        if (msg.getType() == CommandType.FILE_INFO) {
            FileInfoCommandData data = (FileInfoCommandData) msg.getData();
            String fileName = data.getFileName();
            long fileSize = data.getFileSize();
            Path path = Path.of("./root", fileName);
            if (!Files.exists(path)) {
                Files.createFile(path);
                log.debug("received: {}", fileName);
                ctx.writeAndFlush(msg);
            } else {
                log.debug(fileName + " is already exists.");
                ctx.writeAndFlush(Command.errorCommand(fileName + " is already exists."));
            }
            Files.write(path, data.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        }
    }
}