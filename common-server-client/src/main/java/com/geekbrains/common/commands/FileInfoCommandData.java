package com.geekbrains.common.commands;

import lombok.Getter;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileInfoCommandData implements Serializable {
    @Serial
    private static final long serialVersionUID = 8128138402141926015L;
    public static final String DIRECTORY = "Directory";
    private Path path;
    private final String name;
    private final long size;
    private String type;
    private byte[] bytes;
    private boolean isStart;
    private int endPos;

    public FileInfoCommandData(String name, long size, byte[] bytes, boolean isStart, int endPos) {
        this.name = name;
        this.size = size;
        this.bytes = bytes;
        this.isStart = isStart;
        this.endPos = endPos;
    }

    public FileInfoCommandData(Path path) throws IOException {
        this.path = path;
        name = path.getFileName().toString();
        type = getFileType(path);
        size = Files.size(path);
    }

    public FileInfoCommandData(Path path, long size) {
        name = path.getFileName().toString();
        this.size = size;
        type = getFileType(path);
    }

    private String getFileType(Path path) {
        if (Files.isDirectory(path)) {
            return DIRECTORY;
        } else if (name.contains(".") && !name.startsWith(".")) {
            return name.substring(name.lastIndexOf(".") + 1) + " file";
        }
        return "file";
    }

    @Override
    public String toString() {
        if (type.equals(DIRECTORY)) {
            return String.format("%s   %s", name, type);
        } else if (name.contains(".") && !name.startsWith(".")) {
            return String.format("%s   %s   %s", name.substring(0, name.lastIndexOf(".")), type, getSizeToString());
        }
        return String.format("%s   %s   %s", name, type, getSize());
    }

    private String getSizeToString() {
        if (size < 1024) {
            return String.format("%d bytes", size);
        } else if (size < 1048576) {
            return String.format("%.2f KB", (double) size / 1024);
        } else if (size < 1073741824) {
            return String.format("%.2f MB", (double) size / 1048576);
        } else {
            return String.format("%.2f GB", (double) size / 1073741824);
        }
    }
}
