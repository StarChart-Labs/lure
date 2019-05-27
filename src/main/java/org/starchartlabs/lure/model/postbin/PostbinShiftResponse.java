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

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.starchartlabs.alloy.core.MoreObjects;
import org.starchartlabs.alloy.core.Strings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Represents a response from the <a href="https://postb.in/api/#shift-req">postb.in "shift" API</a>
 *
 * <p>
 * Represents a present request, a lack of available recorded request, and the possibility of the BIN no longer existing
 *
 * @author romeara
 * @since 0.2.0
 */
public class PostbinShiftResponse {

    private static final Gson GSON = new GsonBuilder().create();

    private static final String BIN_DOESNT_EXIST_MESSAGE = "Bin Does Not Exist";

    private final Optional<RecordedRequest> recordedRequest;

    private final boolean binExists;

    /**
     * @param recordedRequest
     *            The retrieved request, if available. Null if no request is present, or the bin doesn't exist
     * @param binExists
     *            True if the bin referenced is still active, false otherwise
     */
    private PostbinShiftResponse(@Nullable RecordedRequest recordedRequest, boolean binExists) {
        this.recordedRequest = Optional.ofNullable(recordedRequest);
        this.binExists = binExists;
    }

    /**
     * @return The retrieved request, if available. Empty if no request is present, or the bin doesn't exist
     * @since 0.2.0
     */
    public Optional<RecordedRequest> getRecordedRequest() {
        return recordedRequest;
    }

    /**
     * @return True if the bin referenced is still active, false otherwise
     * @since 0.2.0
     */
    public boolean isBinExists() {
        return binExists;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRecordedRequest(),
                isBinExists());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof PostbinShiftResponse) {
            PostbinShiftResponse compare = (PostbinShiftResponse) obj;

            result = Objects.equals(compare.getRecordedRequest(), getRecordedRequest())
                    && Objects.equals(compare.isBinExists(), isBinExists());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("recordedRequest", getRecordedRequest())
                .add("binExists", isBinExists())
                .toString();
    }

    /**
     * Parses an OkHttp {@link Response}, determining if the postb.in "Bin" called is still active, and if so if there
     * was a request available to read
     *
     * @param response
     *            The OkHttp response to process
     * @return A representation of the state of the called "Bin" and any recorded request it contained
     * @since 0.2.0
     */
    public static PostbinShiftResponse fromResponse(Response response) {
        Objects.requireNonNull(response);

        PostbinShiftResponse result = null;

        if (response.code() == 200) {
            try (ResponseBody body = response.body()) {
                RecordedRequest recordedRequest = RecordedRequest.fromJson(body.string());

                result = new PostbinShiftResponse(recordedRequest, true);
            } catch (IOException e) {
                throw new RuntimeException("Error reading recorded response from PostBin", e);
            }
        } else if (response.code() == 404) {
            try (ResponseBody body = response.body()) {
                ErrorResponse error = GSON.fromJson(body.string(), ErrorResponse.class);

                if (Objects.equals(BIN_DOESNT_EXIST_MESSAGE, error.getMsg())) {
                    result = new PostbinShiftResponse(null, false);
                } else {
                    result = new PostbinShiftResponse(null, true);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading error response from PostBin", e);
            }
        } else {
            throw new RuntimeException(Strings.format("Error reading recorded requests from PostBin (%s: %s)",
                    response.code(), response.message()));
        }

        return result;
    }

    /**
     * Represents response contents when an error occurred
     *
     * @author romeara
     */
    private static final class ErrorResponse {

        @SerializedName("msg")
        private final String msg;

        // Suppressed as this is invoked by the JSON deserializer
        @SuppressWarnings("unused")
        public ErrorResponse(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

    }

}
