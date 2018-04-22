package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class NewAccountController {

    @FXML
    public ComboBox<String> accChoiceComboBox;

    public void initialize() {
        ObservableList<String> items = FXCollections.observableArrayList(App.MESSENGERS_ADAPTER.getMessengerNames());
        accChoiceComboBox.setItems(items);
        accChoiceComboBox.setValue(items.get(0));
    }


    public void onLoginClicked(ActionEvent actionEvent) {
        App.MESSENGERS_ADAPTER.authorize(accChoiceComboBox.getValue(), "desktop");
    }

    public void onCancelClicked(ActionEvent actionEvent) {
        App.newAccountStage.close();
    }

    public void onUpdateClicked(ActionEvent actionEvent) {
        if (App.MESSENGERS_ADAPTER.updateMessengers()) {
            MainController.getInstance().updateDialogsScreen();
            App.newAccountStage.close();
        }
    }
}
