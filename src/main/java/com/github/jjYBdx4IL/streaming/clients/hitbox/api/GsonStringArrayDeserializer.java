package com.github.jjYBdx4IL.streaming.clients.hitbox.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Flexible deserializer for String[]. Allows String[] with only one element to be written as a simple string.
 * Null values get transformed to an empty list.
 *
 * @author Github jjYBdx4IL Projects
 */
public class GsonStringArrayDeserializer implements JsonDeserializer<String[]> {

    @Override
    public String[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return new String[]{};
        }
        if (json.isJsonPrimitive()) {
            return new String[]{json.getAsString()};
        }
        if (json.isJsonObject()) {
            throw new JsonParseException("exptected String[], found JsonObject");
        }
        JsonArray arr = json.getAsJsonArray();
        String[] result = new String[arr.size()];
        int i = 0;
        for (JsonElement el : arr) {
            result[i] = el.getAsString();
            i++;
        }
        return result;
    }

}
