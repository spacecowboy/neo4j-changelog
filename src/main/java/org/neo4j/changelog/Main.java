package org.neo4j.changelog;


import javax.annotation.Nonnull;
import java.util.List;

public class Main {

    /**
     * Generate changelog for current branch
     * @param changeLogFilePath
     */
    public void run(@Nonnull String changeLogFilePath) {
        // We want to generate changelog based on the branch we are on by default
        String branch = getCurrentBranch();
        run(branch, changeLogFilePath);
    }

    /**
     * Generate changelog for the specified branch
     * @param branch
     * @param changeLogFilePath
     */
    private void run(@Nonnull String branch, @Nonnull String changeLogFilePath) {
        // Get the default upstream for this branch
        String upstream = getUpstreamFor(branch);
        run(branch, upstream, changeLogFilePath);
    }

    /**
     * Generate changelog for the specific branch and specified upstream
     * @param branch
     * @param upstream
     */
    private void run(@Nonnull String branch, @Nonnull String upstream, @Nonnull String changeLogFilePath) {
        if (!isGitHubRepo(upstream)) {
            throw new IllegalArgumentException("Only supports GitHub repositories");
        }

        List<String> tags = getTagsForBranch(upstream, branch);
        ChangeLog changeLog = new ChangeLog(tags);

        getPullRequests(upstream).stream()
                .filter(pr -> branch.equals(pr.getBaseBranch()) && isChangeLogWorthy(pr))
                .map(Main::convertToChange)
                .sorted()
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogFilePath);
    }

    private List<String> getTagsForBranch(String upstream, String branch) {
        return null;
    }

    private static Change convertToChange(PullRequest pullRequest) {
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

    private String getUpstreamFor(@Nonnull String branch) {
        return null;
    }

    private String getCurrentBranch() {
        return null;
    }

    interface PullRequest {
        String getBaseBranch();
    }

    interface Change extends Comparable {

    }
}
