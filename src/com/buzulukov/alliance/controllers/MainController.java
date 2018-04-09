package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.concurrent.Callable;

public class MainController {

    private static final int TEXT_INPUT_MAX_NUMBER_OF_LINES = 20;

    @FXML
    public AnchorPane dialogsAnchorPane;
    @FXML
    public MenuButton accountsChoice;
    @FXML
    public ToggleButton settingsToggleButton;
    @FXML
    public AnchorPane settingsAnchorPane;
    @FXML
    public TextArea sendMessageTextArea;
    @FXML
    public SplitPane settingsDialogsSplitPane;
    @FXML
    public SplitPane dialogsChatSplitPane;
    @FXML
    public TextField searchTextField;
    @FXML
    public ListView dialogsListView;
    @FXML
    public GridPane chatGridPane;
    @FXML
    public Button backToDialogsButton;
    @FXML
    public Label chatTitleLabel;
    @FXML
    public Label lastActivityLabel;
    @FXML
    public Label accountLabel;
    @FXML
    public ListView chatListView;
    @FXML
    public Button sendButton;
    @FXML
    public ComboBox<String> accountLoginChoice;
    @FXML
    public Button loginButton;


    public void initialize() {
        Platform.runLater(this::enableTextAreaAutoResize);
        loadAccountNames();
    }

    public void onSettingsButtonToggled(ActionEvent actionEvent) {
        if (settingsToggleButton.isSelected()) {
            settingsAnchorPane.setMaxWidth(200);
            settingsAnchorPane.setMinWidth(200);
        } else {
            settingsAnchorPane.setMinWidth(0);
            settingsAnchorPane.setMaxWidth(0);

            if(App.MESSENGERS_ADAPTER.updateMessengers()) {
                System.out.println("Some messengers were added");
            }
        }
    }

    private void loadAccountNames() {
        var loginItems = FXCollections.observableArrayList(App.MESSENGERS_ADAPTER.getMessengerNames());
        accountLoginChoice.setItems(loginItems);
        accountLoginChoice.setValue(loginItems.get(0));
    }

    private void enableTextAreaAutoResize() {
        Text text = (Text) sendMessageTextArea.lookup(".text");
        sendMessageTextArea.minHeightProperty().bind(Bindings.createDoubleBinding(new Callable<>() {
            private double maxHeight = text.getBoundsInLocal().getHeight() * TEXT_INPUT_MAX_NUMBER_OF_LINES;
            @Override
            public Double call() {
                if (text.getBoundsInLocal().getHeight() < maxHeight) {
                    return text.getBoundsInLocal().getHeight();
                } else {
                    return maxHeight;
                }
            }
        }, text.boundsInLocalProperty()).add(10));
        sendMessageTextArea.setPromptText("Write a message...");
    }

    public void onSendClicked(ActionEvent actionEvent) {

    }

    public void onLoginClicked(ActionEvent actionEvent) {
        App.MESSENGERS_ADAPTER.authorize(accountLoginChoice.getValue(), "desktop");
    }
}
