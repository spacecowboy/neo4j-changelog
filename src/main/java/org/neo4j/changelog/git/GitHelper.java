package org.neo4j.changelog.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.DepthWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.neo4j.changelog.Util;

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
        String tag = Util.getTagName(ref);
        return VERSION_TAG_PATTERN.asPredicate().test(tag);
    }

    private static Git getGit(@Nonnull File localDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder().findGitDir(localDir).readEnvironment();
        if (builder.getGitDir() == null) {
            throw new RepositoryNotFoundException(localDir);
        }
        return Git.wrap(builder.build());
    }

    /**
     * Check if base is an ancestor of tip.
     */
    public static boolean isAncestorOf(@Nonnull File localDir, @Nonnull String base, @Nonnull String tip) {
        try {
            Git git = getGit(localDir.getAbsoluteFile());
            RevWalk walk = new RevWalk(git.getRepository());
            return walk.isMergedInto(walk.parseCommit(ObjectId.fromString(base)),
                    walk.parseCommit(ObjectId.fromString(tip)));
        } catch (IOException e) {
            return false;
        }
    }
}
