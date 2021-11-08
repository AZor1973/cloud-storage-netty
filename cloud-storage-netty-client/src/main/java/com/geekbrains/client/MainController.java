package com.geekbrains.client;

import com.geekbrains.common.commands.FileInfoCommandData;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
    public ListView<FileInfoCommandData> serverListView;
    @FXML
    public ListView<FileInfoCommandData> clientListView;
    @FXML
    public Label currentPathLabelClient;
    @FXML
    public Label currentPathLabelServer;
    @FXML
    public Label connectLabel;
    @FXML
    public ComboBox<String> disksBox;
    private static final int BUFFER_SIZE = 8192;
    private static final String WARN_RESOURCE = "com/geekbrains/client/warn.css";
    private static final String INFO_RESOURCE = "com/geekbrains/client/info.css";
    private Network network;
    private Path currentPath;
    private int copyNumber = 0;  // Если файл существует - делаем копию, а не удаляем (с учётом загрузки частями).

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
            currentPath = parentPath;
            updateClientListView(currentPath);
        }
    }

    private void deleteFile() {
        String fileName = clientListView.getSelectionModel().getSelectedItem().getName();
        if (fileName == null) {
            return;
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
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(WARN_RESOURCE);
        alert.showAndWait();
        return alert.getResult() == ButtonType.CANCEL;
    }

    private boolean deleteFileAlert(String str) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deletion");
        alert.setHeaderText("Deletion");
        alert.setContentText("Are you sure?\n" + str + " will be deleted!");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(WARN_RESOURCE);
        alert.showAndWait();
        return alert.getResult() == ButtonType.CANCEL;
    }

    private void createDir() {
        String name = getNewNameFromDialog("Enter directory name", "Create a directory", "Directory name:");
        if (!name.isBlank()) {
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

    private String getNewNameFromDialog(String s, String s2, String s3) {
        TextInputDialog editDialog = new TextInputDialog();
        editDialog.setTitle(s2);
        editDialog.setHeaderText(s);
        editDialog.setContentText(s3);
        DialogPane dialogPane = editDialog.getDialogPane();
        dialogPane.getStylesheets().add(INFO_RESOURCE);
        Optional<String> optName = editDialog.showAndWait();
        return optName.orElse("");
    }

    private void renameFile() {
        if (clientListView.getSelectionModel().getSelectedItem() != null) {
            String file = clientListView.getSelectionModel().getSelectedItem().getName();
            Path path = Path.of(currentPath.toString(), file);
            String name, extension = "";
            if (file.contains(".") && !file.startsWith(".")) {
                name = file.substring(0, file.lastIndexOf("."));
                extension = file.substring(file.lastIndexOf("."));
            } else {
                name = file;
            }
            String newName = getNewNameFromDialog(name + " will be renamed!", "Rename file", "New name:");
            if (!newName.isBlank()) {
                newName += extension;
                Path newPath = currentPath.resolve(newName);
                if (!Files.exists(newPath)) {
                    try {
                        if (Files.isDirectory(path)) {
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
                        } else {
                            Files.move(path, path.resolveSibling(newName));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateClientListView(currentPath);
                    log.debug(file + " renamed to " + newName);
                    showAlert(file + " renamed to " + newName, Alert.AlertType.INFORMATION);
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
        String name = serverListView.getSelectionModel().getSelectedItem().getName();
        if (name == null) {
            return;
        }
        if (deleteFileAlert(name)) return;
        if (!name.isEmpty()) {
            if (serverListView.getSelectionModel().getSelectedItem().getType().equals(FileInfoCommandData.DIRECTORY)) {
                if (deleteDirAlert(name)) return;
            }
            try {
                network.sendDeleteRequest(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDirRequest() {
        String name = getNewNameFromDialog("Enter directory name", "Create a directory", "Directory name:");
        if (!name.isBlank()) {
            try {
                network.sendCreateDirRequest(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void renameRequest() {
        if (serverListView.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        String oldName = serverListView.getSelectionModel().getSelectedItem().getName();
        String name, extension = "";
        if (oldName.contains(".") && !oldName.startsWith(".")){
            name = oldName.substring(0, oldName.lastIndexOf("."));
            extension = oldName.substring(oldName.lastIndexOf("."));
        }else {
            name = oldName;
        }
        String newName = getNewNameFromDialog(name + " will be renamed!", "Rename file", "New name:");
        if (!newName.isBlank()) {
            newName += extension;
            network.sendRenameRequest(oldName, newName);
        }
    }

    public void updateClientListView(Path path) {
        currentPathLabelClient.setText(currentPath.toString());
        clientListView.getItems().clear();
        List<FileInfoCommandData> fileListClient = new ArrayList<>();
        try {
            fileListClient.addAll(Files.list(path).map(p -> {
                try {
                    return new FileInfoCommandData(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientListView.getItems().addAll(fileListClient);
    }

    public void updateServerListView(List<FileInfoCommandData> files) {
        currentPathLabelServer.setText(files.get(0).getName());
        files.remove(0);
        serverListView.getItems().clear();
        serverListView.getItems().addAll(files);
    }

    // Выбор файла для выгрузки с помощью мыши
    public void selectFileToUploadMouse(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            getFileToUpload();
        }
    }

    // Выбор файла для выгрузки с помощью клавиатуры
    public void selectFileToUploadKey(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFileToUpload();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            moveToParent();
        }
    }

    // Получение данных о фале и передача имени в input(TextField)
    private void getFileToUpload() {
        if (clientListView.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        String fileName = clientListView.getSelectionModel().getSelectedItem().getName();
        Path path = clientListView.getSelectionModel().getSelectedItem().getPath();
        if (Files.isDirectory(path)) {
            currentPath = path;
            updateClientListView(currentPath);
        } else {
            input.setText(fileName);
            input.requestFocus();
        }
    }

    // Передача файла на сервер
    public void upload() throws IOException {
        if (!input.getText().isBlank()) {
            Path path = currentPath.resolve(input.getText());
            long selectedFileSize = Files.size(path);
            FileInputStream fis = new FileInputStream(path.toString());
            byte[] buffer = new byte[BUFFER_SIZE];
            int readBytes;
            boolean start = true;
            while ((readBytes = fis.read(buffer)) != -1) {
                network.sendFile(input.getText(), selectedFileSize, buffer, start, readBytes);
                start = false;
            }
            input.clear();
            fis.close();
            clientListView.requestFocus();
        }
    }

    // Выбор файла для загрузки с помощью мыши
    public void selectFileToDownloadMouse(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            getFileToDownload();
        }
    }

    // Управление serverListView с помощью клавиатуры
    public void handleServerListViewKey(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getFileToDownload();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            getServerParent();
        } else if (keyEvent.getCode() == KeyCode.DELETE) {
            deleteRequest();
        }
    }

    // Передача имени файла для загрузки в output(TextField)
    private void getFileToDownload() {
        if (serverListView.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        String fileName = serverListView.getSelectionModel().getSelectedItem().getName();
        if (serverListView.getSelectionModel().getSelectedItem().getType().equals(FileInfoCommandData.DIRECTORY)) {
            network.sendFileRequest(fileName);
        } else {
            output.setText(fileName);
            output.requestFocus();
        }
    }

    @FXML
    private void downloadRequest() {
        if (!output.getText().isBlank()) {
            network.sendFileRequest(output.getText());
            output.clear();
        }
        serverListView.requestFocus();
    }

    // Загрузка файла с сервера
    public void download(String fileName, long fileSize, byte[] bytes, boolean isStart, int endPos) throws IOException {
        Path path = getPathOfCopy(fileName, isStart);
        FileOutputStream fos = new FileOutputStream(path.toString(), true);
        fos.write(bytes, 0, endPos);
        if (Files.size(path) == fileSize) {
            copyNumber = 0;
            updateClientListView(currentPath);
            log.debug(fileName + " downloaded.");
            showAlert(fileName + " downloaded.", Alert.AlertType.INFORMATION);
        }
        fos.close();
    }

    // Если файл существует - делаем копию, а не удаляем (с учётом загрузки частями).
    private Path getPathOfCopy(String fileName, boolean isStart) {
        Path path = currentPath.resolve(fileName);
        String name;
        while (isStart && Files.exists(path)) {
            copyNumber++;
            if (fileName.contains(".") && !fileName.startsWith(".")) {
                name = fileName.substring(0, fileName.lastIndexOf("."))
                        + "(" + copyNumber + ")"
                        + fileName.substring(fileName.lastIndexOf("."));
            } else {
                name = fileName + "(" + copyNumber + ")";
            }
            path = currentPath.resolve(name);
        }
        if (!isStart && copyNumber != 0) {
            name = fileName.substring(0, fileName.lastIndexOf("."))
                    + "(" + copyNumber + ")"
                    + fileName.substring(fileName.lastIndexOf("."));
            path = currentPath.resolve(name);
        }
        return path;
    }

    boolean showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        if (type.equals(Alert.AlertType.ERROR)) {
            dialogPane.getStylesheets().add(WARN_RESOURCE);
        }else {
            dialogPane.getStylesheets().add(INFO_RESOURCE);
        }
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    // Вспомогательный метод текстового поля
    public void keyHandleInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP) {
            input.clear();
            clientListView.requestFocus();
        } else if (keyEvent.getCode() == KeyCode.LEFT) {
            output.requestFocus();
        }
    }

    // Вспомогательный метод текстового поля
    public void keyHandleOutput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP) {
            output.clear();
            serverListView.requestFocus();
        } else if (keyEvent.getCode() == KeyCode.RIGHT) {
            input.requestFocus();
        }
    }

    public void connectLost() {
        connectLabel.setText("SERVER: OFF. Reconnect?");
        log.warn("Connection lost");
        if (showAlert("Connection lost. Reconnect?", Alert.AlertType.CONFIRMATION)) {
            network.reAuth();
        }
    }

    public void labelReconnect(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            network.reAuth();
        }
    }

    public void changeUsername() {
        String name = getNewNameFromDialog("Enter new name", "Change nick", "New name:");
        if (!name.isBlank()) {
            network.sendChangeUsername(name);
        }
    }
}

