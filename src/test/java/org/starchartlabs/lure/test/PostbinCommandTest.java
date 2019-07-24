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
package org.starchartlabs.lure.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.starchartlabs.lure.CommandLineInterface;
import org.starchartlabs.lure.command.PostbinCommand;
import org.testng.Assert;
import org.testng.annotations.Test;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class PostbinCommandTest {

    private static final Path TEST_PAYLOADS_DIRECTORY = Paths.get("org", "starchartlabs", "lure", "test", "payloads",
            "postbin");

    private static final Path GET_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("recordedRequest.json");

    private static final Path POST_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("recordedPostRequest.json");

    private static final Path COMPLEX_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("recordedComplexRequest.json");

    private static final Path NO_REQUEST_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("notFoundNoRequest.json");

    private static final Path BIN_EXPIRED_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("notFoundBinDoesntExist.json");

    private static final Path CREATE_BIN_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("createBin.json");

    @Test
    public void postbinFilteredByGet() throws Exception {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(PostbinCommand.class);

        MockResponse response = new MockResponse()
                .setBody(getContent(GET_PAYLOAD))
                .setResponseCode(200);
        MockResponse responseBinComplete = new MockResponse()
                .setBody(getContent(BIN_EXPIRED_PAYLOAD))
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.enqueue(responseBinComplete);
            server.start();

            HttpUrl rootUrl = server.url("/api");
            String binId = "binId";
            String url = server.url("/webhook").toString();

            String[] args = new String[] { PostbinCommand.COMMAND_NAME, "--target-url=" + url, "--bin-id=" + binId,
                    "--postbin-root-url=" + rootUrl.toString(), "--poll-frequency=1" };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 2);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                List<String> events = testLogger.getLoggingEvents().stream()
                        .filter(event -> Level.INFO.equals(event.getLevel()))
                        .map(LoggingEvent::getMessage)
                        .collect(Collectors.toList());

                Assert.assertTrue(
                        events.contains(
                                "Ignoring request, GitHub webhooks are expected to always operate via POST operations"),
                        "Found: " + events);
            }
        }
    }

    @Test
    public void postbin() throws Exception {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(PostbinCommand.class);

        MockResponse response = new MockResponse()
                .setBody(getContent(POST_PAYLOAD))
                .setResponseCode(200);
        MockResponse postResponse = new MockResponse()
                .setResponseCode(200);
        MockResponse responseBinComplete = new MockResponse()
                .setBody(getContent(BIN_EXPIRED_PAYLOAD))
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.enqueue(postResponse);
            server.enqueue(responseBinComplete);
            server.start();

            HttpUrl rootUrl = server.url("/api");
            String binId = "binId";
            String url = server.url("/webhook").toString();

            String[] args = new String[] { PostbinCommand.COMMAND_NAME, "--target-url=" + url, "--bin-id=" + binId,
                    "--postbin-root-url=" + rootUrl.toString(), "--poll-frequency=1" };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 3);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "POST");
                Assert.assertEquals(request.getHeader("Content-Type"), "application/json; charset=utf-8");
                Assert.assertEquals(request.getHeader("User-Agent"), "curl/7.35.0");
                Assert.assertEquals(request.getPath(), "/webhook");
                Assert.assertEquals(request.getBody().readUtf8(), "{\"name\":\"value\"}");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                List<String> events = testLogger.getLoggingEvents().stream()
                        .filter(event -> Level.INFO.equals(event.getLevel()))
                        .map(LoggingEvent::getMessage)
                        .collect(Collectors.toList());

                Assert.assertTrue(events.contains("Successful POST of event to {} (HTTP: {}: {})"),
                        "Found: " + events);
            }
        }
    }

    @Test
    public void postbinComplexRequestPreservedSignature() throws Exception {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(PostbinCommand.class);

        MockResponse response = new MockResponse()
                .setBody(getContent(COMPLEX_PAYLOAD))
                .setResponseCode(200);
        MockResponse postResponse = new MockResponse()
                .setResponseCode(200);
        MockResponse responseBinComplete = new MockResponse()
                .setBody(getContent(BIN_EXPIRED_PAYLOAD))
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.enqueue(postResponse);
            server.enqueue(responseBinComplete);
            server.start();

            HttpUrl rootUrl = server.url("/api");
            String binId = "binId";
            String url = server.url("/webhook").toString();

            String[] args = new String[] { PostbinCommand.COMMAND_NAME, "--target-url=" + url, "--bin-id=" + binId,
                    "--postbin-root-url=" + rootUrl.toString(), "--poll-frequency=1" };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 3);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                String originalSignature = request.getHeader("X-Hub-Signature");

                Assert.assertEquals(request.getMethod(), "POST");
                Assert.assertEquals(request.getHeader("Content-Type"), "application/json; charset=utf-8");
                Assert.assertEquals(request.getHeader("User-Agent"), "GitHub-Hookshot/634bbb7");
                Assert.assertEquals(originalSignature, "sha1=fea260b8b5179f781a111e91a0920c6957eaf0e8");
                Assert.assertEquals(request.getPath(), "/webhook");

                String body = request.getBody().readUtf8();

                // This test is about the HMAC matching, not the body (that is covered by simpler tests)
                Assert.assertNotNull(body);

                // Secret was input as part of generating request saved to file
                String sentPayloadSignature = getPayloadSignature(body, "12345");
                Assert.assertEquals(sentPayloadSignature, originalSignature);

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                List<String> events = testLogger.getLoggingEvents().stream()
                        .filter(event -> Level.INFO.equals(event.getLevel()))
                        .map(LoggingEvent::getMessage)
                        .collect(Collectors.toList());

                Assert.assertTrue(events.contains("Successful POST of event to {} (HTTP: {}: {})"),
                        "Found: " + events);
            }
        }
    }

    @Test
    public void postbinCreateBin() throws Exception {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(PostbinCommand.class);

        MockResponse createResponse = new MockResponse()
                .setBody(getContent(CREATE_BIN_PAYLOAD))
                .setResponseCode(200);
        MockResponse response = new MockResponse()
                .setBody(getContent(POST_PAYLOAD))
                .setResponseCode(200);
        MockResponse postResponse = new MockResponse()
                .setResponseCode(200);
        MockResponse responseBinComplete = new MockResponse()
                .setBody(getContent(BIN_EXPIRED_PAYLOAD))
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(createResponse);
            server.enqueue(response);
            server.enqueue(postResponse);
            server.enqueue(responseBinComplete);
            server.start();

            HttpUrl rootUrl = server.url("/api");
            String binId = "YS4il4gS";
            String url = server.url("/webhook").toString();

            String[] args = new String[] { PostbinCommand.COMMAND_NAME, "--target-url=" + url,
                    "--postbin-root-url=" + rootUrl.toString(), "--poll-frequency=1" };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 4);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "POST");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "POST");
                Assert.assertEquals(request.getHeader("Content-Type"), "application/json; charset=utf-8");
                Assert.assertEquals(request.getHeader("User-Agent"), "curl/7.35.0");
                Assert.assertEquals(request.getPath(), "/webhook");
                Assert.assertEquals(request.getBody().readUtf8(), "{\"name\":\"value\"}");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                List<String> events = testLogger.getLoggingEvents().stream()
                        .filter(event -> Level.INFO.equals(event.getLevel()))
                        .map(LoggingEvent::getMessage)
                        .collect(Collectors.toList());

                Assert.assertTrue(events.contains("Successful POST of event to {} (HTTP: {}: {})"),
                        "Found: " + events);
            }
        }
    }

    @Test
    public void postbinNoRequest() throws Exception {
        MockResponse response = new MockResponse()
                .setBody(getContent(NO_REQUEST_PAYLOAD))
                .setResponseCode(404);
        MockResponse responseBinComplete = new MockResponse()
                .setBody(getContent(BIN_EXPIRED_PAYLOAD))
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.enqueue(responseBinComplete);
            server.start();

            HttpUrl rootUrl = server.url("/api");
            String binId = "binId";
            String url = server.url("/webhook").toString();

            String[] args = new String[] { PostbinCommand.COMMAND_NAME, "--target-url=" + url, "--bin-id=" + binId,
                    "--postbin-root-url=" + rootUrl.toString(), "--poll-frequency=1" };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 2);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");

                request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertEquals(request.getMethod(), "GET");
                Assert.assertEquals(request.getHeader("Accept"), "application/json");
                Assert.assertEquals(request.getHeader("User-Agent"), "StarChart-Labs/lure");
                Assert.assertEquals(request.getPath(), "/api/bin/" + binId + "/req/shift");
            }
        }
    }

    private String getContent(Path filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(filePath.toString()), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    private String getPayloadSignature(String payload, String webhookSecret) {
        Objects.requireNonNull(payload);
        Objects.requireNonNull(webhookSecret);

        HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, webhookSecret);

        return "sha1=" + hmacUtils.hmacHex(payload);
    }

}
