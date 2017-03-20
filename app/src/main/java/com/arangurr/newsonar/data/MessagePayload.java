package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;
import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Created by Rodrigo on 20/03/2017.
 */

public abstract class MessagePayload implements Serializable {

    static final Gson gson = new Gson();

    public MessagePayload() {
    }

    public static MessagePayload fromString(String string) throws JsonSyntaxException {
        return gson.fromJson(string, MessagePayload.class);
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public static Message newMessage(MessagePayload messagePayload, String type) {
        return new Message(messagePayload.toString().getBytes(StandardCharsets.UTF_8),
                Constants.NAMESPACE, type);
    }

}
