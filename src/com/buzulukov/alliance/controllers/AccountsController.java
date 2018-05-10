package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import com.buzulukov.alliance.controllers.selection_models.NoSelectionModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

public class AccountsController {

    private static AccountsController instance;

    public AccountsController() {
        instance = this;
    }

    public static AccountsController getInstance() {
        return instance;
    }

    @FXML
    public ListView<String> accountsListView;

    public void initialize() {
        accountsListView.setSelectionModel(new NoSelectionModel<>());
        accountsListView.setCellFactory(param -> new AccountCell());
    }

    public void onClose(ActionEvent actionEvent) {
        App.accountsStage.close();
    }

    public void updateAccountsScreen() {
        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll(App.MESSENGERS_ADAPTER.getAccountNames());
        accountsListView.setItems(items);
    }

    static class AccountCell extends ListCell<String> {
        private static final Insets INSETS = new Insets(10);

        @Override
        protected void updateItem(String accountName, boolean empty) {
            super.updateItem(accountName, empty);

            if (accountName == null || empty) {
                setGraphic(null);
            } else {
                Label accountInfoLabel = new Label(accountName);
                accountInfoLabel.getStyleClass().add("label-bold");
                Button logoutButton = new Button("Logout");
                logoutButton.getStyleClass().add("button-main");
                logoutButton.setOnAction(event -> {
                    App.MESSENGERS_ADAPTER.logout(accountName);
                    AccountsController.getInstance().updateAccountsScreen();
                });
                StackPane stackPane = new StackPane(accountInfoLabel, logoutButton);
                stackPane.setPadding(INSETS);
                StackPane.setAlignment(accountInfoLabel, Pos.CENTER_LEFT);
                StackPane.setAlignment(logoutButton, Pos.CENTER_RIGHT);
                setGraphic(stackPane);
            }
        }

    }
}
