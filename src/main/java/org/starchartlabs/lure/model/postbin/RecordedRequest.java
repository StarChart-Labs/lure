/*
 * Copyright 2019 StarChart-Labs Contributors (https://github.com/StarChart-Labs)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.starchartlabs.lure.model.postbin;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.starchartlabs.alloy.core.MoreObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Represents a response describing a request captured by postb.in
 *
 * @author romeara
 * @since 0.2.0
 */
public class RecordedRequest {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(RecordedRequest.class, new Deserializer())
            .create();

    private final String method;

    private final List<Entry<String, String>> headers;

    private final String body;

    /**
     * @param method
     *            The HTTP method used to make the request
     * @param headers
     *            Representation of the headers recorded with the request
     * @param body
     *            The JSON body of the recorded request
     * @since 0.2.0
     */
    public RecordedRequest(String method, List<Entry<String, String>> headers, String body) {
        this.method = Objects.requireNonNull(method);
        this.headers = Objects.requireNonNull(headers);
        this.body = Objects.requireNonNull(body);
    }

    /**
     * @return The HTTP method used to make the request
     * @since 0.2.0
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return Representation of the headers recorded with the request
     * @since 0.2.0
     */
    public List<Entry<String, String>> getHeaders() {
        return headers;
    }

    /**
     * @return The JSON body of the recorded request
     * @since 0.2.0
     */
    public String getBody() {
        return body;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(),
                getHeaders(),
                getBody());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof RecordedRequest) {
            RecordedRequest compare = (RecordedRequest) obj;

            result = Objects.equals(compare.getMethod(), getMethod())
                    && Objects.equals(compare.getHeaders(), getHeaders())
                    && Objects.equals(compare.getBody(), getBody());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("method", getMethod())
                .add("headers", getHeaders())
                .add("body", getBody())
                .toString();
    }

    /**
     * Deserializes a Java representation of a request recorded by postb.in
     *
     * @param json
     *            JSON representation to parse/deserialize
     * @return Java representation of the provided JSON
     * @since 0.2.0
     */
    public static RecordedRequest fromJson(String json) {
        Objects.requireNonNull(json);

        return GSON.fromJson(json, RecordedRequest.class);
    }

    /**
     * Handles custom JSON deserialization for request data via GSON deserialization
     *
     * @author romeara
     */
    private static final class Deserializer implements JsonDeserializer<RecordedRequest> {

        private static final Gson GSON = new GsonBuilder()
                .create();

        @Override
        public RecordedRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject responseObject = json.getAsJsonObject();

            String method = responseObject.get("method").getAsString();

            List<Entry<String, String>> headers = responseObject.get("headers").getAsJsonObject().entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getAsString()))
                    .collect(Collectors.toList());

            String body = GSON.toJson(responseObject.get("body"));

            return new RecordedRequest(method, headers, body);
        }

    }

}
