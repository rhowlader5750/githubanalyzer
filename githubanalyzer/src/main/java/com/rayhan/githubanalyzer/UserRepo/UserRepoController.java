package com.rayhan.githubanalyzer.UserRepo;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/github/usersavedrepos")
public class UserRepoController {

    private final UserRepoService userRepoService;

    
    public UserRepoController(UserRepoService userRepoService) {
        this.userRepoService = userRepoService;
    }

    @PostMapping("/save")
    public ResponseEntity<UserRepo> addRepo(@RequestBody UserRepo userRepo) {
        return ResponseEntity.ok(userRepoService.saveRepo(userRepo));
    }

    @GetMapping
    public ResponseEntity<List<UserRepo>> getSavedRepos(@RequestParam String username) {
        List<UserRepo> repos = userRepoService.getRepoByUsername(username);
        return ResponseEntity.ok(repos != null ? repos : new ArrayList<>());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRepo(@PathVariable Long id) {
        userRepoService.deleteRepo(id);
        return ResponseEntity.ok("Repository deleted successfully.");
    }


}
