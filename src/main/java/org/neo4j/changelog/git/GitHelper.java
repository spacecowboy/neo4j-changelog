package org.neo4j.changelog.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.neo4j.changelog.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Miscellaneous utility functions related to Git specific things.
 */
public class GitHelper {
    static Pattern VERSION_TAG_PATTERN = Pattern.compile("^v?[\\d\\.]+");
    private final Git git;
    private final Repository repo;

    public GitHelper(@Nonnull File localDir) throws IOException {
        this.git = getGit(localDir);
        this.repo = git.getRepository();
    }

    /**
     * Returns the version tags which belongs with the specified version. If 3.0.5 is specified,
     * then all 3.0.X tags are returned.
     */
    @Nonnull
    public List<Ref> getVersionTags(@Nonnull String version) throws IOException, GitAPIException {
        return getVersionTags().stream()
                        .filter(ref -> Util.isSameMajorMinorVersion(Util.getTagName(ref), version))
                .collect(Collectors.toList());
    }

    /**
     * Returns the version tags which belongs between the specified versions (inclusive): Example: 2.3.0 - 3.0.9 could
     * return 2.3.0, 2.3.1, 2.3.2,...., 3.0.7, 3.0.8, 3.0.8
     */
    @Nonnull
    public List<Ref> getVersionTags(@Nonnull String from, @Nonnull String to) throws IOException, GitAPIException {
        return getVersionTags().stream()
                               .filter(ref -> Util.versionLiesBetween(Util.getTagName(ref), from, to))
                               .collect(Collectors.toList());
    }

    @Nonnull
    public List<Ref> getVersionTags() throws IOException, GitAPIException {
        return git.tagList().call().stream()
                .filter(GitHelper::isVersionTag)
                .collect(Collectors.toList());
    }

    public static boolean isVersionTag(@Nonnull Ref ref) {
        // Name is like refs/tags/0.0.0
        return isVersionTag(Util.getTagName(ref));
    }

    static boolean isVersionTag(@Nonnull String tagName) {
        return VERSION_TAG_PATTERN.asPredicate().test(tagName);
    }

    public static Git getGit(@Nonnull File localDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder().findGitDir(localDir).readEnvironment();
        if (builder.getGitDir() == null) {
            throw new RepositoryNotFoundException(localDir);
        }
        return Git.wrap(builder.build());
    }

    /**
     * Check if base is an ancestor of tip.
     */
    public boolean isAncestorOf(@Nonnull String base, @Nonnull String tip) {
        try {
            RevWalk walk = new RevWalk(repo);

            ObjectId baseCommit = getCommitFromString(base);
            ObjectId tipCommit = getCommitFromString(tip);

            if (baseCommit != null && tipCommit != null) {

                return walk.isMergedInto(walk.parseCommit(baseCommit),
                        walk.parseCommit(tipCommit));
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Get the merge commit which contains {sha}, which is on the same "branch" as {tip}
     */
    public ObjectId getLatestMergeCommit(@Nonnull String sha, @Nonnull String tip) throws IOException {
        RevWalk walk = new RevWalk(repo);

        RevCommit from = walk.parseCommit(getCommitFromString(sha));
        RevCommit to = walk.parseCommit(getCommitFromString(tip));

        walk.markStart(to);
        walk.markUninteresting(from);

        //walk.setRetainBody(false);
        walk.setRevFilter(new RevFilter() {
            @Override
            public boolean include(RevWalk walker, RevCommit commit) throws StopWalkException, IOException {
                // Merge commits have more than 1 parent
                return (commit.getParentCount() > 1 &&
                        // make sure it is on the ancestry path
                        walk.isMergedInto(from, commit));
            }

            @Override
            public RevFilter clone() {
                return this;
            }
        });

        RevCommit lastMerge = null;
        for (RevCommit commit: walk) {
            String msg = commit.getShortMessage();
                // Is a merge commit?
                lastMerge = commit;
        }
        ObjectId mergeCommit = lastMerge.toObjectId();
        return mergeCommit;
    }

    /**
     * Get the first (chronologically) commit which the second ref can reach, which the first cannot.
     * Equivalent to `git log test-A..test-B --oneline | tail -n 1
     */
    public ObjectId getRoot(@Nonnull String base, @Nonnull String tip) throws IOException, GitAPIException {
        ObjectId baseCommit = getCommitFromString(base);
        ObjectId tipCommit = getCommitFromString(tip);

        if (baseCommit != null && tipCommit != null) {
            ObjectId result = null;
            // Want the last commit
            Iterable<RevCommit> commits = git.log().addRange(baseCommit, tipCommit).call();
            for (RevCommit commit : commits) {
                result = commit.toObjectId();
            }

            if (result != null) {
                return result;
            }
        }
        throw new RuntimeException("No root could be found");
    }

    @Nullable
    public ObjectId getCommitFromString(@Nonnull String base) throws IOException {
        // Try raw commit first
        try {
            return ObjectId.fromString(base);
        } catch (InvalidObjectIdException e) {
            Ref ref = repo.findRef(base);
            if (ref != null) {
                return ref.getObjectId();
            }
        }
        return null;
    }

    @Nonnull
    public String getFirstVersionOf(@Nonnull String commit,
                                    @Nonnull List<Ref> versionTags,
                                    @Nonnull String fallback) {
        versionTags.sort(Util.SemanticComparator());

        for (Ref tag: versionTags) {
            if (isAncestorOf(commit, tag.getName())) {
                return getTagName(tag);
            }
        }
        return fallback;
    }

    private static String getTagName(Ref tag) {
        return tag.getName().substring(1 + tag.getName().lastIndexOf("/"));
    }

    public Repository getRepo() {
        return repo;
    }
}
