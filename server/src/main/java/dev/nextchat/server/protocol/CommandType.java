package dev.nextchat.server.protocol;

public enum CommandType {
    LOGIN,
    SIGNUP,
    SEND_MESSAGE,
    EDIT_MESSAGE,
    DELETE_MESSAGE,
    PIN_MESSAGE,
    FETCH_RECENT,
    FETCH_BEFORE,
    SEARCH_MESSAGES,
    UNKNOWN,
    CREATE_GROUP,
    JOIN_GROUP,
    LEAVE_GROUP;

    public static CommandType fromString(String type) {
        try {
            return CommandType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}