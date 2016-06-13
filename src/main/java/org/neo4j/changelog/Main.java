package org.neo4j.changelog;

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
import java.util.stream.Stream;

public class Main {

    private void run(@Nonnull String nextVersion,
                     @Nonnull File localDir,
                     @Nonnull String gitRef,
                     @Nonnull Path changeLogPath,
                     @Nonnull List<String> categories,
                     @Nonnull Stream<PullRequest> pullRequests) throws GitAPIException, IOException {
        GitHelper gitHelper = new GitHelper(localDir);
        List<Ref> versionTags = gitHelper.getVersionTags(nextVersion);
        ChangeLog changeLog = new ChangeLog(versionTags, categories);

        pullRequests
                .filter(pr -> GitHubHelper.isChangeLogWorthy(pr) && GitHubHelper.isIncluded(pr, nextVersion) &&
                        gitHelper.isAncestorOf(pr.getCommit(), gitRef))
                .map(pr -> GitHubHelper.convertToChange(pr,
                        gitHelper.getFirstVersionOf(pr.getCommit(), versionTags, nextVersion)))
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogPath);
    }
}
