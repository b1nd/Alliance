package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Message;
import com.buzulukov.alliance.controllers.selection_models.NoSelectionModel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

public class MainController {

    // Date format: 23:24 ср 15.04
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("HH:mm E dd.MM");
    // Date format: 23:24
    private static final SimpleDateFormat HOURS_MINS = new SimpleDateFormat("HH:mm");

    @FXML
    public SplitPane dialogsChatSplitPane;
    @FXML
    public Button settingsButton;

    private static MainController mainController;

    public MainController() {
        mainController = this;
    }

    static MainController getInstance() {
        return mainController;
    }

    public void initialize() {
        initializeChat();
        initializeDialogs();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
//                                        DIALOGS PANE                                           //
///////////////////////////////////////////////////////////////////////////////////////////////////

    private final static double DIALOGS_PANE_MIN_WIDTH = 250;
    private final static int MESSAGES_UPDATE_TIME_MS = 1000;

    @FXML
    public AnchorPane dialogsAnchorPane;
    @FXML
    public MenuButton accountsChoice;
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

    void updateDialogsScreen(String... messengerNames) {
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

        ObservableList<Chat> items = FXCollections.observableArrayList();
        items.add(Chat.EMPTY);
        dialogsListView.setItems(items);
    }

    private void initializeDialogsPane() {
        dialogsAnchorPane.setMinWidth(DIALOGS_PANE_MIN_WIDTH);
    }

    public void onSettingsClicked(ActionEvent actionEvent) {
        Timeline tl = new Timeline();
        tl.setAutoReverse(true);
        KeyValue kv = new KeyValue(App.settingsRoot.layoutXProperty(), 0);
        KeyFrame kf = new KeyFrame(Duration.millis(150), kv);
        tl.getKeyFrames().add(kf);
        tl.play();

        App.settingsStage.show();
    }

    public void onBackToDialogs(ActionEvent actionEvent) {

    }

    static class ChatCell extends ListCell<Chat> {
        private static final Insets INSETS = new Insets(5);

        @Override
        protected void updateItem(Chat item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
            } else {
                if (item == Chat.EMPTY) {
                    TextField emptyTextField = new TextField("Chats will be shown here.");
                    emptyTextField.setEditable(false);
                    emptyTextField.setAlignment(Pos.CENTER);
                    emptyTextField.setOpaqueInsets(new Insets(5));
                    emptyTextField.setBackground(new Background(new BackgroundFill(
                            ACTIVE_WRAPPER_COLOR,
                            new CornerRadii(15),
                            new Insets(0))));
                    StackPane stackPane = new StackPane(emptyTextField);
                    stackPane.setPadding(INSETS);
                    StackPane.setAlignment(emptyTextField, Pos.CENTER_LEFT);

                    setGraphic(stackPane);
                } else {
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.getStyleClass().add("label-bold");
                    titleLabel.setPadding(new Insets(2, 0, 0, 0));
                    Label dateLabel = new Label(" " + HOURS_MINS.format(item.getLastMessage().getDate()));
                    dateLabel.setMinWidth(35);
                    dateLabel.setPadding(new Insets(2, 0, 0, 0));
                    dateLabel.getStyleClass().add("label-message");
                    Label libraryNameLabel = new Label(item.getLibraryName());
                    libraryNameLabel.setMinWidth(0);
                    libraryNameLabel.setTextOverrun(OverrunStyle.CLIP);
                    libraryNameLabel.setPadding(new Insets(2, 0, 0, 0));
                    libraryNameLabel.getStyleClass().add("label-message");

                    Pane justSpace = new Pane();
                    HBox topHBox = new HBox(titleLabel, justSpace, libraryNameLabel, dateLabel);
                    HBox.setHgrow(justSpace, Priority.ALWAYS);

                    String messageText = item.getLastMessage().getText();

                    if (messageText.contains("\n")) {
                        messageText = messageText.substring(0, messageText.indexOf("\n")) + "...";
                    }
                    TextField messageTextField = new TextField(messageText);
                    messageTextField.setEditable(false);
                    messageTextField.getStyleClass().setAll("text-field-message");
                    messageTextField.setPadding(new Insets(5, 0, 0, 0));

                    Label fromLabel;

                    if (item.getLastMessage().isOutgoing()) {
                        fromLabel = new Label("You:");
                        fromLabel.setMinWidth(28);
                        fromLabel.setMaxWidth(28);
                    } else {
                        fromLabel = new Label(""); // May be get user name?
                    }
                    fromLabel.setPadding(new Insets(5, 0, 0, 0));
                    fromLabel.getStyleClass().add("label-from");
                    HBox botHBox = new HBox(fromLabel, messageTextField);
                    HBox.setHgrow(messageTextField, Priority.ALWAYS);

                    VBox rightVBox = new VBox(topHBox, botHBox);
                    ImageView chatImage = new ImageView(new Image(
                            item.getChatPhotoUri(),
                            40, 40,
                            true, true, false));
                    Circle clip = new Circle(20, 20, 20);
                    chatImage.setClip(clip);
                    HBox chatCell = new HBox(chatImage, rightVBox);
                    chatCell.setSpacing(10);
                    HBox.setHgrow(rightVBox, Priority.ALWAYS);
                    chatCell.setPadding(INSETS);

                    //TODO: setOnMouseClicked(event -> update right list cell with messages);

                    setGraphic(chatCell);
                    prefWidthProperty().bind(getListView().prefWidthProperty().subtract(2));
                }
            }
        }

    }

///////////////////////////////////////////////////////////////////////////////////////////////////
//                                         CHAT PANE                                             //
///////////////////////////////////////////////////////////////////////////////////////////////////

    private static final Color MESSAGE_WRAPPER_COLOR = Color.web("33393f");
    private static final Color ACTIVE_WRAPPER_COLOR = Color.web("009287");
    private static final double CHAT_PANE_MIN_WIDTH = 300;
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
        //initializeEmptyChat();
    }

    private void initializeEmptyChat() {
        var pane = new BorderPane();
        pane.getStyleClass().setAll("pane");
        pane.setMinWidth(CHAT_PANE_MIN_WIDTH);
        var label = new Label("Select chat to start messaging.");
        label.setMinWidth(250);
        label.setAlignment(Pos.CENTER);
        var grey = new Background(new BackgroundFill(
                ACTIVE_WRAPPER_COLOR,
                new CornerRadii(15),
                new Insets(-3)));
        label.setWrapText(true);
        label.setBackground(grey);
        pane.setCenter(label);
        Platform.runLater(() -> dialogsChatSplitPane.getItems().set(1, pane));
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
                ACTIVE_WRAPPER_COLOR,
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
