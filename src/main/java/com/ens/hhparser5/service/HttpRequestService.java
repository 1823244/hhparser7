package com.ens.hhparser5.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class HttpRequestService {

    private final Logger logger = LoggerFactory.getLogger(HttpRequestService.class);

    public HttpResponse<String> executeRequestAndGetResponse(String url){

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            logger.info("executed request: {}", url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            //throw new RuntimeException(e);
            logger.error("error while execution HTTP request: {}",e.getMessage());
        }
        return response;
    }

    public String executeRequestAndGetResultAsString(String url){
        return executeRequestAndGetResponse(url).body();
    }

    public void executeRequest(String url){
        executeRequestAndGetResponse(url);
    }
}
