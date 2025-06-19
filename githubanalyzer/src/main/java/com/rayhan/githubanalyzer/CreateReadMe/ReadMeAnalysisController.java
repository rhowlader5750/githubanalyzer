
package com.rayhan.githubanalyzer.CreateReadMe;

import com.rayhan.githubanalyzer.CreateReadMe.ReadMeService;
import com.rayhan.githubanalyzer.Github.GithubService; // Corrected import
import com.rayhan.githubanalyzer.UserRepo.UserRepo;
import com.rayhan.githubanalyzer.UserRepo.UserRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/github")
public class ReadMeAnalysisController {

    @Autowired
    private GithubService githubService; // Your reactive GithubService
    @Autowired
    private ReadMeService geminiService;

    @Autowired
    private UserRepoService userRepoService;

    /**
     * Endpoint to analyze a GitHub repository using Gemini AI.
     * Fetches consolidated code data from GitHub and then sends it to Gemini for interpretation.
     *
     * @param owner The owner of the GitHub repository.
     * @param repo The name of the GitHub repository.
     * @return A Mono emitting a ResponseEntity containing the AI's analysis,
     * or an error response if fetching or analysis fails.
     */
    @GetMapping("/repo/overview")
    public Mono<ResponseEntity<String>> getRepoOverviewAnalysis(
            @RequestParam String owner,
            @RequestParam String repo) {

        System.out.println("Received analysis request for GitHub repo: " + owner + "/" + repo);

        // 1. Fetch consolidated GitHub data reactively using your getRepoContents method
        // Pass an empty string ("") as the initial path for the root of the repository.
        return githubService.getRepoContents(owner, repo, "")
                .flatMap(consolidatedGitHubData -> {
                    if (consolidatedGitHubData == null || consolidatedGitHubData.isEmpty()) {
                        System.out.println("No valid code files found for analysis for " + owner + "/" + repo);
                        return Mono.just(ResponseEntity.badRequest().body("Could not retrieve any valid code files from " + owner + "/" + repo + " for analysis."));
                    }

                    // 2. Prepare the prompt for Gemini based on the consolidated data
                    StringBuilder combinedCode = new StringBuilder();
                    consolidatedGitHubData.forEach((filePath, fileContent) -> {
                        combinedCode.append("--- File: ").append(filePath).append(" ---\n");
                        combinedCode.append(fileContent).append("\n\n");
                    });

                    String prompt = "You are an experienced software engineer tasked with generating a professional README.md for this repository. " +
                            "Assume you wrote the code yourself and want to help other developers understand the project. " +
                            "Your README should include the following sections:\n\n" +

                            "1. **Project Overview** – Briefly describe the main purpose of the application.\n" +
                            "2. **Tech Stack** – Identify the main programming languages, frameworks, and libraries used.\n" +
                            "3. **Architecture** – Describe how the project is structured (e.g., frontend/backend split, monolithic, microservices).\n" +
                            "4. **Core Features** – List 3 features and explain the key functionalities of the application, why they are important to the overall product \n" +
                            "5. **Design Patterns** – Note any design patterns or anti-patterns observed in the codebase.\n" +
                            "6. **Improvement Suggestions** – Recommend any improvements in code quality, performance, or potential new features that could be implemented to help grow or expand project.\n\n" +

                            "Make the README clean, concise, and written in Markdown format. Use bullet points and sections where helpful. " +
                            "Use headings (##, ###) and emojis to organize the content clearly.\n\n" +

                            "---\n\n" +
                            "📦 Codebase Snapshot (Path → Content):\n\n" +
                            combinedCode.toString();


                    // 3. Call Gemini API for analysis (this call is already reactive)
                    System.out.println("Calling Gemini Service for analysis...");
                    return geminiService.analyzeRepositoryData(prompt)
                            .map(ResponseEntity::ok) // Map the successful analysis to an OK response
                            .onErrorResume(e -> {
                                // Handle errors specifically from GeminiService
                                System.err.println("Error analyzing repository with Gemini: " + e.getMessage());
                                e.printStackTrace(); // Print stack trace for debugging
                                return Mono.just(ResponseEntity.status(500).body("Error analyzing repository with AI: " + e.getMessage()));
                            });
                })
                .onErrorResume(e -> {
                    // Handle errors from GitHubService or other upstream issues (e.g., network, GitHub API rate limits)
                    System.err.println("An error occurred during GitHub data fetching or an unexpected error: " + e.getMessage());
                    e.printStackTrace(); // Print stack trace for debugging
                    return Mono.just(ResponseEntity.status(500).body("An error occurred during repository data fetching or analysis: " + e.getMessage()));
                });
    }



    @GetMapping("/repos/profileoverview")
    public Mono<ResponseEntity<String>> getMultipleReposOverviewAnalysis(
            @RequestParam String username) {

        System.out.println("Received multi-repo analysis request for user: " + username);

        // Get all saved repos for this user
        List<UserRepo> savedRepos = userRepoService.getRepoByUsername(username);

        if (savedRepos == null || savedRepos.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("No saved repositories found for user: " + username));
        }

        // Group repositories by owner (in case user has saved repos from different owners)
        Map<String, List<String>> reposByOwner = savedRepos.stream()
                .collect(Collectors.groupingBy(
                        UserRepo::getUsername, // Assuming UserRepo has getOwner() method
                        Collectors.mapping(UserRepo::getName, Collectors.toList()) // Assuming getRepoName() method
                ));

        // For now, let's handle the case where all repos are from the same owner
        // You can expand this later to handle multiple owners
        if (reposByOwner.size() != 1) {
            return Mono.just(ResponseEntity.badRequest().body("Currently only supports repositories from a single owner."));
        }

        String owner = reposByOwner.keySet().iterator().next();
        List<String> repoNames = reposByOwner.get(owner);

        System.out.println("Analyzing " + repoNames.size() + " saved repos for " + username + " from owner: " + owner);

        // 1. Fetch consolidated GitHub data from multiple repositories
        return githubService.getMultipleRepoContents(owner, repoNames)
                .flatMap(allRepoData -> {
                    if (allRepoData == null || allRepoData.isEmpty()) {
                        System.out.println("No valid code files found for analysis for user: " + username);
                        return Mono.just(ResponseEntity.badRequest().body("Could not retrieve any valid code files from saved repositories for analysis."));
                    }

                    // 2. Prepare the prompt for Gemini based on all repository data
                    StringBuilder combinedCode = new StringBuilder();

                    // Add each repository's content with clear separation
                    allRepoData.forEach((repoName, repoContents) -> {
                        combinedCode.append("=== REPOSITORY: ").append(repoName).append(" ===\n\n");

                        repoContents.forEach((filePath, fileContent) -> {
                            combinedCode.append("--- File: ").append(filePath).append(" ---\n");
                            combinedCode.append(fileContent).append("\n\n");
                        });

                        combinedCode.append("\n");
                    });

                    String prompt = "You are creating a GitHub profile README.md that showcases " + owner + "'s developer skills and competencies. " +
                            "Analyze the code from their " + repoNames.size() + " repositories to understand their technical abilities, preferred technologies, and coding style. " +
                            "Create a professional developer bio/profile README similar to popular GitHub profiles.\n\n" +
                            "Speak as if you were the developer of the repositories, make sure the descriptions are in first person.\n\n" +

                            "Generate a README with this structure and styling:\n\n" +

                            "# <h1 align=\"center\">Hi 👋 I'm " + owner + "</h1>\n\n" +

                            "## 💫 About Me:\n" +
                            "Write a compelling developer bio based on the technologies and patterns you see in their code. " +
                            "Mention their apparent specializations, interests, and what kind of developer they appear to be.\n\n" +

                            "## 🚀 What I'm Working With\n" +
                            "Based on their repositories, mention specific technologies they're actively using:\n" +
                            "- 🤖 Working with [specific framework/technology]\n" +
                            "- 💻 Building applications with [tech stack]\n" +
                            "- 🔧 Exploring [emerging technologies they use]\n\n" +

                            "## 💻 Tech Stack:\n" +
                            "Create comprehensive badge sections based on technologies found in their code:\n\n" +
                            "**Languages:**\n" +
                            "![Language](https://img.shields.io/badge/language-color?style=for-the-badge&logo=language&logoColor=white)\n" +
                            "(Generate actual badges for each language you identify)\n\n" +

                            "**Frameworks & Libraries:**\n" +
                            "![Framework](https://img.shields.io/badge/framework-color?style=for-the-badge&logo=framework&logoColor=white)\n" +
                            "(Generate badges for frameworks, libraries, and tools they use)\n\n" +

                            "**Developer Tools:**\n" +
                            "![Tool](https://img.shields.io/badge/tool-color?style=for-the-badge&logo=tool&logoColor=white)\n" +
                            "(Include development tools, databases, cloud services, etc.)\n\n" +

                            "## 🎯 Development Focus\n" +
                            "Based on their code patterns and project types, describe their development focus areas:\n" +
                            "- Backend development\n" +
                            "- Frontend frameworks\n" +
                            "- Full-stack applications\n" +
                            "- DevOps practices\n" +
                            "- etc.\n\n" +

                            "## 🌟 Coding Style & Patterns\n" +
                            "Highlight notable patterns, architectures, or best practices you observe in their code.\n\n" +

                            "## 📈 GitHub Stats\n" +
                            "Add placeholders for GitHub stats widgets (common in profile READMEs).\n\n" +

                            "Focus on showcasing " + owner + " as a developer - their skills, technologies, and expertise. " +
                            "Use modern GitHub profile styling with lots of emojis and shield.io badges. " +
                            "Make it personal and engaging while highlighting their technical competencies.\n\n" +

                            "---\n\n" +
                            "📦 Multi-Repository Codebase Snapshot:\n\n" +
                            combinedCode.toString();

                    // 3. Call Gemini API for analysis
                    System.out.println("Calling Gemini Service for multi-repository analysis...");
                    return geminiService.analyzeRepositoryData(prompt)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> {
                                System.err.println("Error analyzing repositories with Gemini: " + e.getMessage());
                                e.printStackTrace();
                                return Mono.just(ResponseEntity.status(500).body("Error analyzing repositories with AI: " + e.getMessage()));
                            });
                })
                .onErrorResume(e -> {
                    System.err.println("An error occurred during multi-repository data fetching: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(500).body("An error occurred during repository data fetching or analysis: " + e.getMessage()));
                });
    }


}