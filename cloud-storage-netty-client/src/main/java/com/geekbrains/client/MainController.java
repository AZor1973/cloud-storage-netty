package com.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    private ContextMenu clientContextMenu;
    private ContextMenu serverContextMenu;
    private MenuItem menuItem1;
    private MenuItem menuItem2;
    private MenuItem menuItem3;
    private MenuItem menuItem4;
    private MenuItem menuItem5;
    private MenuItem menuItem6;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientContextMenu = new ContextMenu();
        serverContextMenu = new ContextMenu();
        menuItem1 = new MenuItem("Go to parent directory");
        menuItem2 = new MenuItem("Delete");
        menuItem3 = new MenuItem("Create new directory");
        menuItem4 = new MenuItem("Go to parent directory");
        menuItem5 = new MenuItem("Delete");
        menuItem6 = new MenuItem("Create new directory");
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
        clientListView.setContextMenu(clientContextMenu);
        serverListView.setContextMenu(serverContextMenu);
        clientContextMenu.getItems().add(menuItem1);
        clientContextMenu.getItems().add(menuItem2);
        clientContextMenu.getItems().add(menuItem3);
        serverContextMenu.getItems().add(menuItem4);
        serverContextMenu.getItems().add(menuItem5);
        serverContextMenu.getItems().add(menuItem6);
        menuItem1.setOnAction(event -> moveToParent());
        menuItem4.setOnAction(event -> getServerParent());
    }

    public void updateClientListViewStatic() {
        updateClientListView(currentPath);
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
            getFileToUpload();
        }
    }

    public void selectFileToUploadKey(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFileToUpload();
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

    private void getFileToUpload() throws IOException {
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
            getFileToDownload();
        }
    }

    private void getFileToDownload() {
        fileNameToDownload = serverListView.getSelectionModel().getSelectedItem();
        output.setText(fileNameToDownload);
        output.requestFocus();
    }

    public void selectFileToDownloadKey(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFileToDownload();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            getServerParent();
        }
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
            output.clear();
            serverListView.requestFocus();
            fileNameToDownload = null;
        } else if (keyEvent.getCode() == KeyCode.RIGHT) {
            input.requestFocus();
        }
    }

    public void getServerParent(){
        try {
            network.sendUpRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getClientParent() {
        moveToParent();
    }

    public void download() throws IOException {
        network.sendFileRequest(fileNameToDownload);
        output.clear();
        fileNameToDownload = null;
        serverListView.requestFocus();
    }
}

