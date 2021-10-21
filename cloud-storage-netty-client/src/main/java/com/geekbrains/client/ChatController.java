package com.geekbrains.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class ChatController implements Initializable {

    public ListView<String> listView;
    public TextField input;
    private Network network;
    private List<Path> fileList;
    private String fileName;
    private long fileSize;
    private Path path = null;
    private FileInputStream fis;
    private byte[] bytes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateFileList(Paths.get("./files"));
        network = Network.getInstance();
        network.connect();
        Thread readThread = new Thread(() -> {
            try {
                while (true) {
                    String message = network.readMessage();
                    Platform.runLater(() -> putMessage(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    @FXML
    public void getMessage() throws IOException {
        network.sendFileInfo(fileName, fileSize, bytes);
        input.clear();
        fis.close();
    }

    public void updateFileList(Path path) {
        listView.getItems().clear();
        fileList = new ArrayList<>();
        try {
            fileList.addAll(Files.list(path).map(Path::toAbsolutePath).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Path path1 : fileList) {
            listView.getItems().add(String.valueOf(path1.getFileName()));
        }
    }

    public void getFile(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            fileName = listView.getSelectionModel().getSelectedItem();
            for (Path value : fileList) {
                if (value.getFileName().equals(Paths.get(fileName))) {
                    path = value;
                }
            }
            assert path != null;
            if (Files.isDirectory(path)) {
                updateFileList(path);
            } else {
                fileSize = Files.size(path);
                fis = new FileInputStream(String.valueOf(path));
                bytes = new byte[(int)fileSize];
                fis.read(bytes);
                input.setText(fileName);
                input.requestFocus();
            }
        } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            fileName = listView.getSelectionModel().getSelectedItem();
            Path upperPath = Paths.get(fileName).getParent();
            for (Path value : fileList) {
                if (value.getFileName().equals(Paths.get(fileName))) {
                    upperPath = value.getParent().getParent();
                }
            }
            if (upperPath != null) {
                updateFileList(upperPath);
            }
        }
    }

    private void putMessage(String message){
        input.clear();
        input.setText(message);
    }
}

