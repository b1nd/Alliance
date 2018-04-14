package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Message;
import com.buzulukov.alliance.controllers.selection_models.NoSelectionModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

public class MainController {

    // Date format: 23:24 ср 07.03
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("HH:mm E dd.MM");

    @FXML
    public SplitPane settingsDialogsSplitPane;
    @FXML
    public SplitPane dialogsChatSplitPane;

    public void initialize() {
        initializeChat();
        initializeSettings();
        initializeDialogs();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
//                                       SETTINGS PANE                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////

    private static final double SETTINGS_PANE_MAX_WIDTH = 200;

    @FXML
    public AnchorPane settingsAnchorPane;
    @FXML
    public ComboBox<String> accountLoginChoice;
    @FXML
    public Button loginButton;

    public void onSettingsButtonToggled(ActionEvent actionEvent) {
        if (settingsToggleButton.isSelected()) {
            settingsAnchorPane.setMaxWidth(SETTINGS_PANE_MAX_WIDTH);
            settingsAnchorPane.setMinWidth(SETTINGS_PANE_MAX_WIDTH);
        } else {
            settingsAnchorPane.setMinWidth(0);
            settingsAnchorPane.setMaxWidth(0);

            if (App.MESSENGERS_ADAPTER.updateMessengers()) {
                System.out.println("Some messengers were added");
                Platform.runLater(this::updateDialogsScreen);
            }
        }
    }

    private void initializeSettings() {
        initializeSettingsSize();
        loadAccountNames();
    }

    private void initializeSettingsSize() {
        settingsAnchorPane.setMinWidth(0);
        settingsAnchorPane.setMaxWidth(0);
    }

    private void loadAccountNames() {
        var loginItems = FXCollections.observableArrayList(App.MESSENGERS_ADAPTER.getMessengerNames());
        accountLoginChoice.setItems(loginItems);
        accountLoginChoice.setValue(loginItems.get(0));
    }

    public void onLoginClicked(ActionEvent actionEvent) {
        App.MESSENGERS_ADAPTER.authorize(accountLoginChoice.getValue(), "desktop");
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
//                                        DIALOGS PANE                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////

    private final static double DIALOGS_PANE_MIN_WIDTH = 135;
    private final static int MESSAGES_UPDATE_TIME_MS = 1000;

    @FXML
    public AnchorPane dialogsAnchorPane;
    @FXML
    public MenuButton accountsChoice;
    @FXML
    public ToggleButton settingsToggleButton;
    @FXML
    public TextField searchTextField;
    @FXML
    public ListView<Chat> dialogsListView;

    private ScheduledService<Boolean> updateService = new ScheduledService<Boolean>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    if (isCancelled()) {
                        return false;
                    }
                    boolean updated = App.MESSENGERS_ADAPTER.updateChats();

                    if (updated) {
                        Platform.runLater(MainController.this::updateDialogsScreen);
                    }
                    return updated;
                }
            };
        }
    };

    private void updateDialogsScreen(String... messengerNames) {
        ObservableList<Chat> items = FXCollections.observableArrayList();

        items.addAll(App.MESSENGERS_ADAPTER.getChats(messengerNames));

        dialogsListView.setItems(items);
        dialogsListView.refresh();
    }

    private void initializeDialogs() {
        initializeDialogsListView();
        initializeDialogsPane();
        initializeDialogsUpdate();
    }

    private void initializeDialogsUpdate() {
        updateService.setDelay(Duration.millis(MESSAGES_UPDATE_TIME_MS));
        updateService.setPeriod(Duration.millis(MESSAGES_UPDATE_TIME_MS));
        updateService.start();
    }

    private void initializeDialogsListView() {
        dialogsListView.setSelectionModel(new NoSelectionModel<>());
        dialogsListView.setCellFactory(param -> new ChatCell());
    }

    private void initializeDialogsPane() {
        dialogsAnchorPane.setMinWidth(DIALOGS_PANE_MIN_WIDTH);
    }

    static class ChatCell extends ListCell<Chat> {
        private static final Insets INSETS = new Insets(10);

        @Override
        protected void updateItem(Chat item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
            } else {
                if (item == Chat.EMPTY) {
                    Label label = new Label("Chats will be shown here.");

                    StackPane stackPane = new StackPane(label);
                    stackPane.setPadding(INSETS);
                    StackPane.setAlignment(label, Pos.CENTER_LEFT);

                    setGraphic(stackPane);
                } else {
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setMinWidth(150);
                    titleLabel.setMaxWidth(150);
                    titleLabel.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize()));

                    String messageText = item.getLastMessage().getText();

                    if (messageText.contains("\n")) {
                        messageText = messageText.substring(0, messageText.indexOf("\n")) + "...";
                    }
                    Label messageLabel = new Label(messageText);
                    HBox hBox = new HBox(titleLabel, messageLabel);
                    hBox.setSpacing(10);

                    Label accountLabel = new Label(item.getLibraryName());
                    Label dateLabel = new Label(SHORT_DATE_FORMAT.format(item.getLastMessage().getDate()));

                    StackPane stackPane = new StackPane(accountLabel, dateLabel);
                    StackPane.setAlignment(accountLabel, Pos.CENTER_LEFT);
                    StackPane.setAlignment(dateLabel, Pos.CENTER_RIGHT);

                    VBox vBox = new VBox(hBox, stackPane);
                    vBox.setPadding(INSETS);

                    //TODO: setOnMouseClicked(event -> update right list cell with messages);

                    setGraphic(vBox);
                    prefWidthProperty().bind(getListView().prefWidthProperty().subtract(2));
                }
            }
        }

    }

///////////////////////////////////////////////////////////////////////////////////////////////////
//                                         CHAT PANE                                             //
///////////////////////////////////////////////////////////////////////////////////////////////////

    private static final double CHAT_PANE_MIN_WIDTH = 300;
    private static final Color MESSAGE_WRAPPER_COLOR = Color.web("ACE0EE");
    private static final int TEXT_INPUT_MAX_NUMBER_OF_LINES = 20;

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
    public TextArea sendMessageTextArea;
    @FXML
    public Button sendButton;

    private static Chat chat;

    private void initializeChat() {
        Platform.runLater(this::enableTextAreaAutoResize);
        initializeChatSize();
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

    private void initializeChatSize() {
        chatGridPane.setMinWidth(CHAT_PANE_MIN_WIDTH);
    }

    public void onSendClicked(ActionEvent actionEvent) {

    }

    static class MessageCell extends ListCell<Message> {
        private static final Insets INSETS = new Insets(10);
        private static final Background MESSAGE_BACKGROUND = new Background(new BackgroundFill(
                MESSAGE_WRAPPER_COLOR,
                new CornerRadii(4),
                new Insets(-10)));

        ChangeListener<Number> scrollListener = (observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0 && !chat.areAllMessagesLoaded()) {
                Platform.runLater(() -> {
                    Message currentMessage = chat.getFirstMessage();

                    chat.loadMessages();

                    ObservableList<Message> items = FXCollections.observableArrayList();
                    items.addAll(chat.getMessages());
                    getListView().setItems(items);
                    getListView().scrollTo(currentMessage);
                });
            }
        };

        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
            } else {
                if (item == Message.EMPTY) {
                    Label label = new Label("Messages will be shown here.");
                    setGraphic(label);
                } else {
                    Label messageLabel = new Label(item.getText());
                    messageLabel.setWrapText(true);
                    messageLabel.setBackground(MESSAGE_BACKGROUND);

                    Label dateLabel = new Label(SHORT_DATE_FORMAT.format(item.getDate()));
                    dateLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            System.out.println("Date label width = " + dateLabel.getWidth());
                        }
                    });
                    dateLabel.setMinWidth(78);

                    HBox hBox;

                    if (item.isOutgoing()) {
                        hBox = new HBox(dateLabel, messageLabel);
                        hBox.setAlignment(Pos.BOTTOM_RIGHT);
                    } else {
                        hBox = new HBox(messageLabel, dateLabel);
                        hBox.setAlignment(Pos.BOTTOM_LEFT);
                    }
                    hBox.setPadding(INSETS);
                    hBox.setSpacing(20);

                    setGraphic(hBox);

                    prefWidthProperty().bind(getListView().prefWidthProperty().subtract(2));
                    layoutYProperty().removeListener(scrollListener);

                    if (item == chat.getFirstMessage()) {
                        layoutYProperty().addListener(scrollListener);
                    }
                }
            }
        }

    }
}
