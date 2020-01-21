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

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starchartlabs.lure.model.postbin.PostbinCreateResponse;
import org.starchartlabs.lure.model.postbin.PostbinShiftResponse;
import org.starchartlabs.lure.model.postbin.RecordedRequest;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * Command line sub-command that will pull requests from PostBin and push it to a web URL following patterns defined for
 * GitHub webhook calls
 *
 * @author romeara
 * @since 0.2.0
 */
@Command(
        description = "Pipes a payloads from postb.in to an indicated web URL, mimicking the GitHub webook call pattern",
        name = PostbinCommand.COMMAND_NAME, mixinStandardHelpOptions = true)
public class PostbinCommand implements Runnable {

    public static final String COMMAND_NAME = "postbin";

    private static final String USER_AGENT = "StarChart-Labs/lure";

    private static final Set<String> PASSED_HEADERS = Stream
            .of("X-GitHub-Event", "X-GitHub-Delivery", "X-Hub-Signature", "User-Agent")
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    /** Logger reference to output information to the application log files */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OkHttpClient httpClient = new OkHttpClient();

    @ParentCommand
    private LureCommand lureCommand;

    @Option(names = { "-t", "--target-url" }, required = true,
            description = "Specifies the server URL to POST the event to. Required")
    private String targetUrl;

    @Option(names = { "-b", "--bin-id" }, required = false,
            description = "Specifies the Postbin bin ID to poll. If unspecified, a new bin is created")
    private String binId;

    @Option(names = { "-p", "--poll-frequency" }, required = false,
            description = "Specifies the frequency in seconds to poll Postbin for new requests at, must be greater than 0. Defaults to 10 seconds")
    private int pollFrequencySeconds = 10;

    @Option(names = { "-r", "--postbin-root-url" }, required = false,
            description = "Specifies the root URL of Postbin to use. Defaults to https://postb.in/api")
    private String rootUrl = "https://postb.in/api";

    @Override
    public void run() {
        logger.info("Polling PostBin requests");
        long millisecondsWait = TimeUnit.MILLISECONDS.convert(pollFrequencySeconds, TimeUnit.SECONDS);
        PostbinShiftResponse currentResponse = null;

        String effectiveBiId = getBinId(binId);
        String sourceUrl = getShiftRequestUrl(effectiveBiId);

        logger.info("Reading requests from {}", sourceUrl);

        while (currentResponse == null || currentResponse.isBinExists()) {
            currentResponse = getResponse(sourceUrl);

            currentResponse.getRecordedRequest()
            .filter(rr -> Objects.equals(rr.getMethod(), "POST"))
            .ifPresent(request -> postRequest(targetUrl, request));

            boolean postRequest = Objects.equals("POST",
                    currentResponse.getRecordedRequest().map(RecordedRequest::getMethod).orElse("POST"));

            if (!postRequest) {
                logger.info("Ignoring request, GitHub webhooks are expected to always operate via POST operations");
            }

            try {
                Thread.sleep(millisecondsWait);
            } catch (InterruptedException e) {
                logger.error("Postbin polling interrupted", e);
                break;
            }
        }

        logger.info("Polling complete, Bin is no longer active");
    }

    private String getBinId(@Nullable String providedBinId) {
        String result = providedBinId;

        if (providedBinId == null) {
            HttpUrl url = HttpUrl.get(rootUrl).newBuilder()
                    .addPathSegment("bin")
                    .build();

            Request request = new Request.Builder()
                    .header("User-Agent", USER_AGENT)
                    .post(RequestBody.create(null, new byte[] {}))
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                try (ResponseBody body = response.body()) {
                    PostbinCreateResponse postbinResponse = PostbinCreateResponse.fromJson(body.string());

                    logger.info(
                            "Created bin viewable at https://postb.in/b/{} for routing - configure webhooks to POST to https://postb.in/{}",
                            postbinResponse.getBinId(), postbinResponse.getBinId());

                    return postbinResponse.getBinId();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error requesting data from PostBin", e);
            }
        }

        return result;
    }

    private String getShiftRequestUrl(String binId) {
        Objects.requireNonNull(binId);

        return HttpUrl.get(rootUrl).newBuilder()
                .addPathSegment("bin")
                .addPathSegment(binId)
                .addPathSegments("req/shift")
                .build()
                .toString();
    }

    private PostbinShiftResponse getResponse(String url) {
        Objects.requireNonNull(url);

        Request request = new Request.Builder()
                .get()
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            PostbinShiftResponse postbinResponse = PostbinShiftResponse.fromResponse(response);
            if (postbinResponse.isBinExists()) {
                logger.info("Polled Postbin - Request {}",
                        (postbinResponse.getRecordedRequest().isPresent() ? "present" : "not present"));
            }

            return postbinResponse;
        } catch (IOException e) {
            throw new RuntimeException("Error requesting data from PostBin", e);
        }
    }

    private void postRequest(String url, RecordedRequest request) {
        Objects.requireNonNull(request);

        String contentType = request.getHeaders().stream()
                .filter(header -> Objects.equals(header.getKey(), "Content-Type"))
                .map(Entry::getValue)
                .findAny()
                .orElse("application/json");

        Request.Builder requestBuilder = new Request.Builder()
                .post(RequestBody.create(MediaType.get(contentType), request.getBody()))
                .url(url);

        // Only pass on GitHub headers to avoid posting errors from "host" and other such values
        request.getHeaders().stream()
        .filter(header -> !Objects.equals(header.getKey(), "Content-Type"))
        .filter(header -> PASSED_HEADERS.contains(header.getKey().toLowerCase()))
        .forEach(header -> requestBuilder.addHeader(header.getKey(), header.getValue()));

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (response.isSuccessful()) {
                logger.info("Successful POST of event to {} (HTTP: {}: {})", url, response.code(), response.message());
            } else {
                throw new RuntimeException(
                        "Request to " + targetUrl + " unsuccessful (" + response.code() + " - " + response.message()
                        + ")");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error executing request to " + targetUrl, e);
        }
    }

}
