package dev.nextchat.server.protocol;

public enum CommandType {
    CREATE_GROUP,
    FETCH_BEFORE,
    FETCH_GROUP_INFO,
    FETCH_NEW,
    FETCH_RECENT,
    JOIN_GROUP,
    LEAVE_GROUP,
    LOGIN,
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
