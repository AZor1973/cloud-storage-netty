package com.geekbrains.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class MainController implements Initializable {
    @FXML
    public TextField input;
    @FXML
    public TextField output;
    @FXML
    public Button uploadButton;
    @FXML
    public Button downloadButton;
    @FXML
    public ListView<String> serverListView;
    @FXML
    public ListView<String> clientListView;
    private Network network;
    private List<String> fileListClient;
    private String selectedFileName;
    private long selectedFileSize;
    private Path selectedFilePath;
    private FileInputStream fis;
    private byte[] selectedFileBytes;
    private Path parentPath;
    private Path currentPath;
    private String fileNameToDownload;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentPath = Path.of(System.getProperty("user.dir"));
        updateClientListView(currentPath);
        network = Network.getInstance();
        network.connect();
        Thread readThread = new Thread(() -> {
            try {
                while (true) {
                    String message = network.readMessage();
                    if (!message.isEmpty())
                        Platform.runLater(() -> putMessage(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    public void updateClientListView(Path path) {
        clientListView.getItems().clear();
        fileListClient = new ArrayList<>();
        try {
            fileListClient.addAll(Files.list(path).map(p -> p.getFileName().toString()).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientListView.getItems().addAll(fileListClient);
    }

    public void updateServerListView(List<String> files) {
        serverListView.getItems().clear();
        serverListView.getItems().addAll(files);
    }

    public void selectFileToUploadMouse(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            getFile();
        }
    }

    public void selectFileToUploadKey(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFile();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            moveToParent();
        }
    }

    private void moveToParent() {
        if (selectedFilePath != null) {
            if (Files.isDirectory(selectedFilePath)) {
                parentPath = selectedFilePath.getParent();
            } else {
                parentPath = selectedFilePath.getParent().getParent();
            }
            selectedFilePath = parentPath;
        } else {
            parentPath = currentPath.getParent();
        }
        if (parentPath != null) {
            updateClientListView(parentPath);
            currentPath = parentPath;
        }
    }

    private void getFile() throws IOException {
        if (fis != null) {
            fis = null;
//            fis.close();   // does not work
        }
        selectedFileName = clientListView.getSelectionModel().getSelectedItem();
        selectedFilePath = Path.of(String.valueOf(currentPath), selectedFileName);
        if (Files.isDirectory(selectedFilePath)) {
            currentPath = selectedFilePath;
            updateClientListView(selectedFilePath);
        } else {
            selectedFileSize = Files.size(selectedFilePath);
            fis = new FileInputStream(String.valueOf(selectedFilePath));
            selectedFileBytes = new byte[(int) selectedFileSize];
            fis.read(selectedFileBytes);
            input.setText(selectedFileName);
            input.requestFocus();
        }
    }

    private void putMessage(String message) {
        input.clear();
        input.setText(message);
    }

    public void upload() throws IOException {
        if (fis != null) {
            network.sendFile(selectedFileName, selectedFileSize, selectedFileBytes);
            input.clear();
//            fis.close();  // does not work
            fis = null;
            selectedFileName = null;
            selectedFileSize = 0;
            clientListView.requestFocus();
        }
    }

    public void selectFileToDownloadMouse(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            fileNameToDownload = serverListView.getSelectionModel().getSelectedItem();
            network.sendFileRequest(fileNameToDownload);
        }
    }

    public void selectFileToDownloadKey(KeyEvent keyEvent) {

    }

    public void keyHandleInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP) {
            if (fis != null) {
                input.clear();
//            fis.close(); // does not work
                fis = null;
                selectedFileName = null;
                selectedFileSize = 0;
                clientListView.requestFocus();
            }
        } else if (keyEvent.getCode() == KeyCode.LEFT) {
            output.requestFocus();
        }
    }

    public void keyHandleOutput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP) {
            if (fis != null) {
                output.clear();
                serverListView.requestFocus();
            }
        } else if (keyEvent.getCode() == KeyCode.RIGHT) {
            input.requestFocus();
        }
    }

    public void getServerParent() throws IOException {
        network.sendUpRequest();
    }

    public void getClientParent() {
        moveToParent();
    }
}

