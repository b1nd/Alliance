package com.buzulukov.alliance.api.messengers;

import java.util.LinkedList;

/**
 * All messengers must implement this interface.
 * All methods must be implemented properly.
 * For each Messenger you must create 2 more classes of Chat and Message.
 */
public interface Messenger {

    /**
     * Gives name of messenger.
     *
     * @return Messenger name.
     */
    String getName();

    /**
     * Login with required params.
     *
     * @param params required params for login.
     * @return Is login successful.
     */
    boolean login(String... params);

    /**
     * Logout from account.
     */
    void logout();

    /**
     * If account is authorised in this messenger.
     *
     * @return Is account authorised.
     */
    boolean isAuthorized();

    /**
     * Gives information about account.
     *
     * @return Short information about account. e.g. first name + last name.
     */
    String getAccountInfo();

    /**
     * Gives all chats for this account.
     *
     * @return LinkedList<Chat> of all chats.
     */
    LinkedList<Chat> getChats();

    /**
     * Update all chats.
     *
     * @return Are chats updated.
     */
    boolean updateChats();

}
