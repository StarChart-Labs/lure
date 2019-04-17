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
package org.starchartlabs.lure.command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//TODO romeara test
/**
 * Command line sub-command that will push raw data to a web URL following patterns defined for GitHub webhook calls
 *
 * @author romeara
 */
public class PushCommand implements Runnable {

    public static final String COMMAND_NAME = "push";

    // Name of the event type that triggered the delivery.
    private static final String EVENT_NAME_HEADER = "X-GitHub-Event";

    // A GUID to identify the delivery.
    private static final String DELIVERY_HEADER = "X-GitHub-Delivery";

    // The HMAC hex digest of the response body. This header will be sent if the command is provided a secret
    private static final String SECURITY_HEADER = "X-Hub-Signature";

    private static final String USER_AGENT_PREFIX = "GitHub-Hookshot/";

    /** Logger reference to output information to the application log files */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Option(name = "-t", aliases = { "--target-url" }, required = true,
            usage = "Specifies the server URL to POST the event to. Required")
    private String targetUrl;

    @Option(name = "-e", aliases = { "--event-name" }, required = true,
            usage = "Specifies the GitHub event name to send as. Required")
    private String eventName;

    @Nullable
    @Option(name = "-s", aliases = { "--secret" }, required = false,
    usage = "Specifies the webhook secret to secure the event post with")
    private String webhookSecret;

    @Option(name = "-c", aliases = { "--content" }, required = true,
            usage = "Specifies the file containing the event content to send. Required")
    private File contentFile;

    @Override
    public void run() {
        try {
            String payload = Files.lines(contentFile.toPath(), StandardCharsets.UTF_8)
                    .collect(Collectors.joining("\n"));

            OkHttpClient httpClient = new OkHttpClient();
            HttpUrl url = HttpUrl.parse(targetUrl);

            Request request = createRequest(url, payload);

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("Successful POST of {} event to {} (HTTP: {}: {})", eventName, targetUrl,
                            response.code(), response.message());
                } else {
                    throw new RuntimeException(
                            "Request unsuccessful (" + response.code() + " - " + response.message() + ")");
                }
            } catch (IOException e) {
                throw new RuntimeException("Error executing request to " + targetUrl, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file at " + contentFile.getAbsolutePath(), e);
        }
    }

    private Request createRequest(HttpUrl url, String payload) {
        UUID deliveryId = UUID.randomUUID();

        Request.Builder requestBuilder = new Request.Builder()
                .post(RequestBody.create(MediaType.get("application/json"), payload))
                .header("User-Agent", USER_AGENT_PREFIX + "Lure")
                .header(EVENT_NAME_HEADER, eventName)
                .header(DELIVERY_HEADER, deliveryId.toString())
                .url(url);

        getSignatureHeader(payload).ifPresent(value -> requestBuilder.header(SECURITY_HEADER, value));

        return requestBuilder.build();
    }

    private Optional<String> getSignatureHeader(String payload) {
        Objects.requireNonNull(payload);

        String result = null;

        if (webhookSecret != null) {
            HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, webhookSecret);

            result = hmacUtils.hmacHex(payload);
        }

        return Optional.ofNullable(result);
    }

}
