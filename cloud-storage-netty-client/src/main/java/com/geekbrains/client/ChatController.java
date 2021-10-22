package com.geekbrains.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML
    public TextField input;
    @FXML
    public TextField output;
    @FXML
    public Button uploadButton;
    @FXML
    public Button downloadButton;
    @FXML
    public ListView<String> listViewServer;
    @FXML
    public ListView<String> listViewClient;
    private Network network;
    private List<Path> fileListClient;
    private String uploadFileName;
    private long uploadFileSize;
    private Path uploadFilepath = null;
    private FileInputStream fis;
    private byte[] uploadFileBytes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateFileList(Path.of(System.getProperty("user.dir")));
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

    public void updateFileList(Path path) {
        listViewClient.getItems().clear();
        fileListClient = new ArrayList<>();
        try {
            fileListClient.addAll(Files.list(path).map(Path::toAbsolutePath).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Path path1 : fileListClient) {
            listViewClient.getItems().add(String.valueOf(path1.getFileName()));
        }
    }

    public void selectFileToUploadMouse(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            getFile();
        } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            moveToParent();
        }
    }

    public void selectFileToUploadKey(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER){
            getFile();
        }else  if (keyEvent.getCode() == KeyCode.ESCAPE){
            moveToParent();
        }
    }

    private void moveToParent() {
        uploadFileName = listViewClient.getSelectionModel().getSelectedItem();
        Path upperPath = Paths.get(uploadFileName).getParent();
        for (Path value : fileListClient) {
            if (value.getFileName().equals(Paths.get(uploadFileName))) {
                upperPath = value.getParent().getParent();
            }
        }
        if (upperPath != null) {
            updateFileList(upperPath);
        }
    }

    private void getFile() throws IOException {
        if (fis != null){
            fis = null;
//            fis.close();   // don't work
        }
        uploadFileName = listViewClient.getSelectionModel().getSelectedItem();
        for (Path value : fileListClient) {
            if (value.getFileName().equals(Paths.get(uploadFileName))) {
                uploadFilepath = value;
            }
        }
        assert uploadFilepath != null;
        if (Files.isDirectory(uploadFilepath)) {
            updateFileList(uploadFilepath);
        } else {
            uploadFileSize = Files.size(uploadFilepath);
            fis = new FileInputStream(String.valueOf(uploadFilepath));
            uploadFileBytes = new byte[(int) uploadFileSize];
            fis.read(uploadFileBytes);
            input.setText(uploadFileName);
            input.requestFocus();
        }
    }

    private void putMessage(String message){
        input.clear();
        input.setText(message);
    }

    public void upload() throws IOException {
        if (fis != null){
            network.sendFile(uploadFileName, uploadFileSize, uploadFileBytes);
            input.clear();
//            fis.close();  // don't work
            fis = null;
            uploadFileName = null;
            uploadFileSize = 0;
            listViewClient.requestFocus();
        }
    }

    public void selectFileToDownloadMouse(MouseEvent mouseEvent) {

    }

    public void selectFileToDownloadKey(KeyEvent keyEvent) {

    }

    public void keyHandleInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP){
            if (fis != null){
                input.clear();
//            fis.close(); // don't work
                fis = null;
                uploadFileName = null;
                uploadFileSize = 0;
                listViewClient.requestFocus();
            }
        }else if (keyEvent.getCode() == KeyCode.LEFT){
            output.requestFocus();
        }
    }

    public void keyHandleOutput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP){
            if (fis != null){
                output.clear();
                listViewServer.requestFocus();
            }
        }else if (keyEvent.getCode() == KeyCode.RIGHT){
            input.requestFocus();
        }
    }
}

