package net.ponec.x2j.model;
import org.ujorm.tools.Assert;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Pavel Ponec
 */
public class Message {

    private final String text;

    private final boolean error;

    public Message(String text, boolean error) {
        this.text = text;
        this.error = error;
    }

    public String getText() {
        return text;
    }

    public boolean isError() {
        return error;
    }

    public static Message of(@NotNull final String text) {
        return new Message(Assert.notNull(text, "text"), false);
    }

    public static Message of(@NotNull Throwable e) {
        String text = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        return new Message(text, true);
    }

    @Override
    public String toString() {
        return getText();
    }

}
