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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.starchartlabs.lure.CommandLineInterface;
import org.starchartlabs.lure.command.PushCommand;
import org.testng.Assert;
import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class PushCommandTest {

    private static final Path TEST_PAYLOADS_DIRECTORY = Paths.get("org/starchartlabs/lure/test/payloads");

    private static final Path BASIC_JSON_PAYLOAD = TEST_PAYLOADS_DIRECTORY.resolve("basicJson.json");

    @Test
    public void pushNoSecret() throws Exception {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(PushCommand.class);

        String eventName = "test-event";
        String contentPath = getClass().getClassLoader().getResource(BASIC_JSON_PAYLOAD.toString()).toURI()
                .getPath();

        MockResponse response = new MockResponse()
                .setResponseCode(200);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.start();

            String url = server.url("/webhook").toString();

            String[] args = new String[] { PushCommand.COMMAND_NAME, "--target-url=" + url, "--event-name=" + eventName,
                    "--content=" + contentPath };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 1);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertTrue(request.getHeader("User-Agent").startsWith("GitHub-Hookshot/"));
                Assert.assertEquals(request.getHeader("X-GitHub-Event"), eventName);
                Assert.assertNotNull(request.getHeader("X-GitHub-Delivery"));
                Assert.assertNull(request.getHeader("X-Hub-Signature"));
                Assert.assertEquals(request.getBody().readUtf8(), "{ \"json\": \"json\" }");
                Assert.assertEquals(request.getPath(), "/webhook");

                List<String> events = testLogger.getLoggingEvents().stream()
                        .filter(event -> Level.INFO.equals(event.getLevel()))
                        .map(LoggingEvent::getMessage)
                        .collect(Collectors.toList());

                Assert.assertTrue(events.contains("Successful POST of {} event to {} (HTTP: {}: {})"),
                        "Found: " + events);
            }
        }
    }

    @Test
    public void pushWithSecret() throws Exception {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(PushCommand.class);
        String expectedSignature = "sha1=24510be1a28ed09a521e5929842ca47ebc05b414";

        String eventName = "test-event";
        String contentPath = getClass().getClassLoader().getResource(BASIC_JSON_PAYLOAD.toString()).toURI()
                .getPath();

        MockResponse response = new MockResponse()
                .setResponseCode(200);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.start();

            String url = server.url("/webhook").toString();

            String[] args = new String[] { PushCommand.COMMAND_NAME, "--target-url=" + url, "--event-name=" + eventName,
                    "--secret=12345", "--content=" + contentPath };

            try {
                CommandLineInterface.main(args);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 1);
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

                Assert.assertTrue(request.getHeader("User-Agent").startsWith("GitHub-Hookshot/"));
                Assert.assertEquals(request.getHeader("X-GitHub-Event"), eventName);
                Assert.assertNotNull(request.getHeader("X-GitHub-Delivery"));
                Assert.assertEquals(request.getHeader("X-Hub-Signature"), expectedSignature);
                Assert.assertEquals(request.getBody().readUtf8(), "{ \"json\": \"json\" }");
                Assert.assertEquals(request.getPath(), "/webhook");

                List<String> events = testLogger.getLoggingEvents().stream()
                        .filter(event -> Level.INFO.equals(event.getLevel()))
                        .map(LoggingEvent::getMessage)
                        .collect(Collectors.toList());

                Assert.assertTrue(events.contains("Successful POST of {} event to {} (HTTP: {}: {})"),
                        "Found: " + events);
            }
        }
    }

}
