package com.rayhan.githubanalyzer.UserRepo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserRepoService {

    private final UserRepoRepository userRepoRepository;

    public UserRepoService(UserRepoRepository userRepoRepository) {
        this.userRepoRepository = userRepoRepository;
    }

    public UserRepo saveRepo(UserRepo repo) {
        return userRepoRepository.save(repo);
    }

    public List<UserRepo> getRepoByUsername(String username) {
        List<UserRepo> repos = userRepoRepository.findByUsername(username);
        return repos != null ? repos : new ArrayList<>();
    }

    public void deleteRepo(Long id) {
        userRepoRepository.deleteById(id);
    }



}
