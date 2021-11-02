package com.geekbrains.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
    @FXML
    public Label currentPathLabelClient;
    @FXML
    public Label currentPathLabelServer;
    @FXML
    public Label connectLabel;
    @FXML
    public ComboBox<String> disksBox;
    private static final int BUFFER_SIZE = 8192;
    private Network network;
    private String selectedFileName;
    private Path selectedFilePath;
    private Path currentPath;
    private String fileNameToDownload;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();
        network.connect();
        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);
        currentPath = Path.of(disksBox.getSelectionModel().getSelectedItem());
        updateClientListView(currentPath);
        ContextMenu clientContextMenu = new ContextMenu();
        ContextMenu serverContextMenu = new ContextMenu();
        clientListView.setContextMenu(clientContextMenu);
        serverListView.setContextMenu(serverContextMenu);
        MenuItem parentDirClientItem = new MenuItem("Go to parent directory");
        MenuItem deleteClientItem = new MenuItem("Delete");
        MenuItem newDirClientItem = new MenuItem("Create new directory");
        MenuItem renameClientItem = new MenuItem("Rename");
        MenuItem parentDirServerItem = new MenuItem("Go to parent directory");
        MenuItem deleteServerItem = new MenuItem("Delete");
        MenuItem newDirServerItem = new MenuItem("Create new directory");
        MenuItem renameServerItem = new MenuItem("Rename");
        clientContextMenu.getItems().add(parentDirClientItem);
        clientContextMenu.getItems().add(deleteClientItem);
        clientContextMenu.getItems().add(newDirClientItem);
        clientContextMenu.getItems().add(renameClientItem);
        serverContextMenu.getItems().add(parentDirServerItem);
        serverContextMenu.getItems().add(deleteServerItem);
        serverContextMenu.getItems().add(newDirServerItem);
        serverContextMenu.getItems().add(renameServerItem);
        parentDirClientItem.setOnAction(event -> moveToParent());
        deleteClientItem.setOnAction(event -> deleteFile());
        newDirClientItem.setOnAction(event -> createDir());
        renameClientItem.setOnAction(event -> renameFile());
        parentDirServerItem.setOnAction(event -> getServerParent());
        deleteServerItem.setOnAction(event -> deleteRequest());
        newDirServerItem.setOnAction(event -> createDirRequest());
        renameServerItem.setOnAction(event -> renameRequest());
    }

    public void selectDiskAction() {
        currentPath = Path.of(disksBox.getSelectionModel().getSelectedItem());
        updateClientListView(currentPath);
    }

    private void moveToParent() {
        Path parentPath = currentPath.getParent();
        if (parentPath != null) {
            updateClientListView(parentPath);
            currentPath = parentPath;
        }
    }

    private void deleteFile() {
        String fileName = clientListView.getSelectionModel().getSelectedItem();
        if (fileName == null) {
            return;
        }
        if (fileName.endsWith("[DIR]")) {
            fileName = fileName.substring(0, fileName.length() - 6);
        }
        if (deleteFileAlert(fileName)) return;
        Path path = currentPath.resolve(fileName);
        try {
            if (Files.isDirectory(path)) {
                if (deleteDirAlert(fileName)) return;
                FileUtils.forceDelete(new File(String.valueOf(path)));
            } else {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateClientListView(currentPath);
        log.debug(fileName + " deleted.");
        showAlert(fileName + " deleted.", Alert.AlertType.INFORMATION);
    }

    private boolean deleteDirAlert(String str) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting a directory");
        alert.setHeaderText("Deleting a directory");
        alert.setContentText("Are you sure?\n" + str + " will be deleted with all files inside!");
        alert.showAndWait();
        return alert.getResult() == ButtonType.CANCEL;
    }

    private boolean deleteFileAlert(String str) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deletion");
        alert.setHeaderText("Deletion");
        alert.setContentText("Are you sure?\n" + str + " will be deleted!");
        alert.showAndWait();
        return alert.getResult() == ButtonType.CANCEL;
    }

    private void createDir() {
        TextInputDialog editDialog = new TextInputDialog("Enter directory name");
        editDialog.setTitle("Create a directory");
        editDialog.setHeaderText("Enter directory name");
        editDialog.setContentText("Directory name:");
        Optional<String> optName = editDialog.showAndWait();
        if (optName.isPresent()) {
            String name = optName.get();
            Path path = currentPath.resolve(name);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectory(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            updateClientListView(currentPath);
            log.debug(name + " created");
            showAlert(name + " created", Alert.AlertType.INFORMATION);
        }
    }

    private void renameFile() {
        String file;
        if ((file = clientListView.getSelectionModel().getSelectedItem()) != null) {
            Path path = Path.of(currentPath.toString(), file);
            TextInputDialog editDialog = new TextInputDialog();
            editDialog.setTitle("Rename file");
            editDialog.setHeaderText(file + " will be renamed!");
            editDialog.setContentText("New name:");
            Optional<String> optName = editDialog.showAndWait();
            if (optName.isPresent()) {
                String newName = optName.get();
                Path newPath = currentPath.resolve(newName);
                if (!Files.exists(newPath)) {
                    try {
                        Files.move(path, path.resolveSibling(newName));
                        updateClientListView(currentPath);
                        log.debug(file + " renamed to " + newName);
                        showAlert(file + " renamed to " + newName, Alert.AlertType.INFORMATION);
                    } catch (IOException e) {
                        showAlert(file + " is not non-empty directory!", Alert.AlertType.ERROR);
                        e.printStackTrace();
                    }
                } else {
                    showAlert(newName + " is already exists!", Alert.AlertType.ERROR);
                }
            }
        }
    }

    public void getServerParent() {
        try {
            network.sendUpRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteRequest() {
        String str = serverListView.getSelectionModel().getSelectedItem();
        if (str == null) {
            return;
        }
        if (deleteFileAlert(str)) return;
        if (!str.isEmpty()) {
            if (str.endsWith("[DIR]")) {
                str = str.substring(0, str.length() - 6);
                if (deleteDirAlert(str)) return;
            }
            try {
                network.sendDeleteRequest(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDirRequest() {
        TextInputDialog editDialog = new TextInputDialog("Enter directory name");
        editDialog.setTitle("Create a directory");
        editDialog.setHeaderText("Enter directory name");
        editDialog.setContentText("Directory name:");
        Optional<String> optName = editDialog.showAndWait();
        if (optName.isPresent()) {
            String name = optName.get();
            try {
                network.sendCreateDirRequest(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void renameRequest() {
        String file;
        if ((file = serverListView.getSelectionModel().getSelectedItem()) != null) {
            TextInputDialog editDialog = new TextInputDialog();
            editDialog.setTitle("Rename file");
            editDialog.setHeaderText(file + " will be renamed!");
            editDialog.setContentText("New name:");
            Optional<String> optName = editDialog.showAndWait();
            if (optName.isPresent()) {
                String newName = optName.get();
                network.sendRenameRequest(file, newName);
            }
        }
    }

    public void updateClientListView(Path path) {
        currentPathLabelClient.setText(currentPath.toString());
        clientListView.getItems().clear();
        List<String> fileListClient = new ArrayList<>();
        try {
            fileListClient.addAll(Files.list(path).map(this::toStringWithDir).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientListView.getItems().addAll(fileListClient);
    }

    private String toStringWithDir(Path path) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString() + " [DIR]";
        }
        return path.getFileName().toString();
    }

    public void updateServerListView(List<String> files) {
        currentPathLabelServer.setText(files.get(0));
        files.remove(0);
        serverListView.getItems().clear();
        serverListView.getItems().addAll(files);
    }

    public void selectFileToUploadMouse(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            getFileToUpload();
        }
    }

    public void selectFileToUploadKey(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFileToUpload();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            moveToParent();
        }
    }

    private void getFileToUpload() {
        String fileName = clientListView.getSelectionModel().getSelectedItem();
        if (fileName == null)
            return;
        if (fileName.endsWith("[DIR]")) {
            fileName = fileName.substring(0, fileName.length() - 6);
        }
        Path path = currentPath.resolve(fileName);
        if (Files.isDirectory(path)) {
            currentPath = path;
            updateClientListView(currentPath);
        } else {
            selectedFileName = fileName;
            selectedFilePath = path;
            input.setText(selectedFileName);
            input.requestFocus();
        }
    }

    public void upload() throws IOException {
        if (!input.getText().isBlank()) {
            long selectedFileSize = Files.size(selectedFilePath);
            FileInputStream fis = new FileInputStream(selectedFilePath.toString());
            byte[] buffer = new byte[BUFFER_SIZE];
            int readBytes;
            boolean start = true;
            while ((readBytes = fis.read(buffer)) != -1) {
                network.sendFile(selectedFileName, selectedFileSize, buffer, start, readBytes);
                start = false;
            }
            input.clear();
            log.debug(selectedFileName + " uploaded");
            fis.close();
            selectedFilePath = null;
            selectedFileName = null;
            clientListView.requestFocus();
        }
    }

    public void selectFileToDownloadMouse(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            getFileToDownload();
        }
    }

    public void selectFileToDownloadKey(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFileToDownload();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            getServerParent();
        } else if (keyEvent.getCode() == KeyCode.DELETE) {
            deleteRequest();
        }
    }

    private void getFileToDownload() {
        String fileName = serverListView.getSelectionModel().getSelectedItem();
        if (fileName == null)
            return;
        if (fileName.endsWith("[DIR]")) {
            fileName = fileName.substring(0, fileName.length() - 6);
            network.sendFileRequest(fileName);
        } else {
            fileNameToDownload = fileName;
            output.setText(fileNameToDownload);
            output.requestFocus();
        }
    }

    @FXML
    private void downloadRequest() {
        if (!output.getText().isBlank()) {
            network.sendFileRequest(fileNameToDownload);
            output.clear();
        }
        fileNameToDownload = null;
        serverListView.requestFocus();
    }

    public void download(String fileName, long fileSize, byte[] bytes, boolean isStart, int endPos) throws IOException {
        Path path = currentPath.resolve(fileName);
        if (isStart) {
            Files.deleteIfExists(path);
        }
        FileOutputStream fos = new FileOutputStream(path.toString(), true);
        fos.write(bytes, 0, endPos);
        if (Files.size(path) == fileSize) {
            updateClientListView(currentPath);
            log.debug(fileName + " downloaded.");
            showAlert(fileName + " downloaded.", Alert.AlertType.INFORMATION);
        }
        fos.close();
    }

    private boolean showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    public void keyHandleInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP) {
            input.clear();
            selectedFileName = null;
            selectedFilePath = null;
            clientListView.requestFocus();
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

    public void connectLost() {
        connectLabel.setText("SERVER: OFF");
        if (showAlert("Connection lost. Reconnect?", Alert.AlertType.CONFIRMATION)) {
            network.getThread().interrupt();
            network.connect();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            network.reAuth();
        }
    }
}

