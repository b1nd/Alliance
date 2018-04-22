package com.buzulukov.alliance.controllers;


import com.buzulukov.alliance.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class SettingsController {

    private static SettingsController settingsController;

    public SettingsController() {
        settingsController = this;
    }

    static SettingsController getInstance() {
        return settingsController;
    }

    @FXML
    public ListView<HBox> menuListView;

    public void initialize() {
        initializeMenuListView();
    }

    private void initializeMenuListView() {
        final Insets hBoxInsets = new Insets(3);
        final Insets imageInsets = new Insets(0, 0, 0, 20);
        final Insets labelInsets = new Insets(4, 0, 0, 0);

        ImageView newAccountImage = new ImageView(new Image(
                "com/buzulukov/alliance/resources/add-account-button.png",
                24, 24,
                true, true, false));
        Label newAccountLabel = new Label("New Account");
        newAccountLabel.getStyleClass().setAll("label-bold");

        HBox newAccountHBox = new HBox(newAccountImage, newAccountLabel);
        newAccountHBox.setMinHeight(31);
        HBox.setMargin(newAccountImage, imageInsets);
        HBox.setMargin(newAccountLabel, labelInsets);
        newAccountHBox.setSpacing(20);
        newAccountHBox.setPadding(hBoxInsets);
        newAccountHBox.setOnMouseClicked(event -> App.showNewAccountWindow());

        ObservableList<HBox> items = FXCollections.observableArrayList();
        items.addAll(newAccountHBox);

        menuListView.setItems(items);
    }
}
