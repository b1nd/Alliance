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

    @FXML public ListView<HBox> menuListView;

    public void initialize() {
        initializeMenuListView();
    }

    private void initializeMenuListView() {
        final Insets hBoxInsets = new Insets(3);
        final Insets imageInsets = new Insets(0, 0, 0, 20);
        final Insets labelInsets = new Insets(4, 0, 0, 0);

        // Add account.
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

        // All accounts.
        ImageView accountsImage = new ImageView(new Image(
                "com/buzulukov/alliance/resources/my-accounts-button.png",
                24, 24,
                true, true, false));
        Label accountsLabel = new Label("My Accounts");
        accountsLabel.getStyleClass().setAll("label-bold");

        HBox accountsHBox = new HBox(accountsImage, accountsLabel);
        accountsHBox.setMinHeight(31);
        HBox.setMargin(accountsImage, imageInsets);
        HBox.setMargin(accountsLabel, labelInsets);
        accountsHBox.setSpacing(20);
        accountsHBox.setPadding(hBoxInsets);
        accountsHBox.setOnMouseClicked(event -> App.showAccountsWindow());

        ObservableList<HBox> items = FXCollections.observableArrayList();
        items.addAll(newAccountHBox, accountsHBox);

        menuListView.setItems(items);
    }
}
