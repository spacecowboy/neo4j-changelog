package org.neo4j.changelog;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.git.GitHelper;
import org.neo4j.changelog.github.GitHubHelper;
import org.neo4j.changelog.github.PullRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    /**
     * Generate changelog for the specific commit and specified upstream
     */
    private void run(@Nonnull String nextVersion,
                     @Nonnull String localDir,
                     @Nonnull String commit,
                     @Nonnull String repo,
                     @Nonnull Path changeLogPath,
                     @Nonnull List<String> headers) throws GitAPIException, IOException {
        File clone = new File(localDir);
        Git git = GitHelper.getGit(clone);
        List<Ref> versionTags = GitHelper.getVersionTags(clone);
        ChangeLog changeLog = new ChangeLog(versionTags, headers);


        getPullRequests(repo).stream()
                .filter(pr -> GitHubHelper.isChangeLogWorthy(pr) && GitHubHelper.isIncluded(pr, nextVersion) &&
                        GitHelper.isAncestorOf(git.getRepository(), pr.getCommit(), commit))
                .map(pr -> GitHubHelper.convertToChange(pr,
                        GitHelper.getFirstVersionOf(git.getRepository(), pr.getCommit(), versionTags, nextVersion)))
                .sorted()
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogPath);
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

    @Nonnull
    private List<PullRequest> getPullRequests(String upstream) {
        return null;
    }

    private boolean isGitHubRepo(@Nonnull String upstream) {
        return false;
    }

}
