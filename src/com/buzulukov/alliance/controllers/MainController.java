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
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    @FXML public SplitPane dialogsChatSplitPane;
    @FXML public Button settingsButton;

    private static MainController mainController;

    public MainController() {
        mainController = this;
    }

    public static MainController getInstance() {
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

    private String regexDialogsFilter = "";

    @FXML public AnchorPane dialogsAnchorPane;
    @FXML public Button searchButton;
    @FXML public TextField searchTextField;
    @FXML public ListView<Chat> dialogsListView;

    private ScheduledService<Boolean> updateService = new ScheduledService<>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() {
                    if (isCancelled()) {
                        return false;
                    }
                    boolean updated = App.MESSENGERS_ADAPTER.updateChats();

                    if (updated) {
                        updateDialogsScreen(regexDialogsFilter);
                        Platform.runLater(MainController.this::updateChatScreen);
                    }
                    return updated;
                }
            };
        }
    };

    public void updateDialogsScreen(String regex) {
        ObservableList<Chat> items = FXCollections.observableArrayList();
        items.addAll(App.MESSENGERS_ADAPTER.getChats(regex));

        Platform.runLater(() -> dialogsListView.setItems(items));
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
        chat = null;
        initializeEmptyChat();
    }

    public void onSearchClicked(ActionEvent actionEvent) {
        regexDialogsFilter = searchTextField.getText();
        updateDialogsScreen(regexDialogsFilter);
    }

    class ChatCell extends ListCell<Chat> {
        private final Insets INSETS = new Insets(5);

        @Override protected void updateItem(Chat item, boolean empty) {
            TextField textCheck = null;
            Label libraryCheck = null;
            Label titleCheck = null;
            Label dateCheck = null;

            if(super.getGraphic() != null) {
                libraryCheck = (Label) super.getGraphic().lookup("#library-name");
                dateCheck = (Label) super.getGraphic().lookup("#date-label");
                titleCheck = (Label) super.getGraphic().lookup("#title-label");
                textCheck = (TextField) super.getGraphic().lookup("#message-text");
            }
            if(item != null && libraryCheck != null && dateCheck != null && titleCheck != null && textCheck != null &&
                    libraryCheck.getText().equals(item.getLibraryName()) &&
                    titleCheck.getText().equals(item.getTitle()) &&
                    dateCheck.getText().equals(" " + HOURS_MINS.format(item.getLastMessage().getDate())) &&
                    textCheck.getText().equals(item.getLastMessage().getText())) {
                return;
            }
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
                    titleLabel.setId("title-label");
                    titleLabel.getStyleClass().add("label-bold");
                    titleLabel.setPadding(new Insets(2, 0, 0, 0));
                    Label dateLabel = new Label(" " + HOURS_MINS.format(item.getLastMessage().getDate()));
                    dateLabel.setId("date-label");
                    dateLabel.setMinWidth(35);
                    dateLabel.setPadding(new Insets(2, 0, 0, 0));
                    dateLabel.getStyleClass().add("label-message");
                    Label libraryNameLabel = new Label(item.getLibraryName());
                    libraryNameLabel.setId("library-name");
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
                    messageTextField.setId("message-text");
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

                    setOnMouseClicked(event -> initializeChatScreen(item));

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

    @FXML public GridPane chatGridPane;
    @FXML public Button backToDialogsButton;
    @FXML public Label chatTitleLabel;
    @FXML public Label lastActivityLabel;
    @FXML public Label accountLabel;
    @FXML public ListView<Message> chatListView;
    @FXML public TextArea sendMessageTextArea;
    @FXML public Button sendButton;

    private static Chat chat;

    private void initializeChat() {
        Platform.runLater(this::enableTextAreaAutoResize);
        initializeChatSize();
        initializeEmptyChat();
        initializeChatListView();
    }

    private void initializeChatListView() {
        chatListView.setSelectionModel(new NoSelectionModel<>());
        chatListView.setCellFactory(param -> new MessageCell());

        ObservableList<Message> items = FXCollections.observableArrayList();
        items.add(Message.EMPTY);
        chatListView.setItems(items);
    }

    private void updateChatScreen() {
        if (chat != null) {
            ObservableList<Message> items = chatListView.getItems();

            if (items.get(items.size() - 1) != chat.getLastMessage()) {
                items.add(chat.getLastMessage());
            }
        }
    }

    private void initializeChatScreen(Chat selectedChat) {
        chat = selectedChat;
        chatTitleLabel.setText(chat.getTitle());
        lastActivityLabel.setText(SHORT_DATE_FORMAT.format(chat.getLastMessage().getDate()));
        accountLabel.setText(chat.getLibraryName());

        ObservableList<Message> items = FXCollections.observableArrayList();
        items.addAll(chat.getMessages());

        if (items.isEmpty()) {
            items.add(Message.EMPTY);
        }
        chatListView.setItems(items);
        chatListView.scrollTo(chat.getLastMessage());
        Platform.runLater(() -> dialogsChatSplitPane.getItems().set(1, chatGridPane));
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

            @Override public Double call() {
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
        Platform.runLater(() -> chat.sendMessage(sendMessageTextArea.getText()));
        Platform.runLater(sendMessageTextArea::clear);
    }

    static class MessageCell extends ListCell<Message> {
        private static final Insets INSETS = new Insets(2);

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

        void initMessageTextArea(TextArea textArea) {
            try {
                Text text = (Text) textArea.lookup(".text");
                textArea.minHeightProperty().bind(Bindings.createDoubleBinding(new Callable<>() {
                    private double maxHeight = text.getBoundsInLocal().getHeight() * TEXT_INPUT_MAX_NUMBER_OF_LINES;

                    @Override public Double call() {
                        if (text.getBoundsInLocal().getHeight() < maxHeight) {
                            return text.getBoundsInLocal().getHeight();
                        } else {
                            return maxHeight;
                        }
                    }
                }, text.boundsInLocalProperty()).add(15));

                // Anti blur.
                textArea.setCache(false);
                ScrollPane sp = (ScrollPane) textArea.getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }
            } catch (Exception e) {
                // TODO: idk why but sometimes text throws NullPointerException. Fix it!!!
            }
        }

        @Override protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
            } else {
                if (item == Message.EMPTY) {
                    Label label = new Label("You have no messages.");
                    label.setStyle("");
                    setGraphic(label);
                } else {
                    TextArea messageLabel = new TextArea();
                    messageLabel.setPrefRowCount(0);
                    messageLabel.setMinWidth(50);
                    messageLabel.setPrefWidth(50);
                    messageLabel.textProperty().addListener((observable, oldValue, newValue) ->
                            messageLabel.setPrefWidth(messageLabel.getText().length() * 7 + 25));
                    messageLabel.setText(item.getText());
                    messageLabel.getStyleClass().add("text-area-message");
                    messageLabel.setCache(false);
                    Platform.runLater(() -> initMessageTextArea(messageLabel));
                    messageLabel.setEditable(false);
                    messageLabel.setWrapText(true);

                    Label dateLabel = new Label(SHORT_DATE_FORMAT.format(item.getDate()));
                    dateLabel.getStyleClass().add("label-date");
                    dateLabel.setMinWidth(35);
                    dateLabel.setTextOverrun(OverrunStyle.CLIP);

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
