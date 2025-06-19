package com.rayhan.githubanalyzer.CreateReadMe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class ReadMeService {


    private final WebClient webClient;
    private final String gemniApiKey;
    private final String gemniModelId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    public ReadMeService( @Value("${gemini.api.key}") String gemniApiKey,
        @Value("${gemini.api.model-id}") String gemniModelId,
        WebClient.Builder webClientBuilder) {

        this.gemniApiKey = gemniApiKey;
        this.gemniModelId = gemniModelId;
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();

    }

    public Mono<String> analyzeRepositoryData(String textPrompt) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = objectMapper.createArrayNode();
        ObjectNode contentNode = objectMapper.createObjectNode();
        ArrayNode partsArray = objectMapper.createArrayNode();
        ObjectNode partNode = objectMapper.createObjectNode();

        partNode.put("text", textPrompt);
        partsArray.add(partNode);
        contentNode.set("parts", partsArray);
        contentsArray.add(contentNode);
        rootNode.set("contents", contentsArray);

        // Add generation configuration (optional but recommended for control)
        ObjectNode generationConfigNode = objectMapper.createObjectNode();
        generationConfigNode.put("temperature", 0.7); // Adjust creativity (0.0 - 1.0)
        generationConfigNode.put("maxOutputTokens", 2048); // Max tokens in the response
        generationConfigNode.put("topP", 0.95); // Nucleus sampling
        generationConfigNode.put("topK", 40);   // Top-k sampling
        rootNode.set("generationConfig", generationConfigNode);

        String requestBodyJson;
        try {
            requestBodyJson = objectMapper.writeValueAsString(rootNode);
            // System.out.println("Gemini Request Body: " + requestBodyJson); // For debugging
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Error creating Gemini request body", e));
        }

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(gemniModelId + ":generateContent") // e.g., gemini-pro:generateContent
                        .queryParam("key", gemniApiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBodyJson)
                .retrieve()
                // Handle potential API errors (e.g., 400, 500 from Gemini)
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class) // Get the error response body
                                .flatMap(errorBody -> {
                                    System.err.println("Gemini API error: " + clientResponse.statusCode() + " - " + errorBody);
                                    return Mono.error(new RuntimeException("Gemini API error: " + clientResponse.statusCode() + " " + errorBody));
                                })
                )
                .bodyToMono(JsonNode.class) // Expect a JSON response
                .flatMap(jsonResponse -> {
                    // Parse the response to extract the generated text
                    JsonNode candidatesNode = jsonResponse.path("candidates");
                    if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
                        JsonNode firstCandidate = candidatesNode.get(0);
                        JsonNode contentNodeResponse = firstCandidate.path("content");
                        JsonNode partsNode = contentNodeResponse.path("parts");
                        if (partsNode.isArray() && !partsNode.isEmpty()) {
                            JsonNode firstPart = partsNode.get(0);
                            JsonNode textNode = firstPart.path("text");
                            if (textNode.isTextual()) {
                                return Mono.just(textNode.asText());
                            }
                        }
                    }
                    System.err.println("Unexpected Gemini response format: " + jsonResponse.toPrettyString());
                    return Mono.error(new RuntimeException("Unexpected response format from Gemini API"));
                })
                .doOnError(e -> System.err.println("Error calling Gemini API: " + e.getMessage()));
    }



}
