package com.matheushfp.createUrlShortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main implements RequestHandler<Map<String,Object>, Map<String, String>> {

    private final Gson gson = new Gson();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = (String) input.get("body");

        Map<String, String> bodyMap;
        try {
            bodyMap = gson.fromJson(body, Map.class);
        } catch(Exception e) {
            throw new RuntimeException("Error parsing JSON body: " + e.getMessage(), e);
        }

        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        long expirationTimeInSeconds = Long.parseLong(expirationTime);

        String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

        UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

        try {
            String urlDataJson = gson.toJson(urlData);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket("url-shortener-storage-mhfp")
                    .key(shortUrlCode + ".json")
                    .build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch(Exception e) {
            throw new RuntimeException("Error saving data to S3: " + e.getMessage(), e);
        }

        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }
}