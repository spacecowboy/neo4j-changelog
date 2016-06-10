package org.neo4j.changelog.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Miscellaneous utility functions related to Git specific things.
 */
public class GitHelper {
    static Pattern VERSION_TAG_PATTERN = Pattern.compile("^v?[\\d\\.]+$");

    public static @Nonnull List<Ref> getVersionTags(@Nonnull File localDir) throws IOException, GitAPIException {
        Git git = getGit(localDir.getAbsoluteFile());
        return git.tagList().call().stream()
                .filter(GitHelper::isVersionTag)
                .collect(Collectors.toList());
    }

    static boolean isVersionTag(@Nonnull Ref ref) {
        // Name is like refs/tags/0.0.0
        int i = ref.getName().lastIndexOf("/");
        return VERSION_TAG_PATTERN.asPredicate().test(ref.getName().substring(i + 1));
    }

    private static Git getGit(@Nonnull File localDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder().findGitDir(localDir).readEnvironment();
        if (builder.getGitDir() == null) {
            throw new RepositoryNotFoundException(localDir);
        }
        return Git.wrap(builder.build());
    }

    /**
     * Check if commit is an ancestor of ref.
     */
    public static boolean isAncestorOf(@Nonnull String commit, @Nonnull String ref) {
        return false;
    }
}
