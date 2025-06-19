package com.rayhan.githubanalyzer.UserRepo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepoRepository extends JpaRepository<UserRepo, Long> {
    List<UserRepo> findByUsername(String username);
}
