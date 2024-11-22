package com.matheushfp.redirectUrlShortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();
    private final Gson gson = new Gson();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = pathParameters.replace("/", "");

        if(shortUrlCode.isEmpty()){
            throw new IllegalArgumentException("Invalid input: shortUrlCode is required");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-mhfp")
                .key(shortUrlCode + ".json")
                .build();

        InputStream s3ObjectStream;

        try {
            s3ObjectStream = s3Client.getObject(getObjectRequest);
        } catch(Exception e){
            throw new RuntimeException("Error fetching data from S3: " + e.getMessage(), e);
        }

        UrlData urlData;

        try {
            urlData = gson.fromJson(new InputStreamReader(s3ObjectStream), UrlData.class);
        } catch(Exception e) {
            throw new RuntimeException("Error deserializing JSON: " + e.getMessage(), e);
        }

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        Map<String, Object> response = new HashMap<String, Object>();

        if(currentTimeInSeconds > urlData.getExpirationTime()) {
            response.put("statusCode", 410);
            response.put("body", "This URL has expired");

            return response;
        }

        Map<String, String> headers = new HashMap<String, String>();

        headers.put("Location", urlData.getOriginalUrl());

        response.put("statusCode", 302);
        response.put("headers", headers);

        return response;
    }
}