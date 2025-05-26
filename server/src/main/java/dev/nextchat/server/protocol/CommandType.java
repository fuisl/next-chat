package dev.nextchat.server.protocol;

public enum CommandType {
    DELETE_USER,
    LOGIN,
    SIGNUP,
    SEND_MESSAGE,
    FETCH_RECENT,
    FETCH_BEFORE,
    SEARCH_MESSAGES,
    UNKNOWN,
    CREATE_GROUP,
    JOIN_GROUP,
    CHECK_USER_EXISTENCE,
    FETCH_GROUP_INFO,
    FETCH_GROUP_WITH_USER,
    FETCH_NEW,
    FETCH_PER_GROUP,
    SEARCH_CONVERSATION,
    LEAVE_GROUP;

    public static CommandType fromString(String type) {
        try {
            return CommandType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {

            return UNKNOWN;
        }
    }

}
