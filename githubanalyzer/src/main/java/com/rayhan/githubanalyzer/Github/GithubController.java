package com.rayhan.githubanalyzer.Github;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubService githubService;

    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/repo-content")
    public Mono<ResponseEntity<Map<String, String>>> getRepoContents(
            @RequestParam String owner,
            @RequestParam String repo) {

        return githubService.getRepoContents(owner, repo, "")
                .map(ResponseEntity::ok);
    }

//    @GetMapping("/allrepos")
//    public Mono<ResponseEntity<Map<String, Object>>> getAllRepos(
//            @RequestParam String owner
//    ){
//        return githubService.getAllRepos(owner)
//                .map(repoNames -> {
//                    Map<String, Object> response = new HashMap<>();
//                    response.put("repos", repoNames);
//                    return ResponseEntity.ok(response);
//                });
//    }

    @GetMapping("/allrepos")
    public Mono<ResponseEntity<Map<String, Object>>> getAllRepos(
            @RequestParam String owner
    ) {
        return githubService.getAllReposPagination(owner)
                .map(repos -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("repos", repos);
                    return ResponseEntity.ok(response);
                });
    }



    @GetMapping("/userinfo")
    public Mono<ResponseEntity<Github>> getUserInfo(@RequestParam String owner) {
        return githubService.getInfo(owner)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{username}/savedrepocontent")
    public Mono<Map<String, Map<String, String>>> getAllCodeFromRepos(
            @PathVariable String username,
            @RequestParam List<String> repos) {

        return githubService.getMultipleRepoContents(username, repos);
    }




}




