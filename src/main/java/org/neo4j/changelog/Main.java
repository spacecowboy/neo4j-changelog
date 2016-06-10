package org.neo4j.changelog;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.git.GitHelper;
import org.neo4j.changelog.github.GitHubHelper;
import org.neo4j.changelog.github.PullRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    /**
     * Generate changelog for the specific branch and specified upstream
     */
    private void run(@Nonnull String nextVersion,
                     @Nonnull String localDir,
                     @Nonnull String branch,
                     @Nonnull String repo,
                     @Nonnull String changeLogFilePath) throws GitAPIException, IOException {
        List<Ref> versionTags = GitHelper.getVersionTags(new File(localDir));
        ChangeLog changeLog = new ChangeLog(versionTags);

        getPullRequests(repo).stream()
                .filter(pr -> GitHubHelper.isChangeLogWorthy(pr) && isAncestorOf(pr, branch))
                .map(pr -> convertToChange(pr, versionTags, nextVersion))
                .sorted()
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogFilePath);
    }

    private boolean isAncestorOf(@Nonnull PullRequest pr, @Nonnull String branch) {
        return false;
    }


    private static Change convertToChange(@Nonnull PullRequest pullRequest, @Nonnull List<Ref> versionTags,
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

    interface Change extends Comparable {

    }
}
