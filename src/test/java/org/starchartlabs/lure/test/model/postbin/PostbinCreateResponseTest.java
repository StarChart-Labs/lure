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
import java.util.stream.Collectors;

import org.starchartlabs.lure.model.postbin.PostbinCreateResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PostbinCreateResponseTest {

    private static final Path TEST_PAYLOAD = Paths.get("org", "starchartlabs", "lure", "test", "payloads",
            "postbin", "createBin.json");

    private String payload;

    @BeforeClass
    public void setup() throws Exception {
        try (BufferedReader reader = getClasspathReader(TEST_PAYLOAD)) {
            payload = reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void fromJsonNullJson() throws Exception {
        PostbinCreateResponse.fromJson(null);
    }

    @Test
    public void fromJson() throws Exception {
        PostbinCreateResponse result = PostbinCreateResponse.fromJson(payload);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getBinId(), "YS4il4gS");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void constructNullBinId() throws Exception {
        new PostbinCreateResponse(null);
    }

    @Test
    public void getTest() throws Exception {
        PostbinCreateResponse result = new PostbinCreateResponse("binId");

        Assert.assertEquals(result.getBinId(), "binId");
    }

    @Test
    public void hashCodeEqualWhenDataEqual() throws Exception {
        PostbinCreateResponse result1 = new PostbinCreateResponse("binId");
        PostbinCreateResponse result2 = new PostbinCreateResponse("binId");

        Assert.assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    public void equalsNull() throws Exception {
        PostbinCreateResponse result = new PostbinCreateResponse("binId");

        Assert.assertFalse(result.equals(null));
    }

    @Test
    public void equalsDifferentClass() throws Exception {
        PostbinCreateResponse result = new PostbinCreateResponse("binId");

        Assert.assertFalse(result.equals("string"));
    }

    @Test
    public void equalsSelf() throws Exception {
        PostbinCreateResponse result = new PostbinCreateResponse("binId");

        Assert.assertTrue(result.equals(result));
    }

    @Test
    public void equalsDifferentData() throws Exception {
        PostbinCreateResponse result1 = new PostbinCreateResponse("binId1");
        PostbinCreateResponse result2 = new PostbinCreateResponse("binId2");

        Assert.assertFalse(result1.equals(result2));
    }

    @Test
    public void equalsSameData() throws Exception {
        PostbinCreateResponse result1 = new PostbinCreateResponse("binId");
        PostbinCreateResponse result2 = new PostbinCreateResponse("binId");

        Assert.assertTrue(result1.equals(result2));
    }

    @Test
    public void toStringTest() throws Exception {
        PostbinCreateResponse obj = new PostbinCreateResponse("binId");

        String result = obj.toString();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("binId=binId"));
    }

    private BufferedReader getClasspathReader(Path filePath) {
        return new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filePath.toString()),
                        StandardCharsets.UTF_8));
    }

}
