package org.neo4j.changelog.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.neo4j.changelog.Change;
import org.neo4j.changelog.Util;
import org.neo4j.changelog.config.GitCommitConfig;
import org.neo4j.changelog.config.GitConfig;
import org.neo4j.changelog.config.ProjectConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Miscellaneous utility functions related to Git specific things.
 */
public class GitHelper {
    private static final String GITHUB_COMMIT_LINK = "https://github.com/%s/%s/commit/%s";
    static Pattern VERSION_TAG_PATTERN = Pattern.compile("^v?[\\d\\.]+");
    private final Git git;
    private final Repository repo;
    private final GitConfig config;
    private final String fromRef;
    private final String toRef;
    private final ProjectConfig globalConfig;

    public GitHelper(@Nonnull ProjectConfig globalConfig) throws IOException {
        this.globalConfig = globalConfig;
        this.config = globalConfig.getGitConfig();
        this.git = getGit(Paths.get(config.getCloneDir()));
        this.repo = git.getRepository();

        String fromRef1 = config.getFrom();
        if (fromRef1.isEmpty()) {
            try {
                fromRef1 = getOldestCommit().getName();
                System.out.printf("No from-ref specified, using: %s\n", fromRef1);
            } catch (GitAPIException e) {
                System.err.printf("\nError: Could not find oldest commit: %s\n", e.getMessage());
                System.exit(1);
            }
        } else {
            try {
                fromRef1 = getCommitFromString(fromRef1).getName();
            } catch (IOException | NullPointerException e) {
                System.err.printf("\nError: Could not parse from-commit for %s: %s\n", fromRef1, e.getMessage());
                System.exit(1);
            }
        }
        fromRef = fromRef1;

        String toRef1 = config.getTo();
        try {
            toRef1 = getCommitFromString(toRef1).getName();
        } catch (IOException | NullPointerException e) {
            System.err.printf("\nError: Could not parse to-commit for %s: %s\n", toRef1, e.getMessage());
            System.exit(1);
        }
        toRef = toRef1;

        if (!isAncestorOf(fromRef, toRef)) {
            throw new RuntimeException(
                    String.format("%s is not an ancestor of %s, can't generate changelog", fromRef, toRef));
        }
    }

    /**
     * Returns the version tags which belongs between the specified versions (inclusive): Example: 2.3.0 - 3.0.9 could
     * return 2.3.0, 2.3.1, 2.3.2,...., 3.0.7, 3.0.8, 3.0.8
     */
    @Nonnull
    public List<Ref> getVersionTagsForChangelog() throws IOException, GitAPIException {
        return getVersionTags(fromRef, toRef, config.getTagPattern());
    }

    /**
     * Returns the version tags which belongs between the specified versions (inclusive): Example: 2.3.0 - 3.0.9 could
     * return 2.3.0, 2.3.1, 2.3.2,...., 3.0.7, 3.0.8, 3.0.8
     */
    @Nonnull
    public List<Ref> getVersionTags(@Nonnull String from, @Nonnull String to, @Nonnull Pattern pattern) throws IOException, GitAPIException {
        // To is either a ref, or a version
        ObjectId toCommit = getCommitFromString(to);

        return getVersionTags(pattern).stream()
                               .filter(ref -> isAncestorOf(from, ref.getName()))
                               .filter(ref -> {
                                   if (toCommit == null) {
                                       return true;
                                       //return Util.isSameMajorMinorVersion(Util.getTagName(ref), to);
                                   } else {
                                       return isAncestorOf(ref.getName(), to);
                                   }
                               })
                               .collect(Collectors.toList());
    }

    private List<Ref> getVersionTags(@Nonnull Pattern pattern) throws GitAPIException {
        return git.tagList().call().stream()
                .filter(t -> pattern.asPredicate().test(Util.getTagName(t)))
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

    public static Git getGit(@Nonnull Path localDir) throws IOException {
        System.out.printf("Loading repo in: %s\n", localDir.toRealPath());
        FileRepositoryBuilder builder = new FileRepositoryBuilder().findGitDir(localDir.toRealPath().toFile()).readEnvironment();
        if (builder.getGitDir() == null) {
            throw new RepositoryNotFoundException(localDir.toFile());
        }
        return Git.wrap(builder.build());
    }

    @Nonnull
    public ObjectId getOldestCommit() throws GitAPIException {
        RevCommit last = null;
        for (RevCommit commit: git.log().call()) {
            last = commit;
        }
        if (last == null) {
            throw new NullPointerException("Oldest commit could not be found. Is it an empty repo?");
        }
        return last.toObjectId();
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
    @Nullable
    public ObjectId getLatestMergeCommit(@Nonnull String sha, @Nonnull String tip) throws IOException {
        RevWalk walk = new RevWalk(repo);

        final ObjectId from = getCommitFromString(sha);
        final ObjectId to = getCommitFromString(tip);

        walk.markStart(walk.parseCommit(to));
        walk.markUninteresting(walk.parseCommit(from));

        walk.setRetainBody(false);
        walk.setRevFilter(new RevFilter() {
            @Override
            public boolean include(RevWalk walker, RevCommit commit) throws StopWalkException, IOException {
                // Merge commits have more than 1 parent, and we only care about certain paths
                return commit.getParentCount() > 1 && isMergeDecendentOf(commit, from);
            }

            @Override
            public RevFilter clone() {
                return this;
            }
        });

        for (RevCommit commit : walk) {
            return commit.toObjectId();
        }

        return null;
    }

    /**
     * Returns true if there is a linear line of merge-commits between from and commit.
     */
    private boolean isMergeDecendentOf(@Nonnull ObjectId commit, @Nonnull ObjectId from) throws IOException {
        if (from.equals(commit.toObjectId())) {
            return true;
        }

        RevWalk walk = new RevWalk(repo);
        RevCommit revCommit = walk.parseCommit(commit);

        // Walking the path of both parents will always lead to the root commit, but typically it will take
        // a path we don't care about. Instead, force a specific direction. The correct merge commit will be
        // reached by always going "right" (or "left", depending on how you graph looks).
        return (revCommit.getParentCount() > 1 &&
                isAncestorOf(from.getName(), commit.getName()) &&
                isMergeDecendentOf(revCommit.getParent(1), from));
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
        // ^0 ensures that the id is a commit. Annotated tags have their own ids.
        return repo.resolve(base + "^0");
    }

    @Nullable
    public RevCommit getRevCommitFromString(@Nonnull String sha) throws IOException {
        ObjectId commit = getCommitFromString(sha);
        if (commit == null) {
            return null;
        }
        return new RevWalk(repo).parseCommit(commit);
    }

    @Nonnull
    public String getFirstVersionOf(@Nonnull String commit,
                                    @Nonnull List<Ref> versionTags,
                                    @Nonnull String fallback) {
        for (Ref tag : versionTags) {
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

    public boolean isAncestorOfToRef(@Nonnull String commit) {
        return isAncestorOf(commit, toRef);
    }

    public boolean isAncestorOfFromRef(@Nonnull String commit) {
        return isAncestorOf(commit, fromRef);
    }

    public Change convertToSubChange(@Nonnull GitCommitConfig commit,
                                     @Nonnull String category,
                                     @Nonnull String motherVersion) {
        try {
            final RevCommit revCommit = getRevCommitFromString(commit.getSha());
            if (revCommit == null) {
                throw new NullPointerException("Could not find a commit for: " + commit.getSha());
            }
            final String changeText = getChangeText(commit.getText(), revCommit);

            return new Change() {
                @Override
                public int getSortingNumber() {
                    return revCommit.getCommitTime();
                }

                @Nonnull
                @Override
                public List<String> getLabels() {
                    return Collections.singletonList(category);
                }

                @Nonnull
                @Override
                public String getVersion() {
                    return motherVersion;
                }

                @Override
                public String toString() {
                    return changeText;
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getChangeText(@Nonnull String possibleOverride, @Nonnull RevCommit revCommit) {
        final String text;
        if (possibleOverride.isEmpty()) {
            // First line of commit message
            text = revCommit.getShortMessage();
        } else {
            text = possibleOverride;
        }

        List<String> additions = new ArrayList<>();

        if (globalConfig.getGithubConfig().hasUserAndRepo()) {
            // Can format links to commits
            additions.add(String.format("[%s](%s)", revCommit.abbreviate(7).name(),
                    formatCommitLink(globalConfig.getGithubConfig().getUser(),
                            globalConfig.getGithubConfig().getRepo(),
                            revCommit.getId().name())));
        } else {
            // Can't make links to commits
            additions.add(revCommit.abbreviate(7).name());
        }

        if (config.getCommitsConfig().getIncludeAuthor()) {
            additions.add(String.format("(%s <%s>)",
                    revCommit.getAuthorIdent().getName(),
                    revCommit.getAuthorIdent().getEmailAddress()));
        }

        return Util.formatChangeText(text, additions);
    }

    public Change convertToChange(@Nonnull GitCommitConfig commit,
                                  @Nonnull List<Ref> versionTags,
                                  @Nonnull String nextHeader) {
        try {
            final RevCommit revCommit = getRevCommitFromString(commit.getSha());
            if (revCommit == null) {
                throw new NullPointerException("Could not find a commit for: " + commit.getSha());
            }
            final String firstVersion = getFirstVersionOf(commit.getSha(), versionTags, nextHeader);
            final String changeText = getChangeText(commit.getText(), revCommit);

            return new Change() {
                @Override
                public int getSortingNumber() {
                    return revCommit.getCommitTime();
                }

                @Nonnull
                @Override
                public List<String> getLabels() {
                    return Collections.singletonList(commit.getCategory());
                }

                @Nonnull
                @Override
                public String getVersion() {
                    return firstVersion;
                }

                @Override
                public String toString() {
                    return changeText;
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatCommitLink(@Nonnull String user, @Nonnull String repo, @Nonnull String sha) {
        return String.format(GITHUB_COMMIT_LINK, user, repo, sha);
    }
}
