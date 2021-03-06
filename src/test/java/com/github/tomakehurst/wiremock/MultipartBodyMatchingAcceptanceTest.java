/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MultipartBodyMatchingAcceptanceTest extends AcceptanceTestBase {

    HttpClient httpClient = HttpClientFactory.createClient();

    @Test
    public void acceptsAMultipartRequestContainingATextAndAFilePart() throws Exception {
        stubFor(post("/multipart")
            .withMultipartRequestBody(aMultipart()
                .withName("text")
                .withBody(containing("hello")))
            .withMultipartRequestBody(aMultipart()
                .withName("file")
                .withBody(binaryEqualTo("ABCD".getBytes())))
            .willReturn(ok())
        );

        HttpUriRequest request = RequestBuilder
            .post(wireMockServer.baseUrl() + "/multipart")
            .setEntity(MultipartEntityBuilder.create()
                .addTextBody("text", "hello")
                .addBinaryBody("file", "ABCD".getBytes())
                .build()
            )
            .build();

        HttpResponse response = httpClient.execute(request);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void handlesAbsenceOfPartsInAMultipartRequest() throws Exception {
        stubFor(post("/empty-multipart")
            .withMultipartRequestBody(aMultipart()
                .withName("bits")
                .withBody(matching(".*")))
            .willReturn(ok())
        );

        HttpUriRequest request = RequestBuilder
            .post(wireMockServer.baseUrl() + "/empty-multipart")
            .setHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
            .setEntity(new StringEntity("", MULTIPART_FORM_DATA))
            .build();

        HttpResponse response = httpClient.execute(request);

        assertThat(response.getStatusLine().getStatusCode(), is(404));
    }

    @Test
    public void doesNotFailWithMultipartMixedRequest() throws Exception {
        stubFor(post("/multipart-mixed")
                .willReturn(ok())
        );

        HttpUriRequest request = RequestBuilder
                .post(wireMockServer.baseUrl() + "/multipart-mixed")
                .setHeader("Content-Type", "multipart/mixed; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                .setEntity(new StringEntity("", ContentType.create("multipart/mixed")))
                .build();

        HttpResponse response = httpClient.execute(request);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }
}
