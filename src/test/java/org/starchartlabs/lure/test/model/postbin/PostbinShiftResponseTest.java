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
package org.starchartlabs.lure.test.model.postbin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.starchartlabs.lure.model.postbin.PostbinShiftResponse;
import org.starchartlabs.lure.model.postbin.RecordedRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class PostbinShiftResponseTest {

    private static final Path TEST_PAYLOAD_DIRECTORY = Paths.get("org", "starchartlabs", "lure", "test", "payloads",
            "postbin");

    private final OkHttpClient httpClient = new OkHttpClient();

    @Test(expectedExceptions = RuntimeException.class)
    public void fromResponse500() throws Exception {
        MockResponse response = new MockResponse()
                .setResponseCode(500);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.start();

            HttpUrl url = server.url("/shift");

            Request request = new Request.Builder()
                    .get()
                    .header("Accept", "application/json")
                    .url(url)
                    .build();

            try (Response res = httpClient.newCall(request).execute()) {
                PostbinShiftResponse.fromResponse(res);
            } finally {
                Assert.assertEquals(server.getRequestCount(), 1);
            }
        }
    }


    @Test
    public void fromResponse404BinDoesntExist() throws Exception {
        String payload = null;

        try (BufferedReader reader = getClasspathReader(
                TEST_PAYLOAD_DIRECTORY.resolve("notFoundBinDoesntExist.json"))) {
            payload = reader.lines()
                    .collect(Collectors.joining("\n"));
        }

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(payload)
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.start();

            HttpUrl url = server.url("/shift");

            Request request = new Request.Builder()
                    .get()
                    .header("Accept", "application/json")
                    .url(url)
                    .build();

            try (Response res = httpClient.newCall(request).execute()) {
                PostbinShiftResponse result = PostbinShiftResponse.fromResponse(res);

                Assert.assertNotNull(result);
                Assert.assertFalse(result.getRecordedRequest().isPresent());
                Assert.assertFalse(result.isBinExists());
            } finally {
                Assert.assertEquals(server.getRequestCount(), 1);
            }
        }
    }

    @Test
    public void fromResponse404NoRequest() throws Exception {
        String payload = null;

        try (BufferedReader reader = getClasspathReader(
                TEST_PAYLOAD_DIRECTORY.resolve("notFoundNoRequest.json"))) {
            payload = reader.lines()
                    .collect(Collectors.joining("\n"));
        }

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(payload)
                .setResponseCode(404);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.start();

            HttpUrl url = server.url("/shift");

            Request request = new Request.Builder()
                    .get()
                    .header("Accept", "application/json")
                    .url(url)
                    .build();

            try (Response res = httpClient.newCall(request).execute()) {
                PostbinShiftResponse result = PostbinShiftResponse.fromResponse(res);

                Assert.assertNotNull(result);
                Assert.assertFalse(result.getRecordedRequest().isPresent());
                Assert.assertTrue(result.isBinExists());
            } finally {
                Assert.assertEquals(server.getRequestCount(), 1);
            }
        }
    }

    @Test
    public void fromResponse200() throws Exception {
        String expectedMethod = "GET";

        List<Entry<String, String>> expectedHeaders = new ArrayList<>();
        expectedHeaders.add(new AbstractMap.SimpleEntry<>("user-agent", "curl/7.35.0"));
        expectedHeaders.add(new AbstractMap.SimpleEntry<>("host", "postb.in"));
        expectedHeaders.add(new AbstractMap.SimpleEntry<>("accept", "*/*"));

        String expectedBody = "{\"name\":\"value\"}";

        RecordedRequest expectedRequest = new RecordedRequest(expectedMethod, expectedHeaders, expectedBody);
        String payload = null;

        try (BufferedReader reader = getClasspathReader(
                TEST_PAYLOAD_DIRECTORY.resolve("recordedRequest.json"))) {
            payload = reader.lines()
                    .collect(Collectors.joining("\n"));
        }

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(payload)
                .setResponseCode(200);

        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(response);
            server.start();

            HttpUrl url = server.url("/shift");

            Request request = new Request.Builder()
                    .get()
                    .header("Accept", "application/json")
                    .url(url)
                    .build();

            try (Response res = httpClient.newCall(request).execute()) {
                PostbinShiftResponse result = PostbinShiftResponse.fromResponse(res);

                Assert.assertNotNull(result);
                Assert.assertTrue(result.getRecordedRequest().isPresent());
                Assert.assertEquals(result.getRecordedRequest().get(), expectedRequest);
                Assert.assertTrue(result.isBinExists());
            } finally {
                Assert.assertEquals(server.getRequestCount(), 1);
            }
        }
    }

    private BufferedReader getClasspathReader(Path filePath) {
        return new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filePath.toString()),
                        StandardCharsets.UTF_8));
    }

}
