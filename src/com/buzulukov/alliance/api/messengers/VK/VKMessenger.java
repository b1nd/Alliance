package com.buzulukov.alliance.api.messengers.VK;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;
import com.buzulukov.alliance.web.utils.WebUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class VKMessenger implements Messenger, Serializable {

    public static final String LIBRARY_NAME = "VK";

    public static final String METHOD_URI = "https://api.vk.com/method/";
    public static final String API_VERSION = "5.71";

    private String accessToken;
    private String accountInfo;
    private int userId;
    private boolean isAuthorized;

    private transient LinkedList<Chat> chats;

    private transient JsonParser jsonParser;

    public VKMessenger() {
        jsonParser = new JsonParser();
    }

    @Override
    public String getName() {
        return LIBRARY_NAME;
    }

    @Override
    public boolean login(String... params) {
        if (params.length == 2) {
            accessToken = params[0];
            userId = Integer.parseInt(params[1]);
            isAuthorized = true;
        }
        return isAuthorized;
    }

    @Override
    public void logout() {
        isAuthorized = false;
    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public String getAccountInfo() {
        if (accountInfo == null) {
            var response = WebUtils.getResponse(METHOD_URI + "account.getProfileInfo",
                    "access_token=" + accessToken,
                    "v=" + API_VERSION);
            var responseObject = jsonParser.parse(response).getAsJsonObject().get("response").getAsJsonObject();
            accountInfo = responseObject.get("first_name").getAsString() + " " +
                    responseObject.get("last_name").getAsString();
        }
        return accountInfo;
    }

    @Override
    public LinkedList<Chat> getChats() {
        if (chats == null) {
            chats = new LinkedList<>();
            var userIds = new StringBuilder();
            var response = WebUtils.getResponse(METHOD_URI + "messages.getDialogs",
                    "access_token=" + accessToken,
                    "v=" + API_VERSION,
                    "count=200");
            var responseObject = getJsonObjectFromResponse(response);
            var itemsArray = responseObject.get("items").getAsJsonArray();

            for (int i = 0; i < itemsArray.size(); ++i) {
                var messageObject = itemsArray
                        .get(i).getAsJsonObject()
                        .get("message").getAsJsonObject();

                // Handle messages from groups.
                if (messageObject.get("user_id").getAsInt() < 0) {
                    int groupId = Math.abs(messageObject.get("user_id").getAsInt());
                    response = WebUtils.getResponse(METHOD_URI + "groups.getById",
                            "group_id=" + groupId,
                            "v=" + API_VERSION);
                    var responseGroupObject = getJsonArrayFromResponse(response).get(0).getAsJsonObject();
                    var photoUri = responseGroupObject.get("photo_50").getAsString();
                    var chatTitle = responseGroupObject.get("name").getAsString();
                    var chat = new VKChat(accessToken, photoUri, chatTitle, messageObject.get("user_id").getAsInt());
                    VKMessage message = createMessageFromJsonObject(messageObject);
                    chat.getMessages().addLast(message);
                    chats.addLast(chat);
                    continue;
                }
                VKMessage message = createMessageFromJsonObject(messageObject);
                VKChat chat;

                var chatTitle = messageObject.get("title").getAsString();
                int chatId = 0;

                if (messageObject.has("chat_id")) {
                    chatId = messageObject.get("chat_id").getAsInt();
                    String photoUri;
                    if (messageObject.has("photo_50")) {
                        photoUri = messageObject.get("photo_50").getAsString();
                    } else {
                        photoUri = "https://vk.com/images/camera_50.png";
                    }
                    chat = new VKChat(accessToken, photoUri, chatTitle, chatId);
                } else {
                    if (userIds.length() != 0) {
                        userIds.append(",");
                    }
                    userIds.append(messageObject.get("user_id").getAsString());
                    chat = new VKChat(accessToken, chatTitle, chatId);
                }
                chat.messages.addLast(message);
                chats.addLast(chat);
            }

            if (userIds.length() != 0) {
                response = WebUtils.getResponse(METHOD_URI + "users.get",
                        "access_token=" + accessToken,
                        "v=" + API_VERSION,
                        "fields=photo_50",
                        "user_ids=" + userIds.toString());
                var responseArray = getJsonArrayFromResponse(response);
                int i = 0;

                for (Chat chat : chats) {
                    if (chat.getTitle().isEmpty()) {
                        var userObject = responseArray.get(i).getAsJsonObject();
                        var firstName = userObject.get("first_name").getAsString();
                        var lastName = userObject.get("last_name").getAsString();
                        var photoUri = userObject.get("photo_50").getAsString();
                        ((VKChat) chat).title = firstName + " " + lastName;
                        ((VKChat) chat).chatPhotoUri = photoUri;
                        ++i;
                    }
                }
            }
        }
        Collections.sort(chats);
        return chats;
    }

    @Override
    public boolean updateChats() {
        var lastMessage = (VKMessage) chats.getFirst().getLastMessage();
        var response = WebUtils.getResponse(METHOD_URI + "messages.getDialogs",
                "access_token=" + accessToken,
                "v=" + API_VERSION,
                "offset=-1",
                "count=1",
                "start_message_id=" + lastMessage.id);
        var responseObject = getJsonObjectFromResponse(response);
        var itemsArray = responseObject.get("items").getAsJsonArray();

        if (itemsArray.size() == 0) {
            return false;
        }
        var chatObject = itemsArray.get(itemsArray.size() - 1).getAsJsonObject();
        var messageObject = chatObject.get("message").getAsJsonObject();

        // Handle messages from groups.
        if (messageObject.get("user_id").getAsInt() < 0) {
            int groupId = Math.abs(messageObject.get("user_id").getAsInt());
            response = WebUtils.getResponse(METHOD_URI + "groups.getById",
                    "group_id=" + groupId,
                    "v=" + API_VERSION);
            var responseGroupObject = getJsonArrayFromResponse(response).get(0).getAsJsonObject();
            var imageUri = responseGroupObject.get("photo_50").getAsString();
            var chatTitle = responseGroupObject.get("name").getAsString();
            var chat = new VKChat(accessToken, imageUri, chatTitle, messageObject.get("user_id").getAsInt());
            VKMessage message = createMessageFromJsonObject(messageObject);
            chat.getMessages().addLast(message);
            chats.addLast(chat);
            return true;
        }
        var chatTitle = messageObject.get("title").getAsString();
        int chatId = 0;
        int userId = 0;

        if (messageObject.has("chat_id")) {
            chatId = messageObject.get("chat_id").getAsInt();
        } else {
            userId = messageObject.get("user_id").getAsInt();
        }
        VKChat chat;

        // chatID == 0 -> message from user.
        // userId == 0 -> message from conference.
        if (chatId == 0) {
            response = WebUtils.getResponse(METHOD_URI + "users.get",
                    "user_ids=" + userId,
                    "fields=photo_50",
                    "v=" + API_VERSION);
            var userObject = getJsonArrayFromResponse(response).get(0).getAsJsonObject();
            var photoUri = userObject.get("photo_50").getAsString();
            var firstName = userObject.get("first_name").getAsString();
            var lastName = userObject.get("last_name").getAsString();
            chat = new VKChat(accessToken, photoUri, firstName + " " + lastName, chatId);
        } else {
            var photoUri = messageObject.get("photo_50").getAsString();
            chat = new VKChat(accessToken, photoUri, chatTitle, chatId);
        }
        VKMessage message = createMessageFromJsonObject(messageObject);
        chat.getMessages().addLast(message);

        for (var chatIt : chats) {
            if (chatIt.getTitle().equals(chat.getTitle())) {
                if (chatId != 0) {
                    if (((VKChat) chatIt).id == chat.id) {
                        chatIt.getMessages().addLast(message);
                        return true;
                    }
                } else {
                    if (((VKMessage) chatIt.getLastMessage()).userId == message.userId) {
                        chatIt.getMessages().addLast(message);
                        return true;
                    }
                }
            }
        }
        chats.addFirst(chat);
        return true;
    }

     static VKMessage createMessageFromJsonObject(JsonObject messageObject) {
        String messageText = messageObject.get("body").getAsString();

        if (messageText.isEmpty()) {
            if (messageObject.has("attachments")) {
                messageText = "[Attachment]";
            } else if (messageObject.has("fwd_messages")) {
                messageText = "[Forwarded message]";
            } else if (messageObject.has("action")) {
                messageText = "[Action]";
            }
        }
        var messageDate = new Date(messageObject.get("date").getAsLong() * 1000);
        int messageId = messageObject.get("id").getAsInt();
        int messageUserId = messageObject.get("user_id").getAsInt();
        boolean messageOutgoing = (messageObject.get("out").getAsInt() == 1);

        return new VKMessage(messageText, messageDate, messageId, messageUserId, messageOutgoing);
    }

    static JsonObject getJsonObjectFromResponse(String response) {
        return new JsonParser().parse(response).getAsJsonObject().get("response").getAsJsonObject();
    }

    static JsonArray getJsonArrayFromResponse(String response) {
        return new JsonParser().parse(response).getAsJsonObject().get("response").getAsJsonArray();
    }
}