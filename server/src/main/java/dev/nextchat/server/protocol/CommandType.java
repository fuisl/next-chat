package dev.nextchat.server.protocol;

public enum CommandType {
    CHECK_USER_EXISTENCE,
    CREATE_GROUP,
    DELETE_USER,
    FETCH_BEFORE,
    FETCH_GROUP_INFO,
    FETCH_GROUP_WITH_USER,
    FETCH_NEW,
    FETCH_PER_GROUP,
    FETCH_RECENT,
    JOIN_GROUP,
    LEAVE_GROUP,
    LOGIN,
    RENAME_GROUP,
    SEARCH_CONVERSATION,
    SEARCH_MESSAGES,
    SEND_MESSAGE,
    SIGNUP,
    UNKNOWN;

    public static CommandType fromString(String type) {
        try {
            return CommandType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {

            return UNKNOWN;
        }
    }

}
