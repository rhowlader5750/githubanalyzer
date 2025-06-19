package com.rayhan.githubanalyzer.Github;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GithubService {

    private final WebClient webClient;
    private static final String DEFAULT_BRANCH = "main";

    private static final List<String> VALID_EXTENSIONS = List.of("java", "py", "js", "cpp", "c", "ipynb", "php");

    public GithubService(WebClient webClient) {
        this.webClient = webClient;
    }

   public Mono<Github> getInfo(String owner){
       String theurl = String.format("https://api.github.com/users/%s", owner);
        return webClient.get()
                .uri(theurl)
                .retrieve()
                .bodyToMono(Github.class);

   }

    public Mono<List<Github>> getAllRepos(String owner){
        String theurl = String.format("https://api.github.com/users/%s/repos", owner);
        return webClient.get()
                .uri(theurl)
                .retrieve()
                .bodyToFlux(Github.class)
                .collectList();
    }


    public Mono<List<Github>> getAllReposPagination(String owner) {
        return fetchReposPage(owner, 1, new ArrayList<>());
    }

    private Mono<List<Github>> fetchReposPage(String owner, int page, List<Github> accumulated) {
        String url = String.format("https://api.github.com/users/%s/repos?page=%d&per_page=100", owner, page);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Github.class)
                .collectList()
                .flatMap(repos -> {
                    if (repos.isEmpty()) {
                        return Mono.just(accumulated);
                    } else {
                        accumulated.addAll(repos);
                        return fetchReposPage(owner, page + 1, accumulated); // recurse
                    }
                });
    }


    private String buildRawFileUrl(String owner, String repo, String branch, String filePath) {
        String baseUrl = "https://raw.githubusercontent.com";

        String encodedPath = Arrays.stream(filePath.split("/"))
                .map(segment -> segment.replace(" ", "%20"))
                .collect(Collectors.joining("/"));

        String finalUrl = String.format("%s/%s/%s/%s/%s", baseUrl, owner, repo, branch, encodedPath);

        return finalUrl;
    }

    public Mono<Map<String, String>> getRepoContents(String owner, String repo, String path) {
        String endpoint = String.format("/repos/%s/%s/contents/%s", owner, repo, path == null ? "" : path);

        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .flatMapMany(Flux::fromIterable)
                .flatMap(item -> {
                    String type = (String) item.get("type");
                    String name = (String) item.get("name");
                    String filePath = (String) item.get("path");

                    if ("dir".equals(type)) {
                        return getRepoContents(owner, repo, filePath)
                                .flatMapMany(map -> Flux.fromIterable(map.entrySet()));
                    } else if ("file".equals(type)) {
                        String extension = getExtension(name);
                        if (VALID_EXTENSIONS.contains(extension)) {
                            String rawUrl = buildRawFileUrl(owner, repo, "main", filePath);

                            return webClient.get()
                                    .uri(URI.create(rawUrl))
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .map(content -> Map.entry(filePath, content))
                                    .flux();
                        }
                    }
                    return Flux.empty();
                })
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public Mono<Map<String, Map<String, String>>> getMultipleRepoContents(String owner, List<String> repoNames) {
        List<Mono<Map.Entry<String, Map<String, String>>>> monos = repoNames.stream()
                .map(repo -> getRepoContents(owner, repo, null)
                        .map(contents -> Map.entry(repo, contents)))
                .toList();

        return Flux.merge(monos)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }



    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }

}
