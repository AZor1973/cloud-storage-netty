package com.geekbrains.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Server {
    private static final List<String> clients = new ArrayList<>();
    private static final Path root = Path.of("root");
    private static final int SERVER_PORT = 8189;

    public Server() {
        EventLoopGroup auth = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new FileDownloadHandler()
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(SERVER_PORT).sync();
            log.debug("Server started...");
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }
            future.channel().closeFuture().sync(); // block
        } catch (Exception e) {
            log.error("error: ", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void addClient(String user) {
        clients.add(user);
        log.debug(user + " added");
    }

    public static void removeClient(String user) {
        clients.removeIf(client -> client.equals(user));
        log.debug(user + " removed");
    }

    public static boolean isUsernameBusy(String username) {
        for (String client : clients) {
            if (client.equals(username)) {
                log.debug(username + " is present");
                return true;
            }
        }
        return false;
    }

    public static Path getRoot() {
        return root;
    }

    public static void main(String[] args) {
        new Server();
    }
}

