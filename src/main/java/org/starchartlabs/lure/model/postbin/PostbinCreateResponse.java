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

import java.util.Objects;

import javax.annotation.Nullable;

import org.starchartlabs.alloy.core.MoreObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents a response from the <a href="https://postb.in/api/#create-bin">postb.in "create" API</a>
 *
 * @author romeara
 * @since 0.2.0
 */
public class PostbinCreateResponse {

    private static final Gson GSON = new GsonBuilder().create();

    private final String binId;

    /**
     * @param binId
     *            PostBin identifier of the Bin created
     * @since 0.2.0
     */
    public PostbinCreateResponse(String binId) {
        this.binId = Objects.requireNonNull(binId);
    }

    /**
     * @return PostBin identifier of the Bin created
     * @since 0.2.0
     */
    public String getBinId() {
        return binId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBinId());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof PostbinCreateResponse) {
            PostbinCreateResponse compare = (PostbinCreateResponse) obj;

            result = Objects.equals(compare.getBinId(), getBinId());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("binId", getBinId())
                .toString();
    }

    public static PostbinCreateResponse fromJson(String json) {
        Objects.requireNonNull(json);

        return GSON.fromJson(json, PostbinCreateResponse.class);
    }

}
