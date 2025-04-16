package dev.nextchat.server.protocol;

public enum CommandType {
    LOGIN,
    SIGNUP,
    SEND_MESSAGE,
    UNKNOWN;

    public static CommandType fromString(String type) {
        try {
            return CommandType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}