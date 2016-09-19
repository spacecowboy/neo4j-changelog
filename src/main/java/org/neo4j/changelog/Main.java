package org.neo4j.changelog;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.config.ConfigReader;
import org.neo4j.changelog.config.ProjectConfig;
import org.neo4j.changelog.git.GitHelper;
import org.neo4j.changelog.github.GitHubHelper;
import org.neo4j.changelog.github.PullRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    private static List<PullRequest> getPullRequests(@Nonnull String token, @Nonnull String user,
                                                     @Nonnull String repo) {
        GitHubHelper gitHubHelper = new GitHubHelper(token, user, repo);
        return gitHubHelper.getChangeLogPullRequests();
    }

    private static void generateChangelog(
            @Nonnull String fromRef,
            @Nonnull String toRef,
            @Nonnull String versionPrefix,
            @Nonnull String nextHeader,
            @Nonnull GitHelper gitHelper,
            @Nonnull String changeLogPath,
            @Nonnull String requiredLabel,
            @Nonnull List<String> categories,
            @Nonnull List<PullRequest> pullRequests) throws GitAPIException, IOException {
        List<Ref> versionTags = gitHelper.getVersionTags(fromRef, versionPrefix);
        versionTags.sort(Util.SemanticComparator());
        ChangeLog changeLog = new ChangeLog(versionTags, nextHeader, categories);

        if (!gitHelper.isAncestorOf(fromRef, toRef)) {
            throw new RuntimeException(
                    String.format("%s is not an ancestor of %s, can't generate changelog", fromRef, toRef));
        }

        System.out.println("Version tags:");
        versionTags.forEach(t -> System.out.println(Util.getTagName(t)));

        System.out.println("PRS: " + pullRequests.size());

        pullRequests.stream()
                .filter(pr -> GitHubHelper.isChangeLogWorthy(pr, requiredLabel) && GitHubHelper.isIncluded(pr, versionPrefix) &&
                        gitHelper.isAncestorOf(pr.getCommit(), toRef) &&
                        !gitHelper.isAncestorOf(pr.getCommit(), fromRef))
                .map(pr -> GitHubHelper.convertToChange(pr,
                        gitHelper.getFirstVersionOf(pr.getCommit(), versionTags, nextHeader)))
                .forEach(changeLog::addToChangeLog);

        ConfigReader configReader;

        changeLog.write(new File(changeLogPath).toPath());
    }

    public static void main(String[] args) {
        ProjectConfig config = null;
        try {
            config = ConfigReader.parseConfig(args);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ArgumentParserException e) {
            System.exit(1);
        }

        // TODO use config below

        GitHelper gitHelper = null;
        try {
            gitHelper = new GitHelper(new File(config.getGitConfig().getCloneDir()));
        } catch (IOException e) {
            System.err.printf("\nError: Could not open git repo at %s: %s\n", config.getGitConfig().getCloneDir(), e.getMessage());
            System.exit(1);
        }

        String fromRef = config.getGitConfig().getFrom();
        if (fromRef.isEmpty()) {
            try {
                fromRef = gitHelper.getOldestCommit().getName();
                System.out.printf("No from-ref specified, using: %s\n", fromRef);
            } catch (GitAPIException e) {
                System.err.printf("\nError: Could not find oldest commit: %s\n", e.getMessage());
                System.exit(1);
            }
        } else {
            try {
                fromRef = gitHelper.getCommitFromString(fromRef).getName();
            } catch (IOException | NullPointerException e) {
                System.err.printf("\nError: Could not parse commit for %s: %s\n", fromRef, e.getMessage());
                System.exit(1);
            }
        }

        String toRef = config.getGitConfig().getTo();
        try {
            toRef = gitHelper.getCommitFromString(toRef).getName();
        } catch (IOException | NullPointerException e) {
            System.err.printf("\nError: Could not parse commit for %s: %s\n", toRef, e.getMessage());
            System.exit(1);
        }

        String versionPrefix = config.getVersionPrefix();
        if (!Util.isSemanticVersion(versionPrefix)) {
            System.err.printf("\nError: version-prefix is not a semantic version: %s\n", versionPrefix);
            System.exit(1);
        }



        List<PullRequest> pullRequests = null;
        try {
            System.out.printf("Fetching pull requests from github.com/%s/%s\n", config.getGithubConfig().getUser(),
                    config.getGithubConfig().getRepo());
            pullRequests = getPullRequests(config.getGithubConfig().getToken(),
                    config.getGithubConfig().getUser(),
                    config.getGithubConfig().getRepo());
            System.out.printf("%d pull requests fetched.\n", pullRequests.size());
        } catch (Exception e) {
            System.err.printf("\nError: An error occurred while fetching pull requests: %s\n", e.getMessage());
            System.exit(1);
        }



        try {
            System.out.printf("Generating changelog between %s and %s for %s\n", fromRef, toRef, versionPrefix);
            generateChangelog(fromRef, toRef, versionPrefix, config.getNextHeader(), gitHelper, config.getOutputPath(),
                    config.getGithubConfig().getRequiredLabel(), config.getCategories(), pullRequests);
            System.out.printf("\nDone. Changelog written to %s\n", config.getOutputPath());
        } catch (Exception e) {
            System.err.printf("\nError: An error occurred while building changelog: %s\n", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
