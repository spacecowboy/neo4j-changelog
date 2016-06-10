package org.neo4j.changelog;


import javax.annotation.Nonnull;
import java.util.List;

public class Main {

    /**
     * Generate changelog for the specific branch and specified upstream
     */
    private void run(@Nonnull String nextVersion,
                     @Nonnull String localDir,
                     @Nonnull String branch,
                     @Nonnull String repo,
                     @Nonnull String changeLogFilePath) {
        if (!isGitHubRepo(repo)) {
            throw new IllegalArgumentException("Only supports GitHub repositories");
        }

        List<String> versionTags = getVersionTags(localDir);
        ChangeLog changeLog = new ChangeLog(versionTags);

        getPullRequests(repo).stream()
                .filter(pr -> isChangeLogWorthy(pr) && isAncestorOf(pr, branch))
                .map(pr -> convertToChange(pr, versionTags, nextVersion))
                .sorted()
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogFilePath);
    }

    private boolean isAncestorOf(@Nonnull PullRequest pr, @Nonnull String branch) {
        return false;
    }

    private List<String> getVersionTags(@Nonnull String localDir) {
        return null;
    }

    private static Change convertToChange(@Nonnull PullRequest pullRequest, @Nonnull List<String> versionTags,
                                          @Nonnull String nextVersion) {
        return null;
    }

    /**
     * Check that PR is tagged with changelog
     *
     * @param pr to check
     * @return true if pr should be included in changelog, false otherwise
     */
    private boolean isChangeLogWorthy(PullRequest pr) {
        return false;
    }

    private List<PullRequest> getPullRequests(String upstream) {
        return null;
    }

    private boolean isGitHubRepo(@Nonnull String upstream) {
        return false;
    }

    interface PullRequest {
        String getBaseBranch();
    }

    interface Change extends Comparable {

    }
}
