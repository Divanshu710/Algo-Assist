package com.codingapp.submissionservice.executor;

import com.codingapp.submissionservice.dto.Judge0Request;
import com.codingapp.submissionservice.dto.Judge0Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class Judge0Service {

    private final RestTemplate restTemplate;

    // Pulls your RapidAPI credentials from application.properties
    @Value("${judge0.api.url}")
    private String apiUrl;

    @Value("${judge0.api.host}")
    private String apiHost;

    @Value("${judge0.api.key}")
    private String apiKey;

    public Judge0Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public Judge0Response runSingleTest(Judge0Request request) {
        // We use wait=true so the API doesn't return a token, it returns the actual result.
        // base64_encoded=false means we send raw strings instead of converting to Base64 first.
        String endpoint = apiUrl + "/submissions?wait=true&base64_encoded=false";

        HttpEntity<Judge0Request> entity = new HttpEntity<>(request, getHeaders());

        ResponseEntity<Judge0Response> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                Judge0Response.class
        );

        return response.getBody();
    }

    /**
     * Used for the "SUBMIT" button (All Test Cases Batch)
     */
    public List<Judge0Response> submitBatchTests(List<Judge0Request> requests) throws InterruptedException {
        // Step 1: POST the batch of submissions
        String postEndpoint = apiUrl + "/submissions/batch?base64_encoded=false";

        Map<String, List<Judge0Request>> payload = new HashMap<>();
        payload.put("submissions", requests);

        HttpEntity<Map<String, List<Judge0Request>>> postEntity = new HttpEntity<>(payload, getHeaders());

        // This returns a list of objects containing {"token": "uuid"}
        ResponseEntity<List> tokenResponse = restTemplate.exchange(
                postEndpoint,
                HttpMethod.POST,
                postEntity,
                List.class
        );

        // Step 2: Extract all tokens and join them with commas
        List<Map<String, String>> tokensList = tokenResponse.getBody();
        String tokens = tokensList.stream()
                .map(t -> String.valueOf(t.get("token"))) // Safely get token
                .collect(Collectors.joining(","));

        // Step 3: Wait for Judge0 to process the batch (2 seconds)
        Thread.sleep(2000);

        // Step 4: GET the results using the tokens
        String getEndpoint = apiUrl + "/submissions/batch?tokens=" + tokens + "&base64_encoded=false";
        HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders());


        ResponseEntity<Map<String, List<Judge0Response>>> resultResponse = restTemplate.exchange(
                getEndpoint,
                HttpMethod.GET,
                getEntity,
                new ParameterizedTypeReference<Map<String, List<Judge0Response>>>() {}
        );

        // Extract the correctly mapped list!
        Map<String, List<Judge0Response>> body = resultResponse.getBody();
        if (body != null && body.containsKey("submissions")) {
            return body.get("submissions");
        }

        return Collections.emptyList();
    }
    /**
     * Helper method to attach RapidAPI Headers to every request
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        headers.set("x-rapidapi-host", apiHost);
        headers.set("x-rapidapi-key", apiKey);
        return headers;
    }
}