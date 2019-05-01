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
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.starchartlabs.lure.model.postbin.RecordedRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RecordedRequestTest {

    private static final List<Entry<String, String>> HEADERS = Collections
            .singletonList(new AbstractMap.SimpleEntry<>("key", "value"));

    private static final Path TEST_PAYLOAD = Paths.get("org", "starchartlabs", "lure", "test", "payloads",
            "postbin", "recordedRequest.json");

    private String recordedResponsePayload;

    @BeforeClass
    public void setup() throws Exception {
        try (BufferedReader reader = getClasspathReader(TEST_PAYLOAD)) {
            recordedResponsePayload = reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromJsonNullJson() throws Exception {
        RecordedRequest.fromJson(null);
    }

    @Test
    public void fromJson() throws Exception {
        String expectedMethod = "GET";

        List<Entry<String, String>> expectedHeaders = new ArrayList<>();
        expectedHeaders.add(new AbstractMap.SimpleEntry<>("user-agent", "curl/7.35.0"));
        expectedHeaders.add(new AbstractMap.SimpleEntry<>("host", "postb.in"));
        expectedHeaders.add(new AbstractMap.SimpleEntry<>("accept", "*/*"));

        String expectedBody = "{\"name\":\"value\"}";

        RecordedRequest result = RecordedRequest.fromJson(recordedResponsePayload);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getMethod(), expectedMethod);
        Assert.assertEquals(result.getHeaders().size(), expectedHeaders.size());
        Assert.assertTrue(result.getHeaders().containsAll(expectedHeaders));
        Assert.assertEquals(result.getBody(), expectedBody);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void constructNullMethod() throws Exception {
        new RecordedRequest(null, HEADERS, "body");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void constructNullHeaders() throws Exception {
        new RecordedRequest("method", null, "body");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void constructNullBody() throws Exception {
        new RecordedRequest("method", HEADERS, null);
    }

    @Test
    public void getTest() throws Exception {
        RecordedRequest result = new RecordedRequest("method", HEADERS, "body");

        Assert.assertEquals(result.getMethod(), "method");
        Assert.assertEquals(result.getHeaders(), HEADERS);
        Assert.assertEquals(result.getBody(), "body");
    }

    @Test
    public void hashCodeEqualWhenDataEqual() throws Exception {
        RecordedRequest result1 = new RecordedRequest("method", HEADERS, "body");
        RecordedRequest result2 = new RecordedRequest("method", HEADERS, "body");

        Assert.assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    public void equalsNull() throws Exception {
        RecordedRequest result = new RecordedRequest("method", HEADERS, "body");

        Assert.assertFalse(result.equals(null));
    }

    @Test
    public void equalsDifferentClass() throws Exception {
        RecordedRequest result = new RecordedRequest("method", HEADERS, "body");

        Assert.assertFalse(result.equals("string"));
    }

    @Test
    public void equalsSelf() throws Exception {
        RecordedRequest result = new RecordedRequest("method", HEADERS, "body");

        Assert.assertTrue(result.equals(result));
    }

    @Test
    public void equalsDifferentData() throws Exception {
        RecordedRequest result1 = new RecordedRequest("method", HEADERS, "body1");
        RecordedRequest result2 = new RecordedRequest("method", HEADERS, "body2");

        Assert.assertFalse(result1.equals(result2));
    }

    @Test
    public void equalsSameData() throws Exception {
        RecordedRequest result1 = new RecordedRequest("method", HEADERS, "body");
        RecordedRequest result2 = new RecordedRequest("method", HEADERS, "body");

        Assert.assertTrue(result1.equals(result2));
    }

    @Test
    public void toStringTest() throws Exception {
        RecordedRequest obj = new RecordedRequest("method", HEADERS, "body");

        String result = obj.toString();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("method=method"));
        Assert.assertTrue(result.contains("headers=" + HEADERS.toString()));
        Assert.assertTrue(result.contains("body=body"));
    }

    private BufferedReader getClasspathReader(Path filePath) {
        return new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filePath.toString()),
                        StandardCharsets.UTF_8));
    }

}
