package com.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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
    private List<Path> fileListClient;
    private String uploadFileName;
    private long uploadFileSize;
    private Path uploadFilepath = null;
    private FileInputStream fis;
    private byte[] uploadFileBytes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateClientListView(Path.of(System.getProperty("user.dir")));
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

    public void updateClientListView(Path path) {
        clientListView.getItems().clear();
        fileListClient = new ArrayList<>();
        try {
            fileListClient.addAll(Files.list(path).map(Path::toAbsolutePath).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> list = fileListClient.stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());
        clientListView.getItems().addAll(list);
//        for (Path path1 : fileListClient) {
//            clientListView.getItems().add(String.valueOf(path1.getFileName()));
//        }
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
        Path upperPath = uploadFilepath.getParent();
        if (upperPath != null) {
            updateClientListView(upperPath);
        }
    }

    private void getFile() throws IOException {
        if (fis != null) {
            fis = null;
//            fis.close();   // does not work
        }
        uploadFileName = clientListView.getSelectionModel().getSelectedItem();
        for (Path value : fileListClient) {
            if (value.getFileName().equals(Paths.get(uploadFileName))) {
                uploadFilepath = value;
            }
        }
        assert uploadFilepath != null;
        if (Files.isDirectory(uploadFilepath)) {
            updateClientListView(uploadFilepath);
        } else {
            uploadFileSize = Files.size(uploadFilepath);
            fis = new FileInputStream(String.valueOf(uploadFilepath));
            uploadFileBytes = new byte[(int) uploadFileSize];
            fis.read(uploadFileBytes);
            input.setText(uploadFileName);
            input.requestFocus();
        }
    }

    private void putMessage(String message) {
        input.clear();
        input.setText(message);
    }

    public void upload() throws IOException {
        if (fis != null) {
            network.sendFile(uploadFileName, uploadFileSize, uploadFileBytes);
            input.clear();
//            fis.close();  // does not work
            fis = null;
            uploadFileName = null;
            uploadFileSize = 0;
            clientListView.requestFocus();
        }
    }

    public void selectFileToDownloadMouse(MouseEvent mouseEvent) {

    }

    public void selectFileToDownloadKey(KeyEvent keyEvent) {

    }

    public void keyHandleInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE || keyEvent.getCode() == KeyCode.UP) {
            if (fis != null) {
                input.clear();
//            fis.close(); // does not work
                fis = null;
                uploadFileName = null;
                uploadFileSize = 0;
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

    public void getServerParent(ActionEvent actionEvent) {

    }

    public void getClientParent(ActionEvent actionEvent) {

        moveToParent();
    }
}

