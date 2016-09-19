package org.neo4j.changelog;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
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

        changeLog.write(new File(changeLogPath).toPath());
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("neo4j-changelog")
                .defaultHelp(true)
                .description("Generate changelog for the given project.");

        parser.addArgument("-ght", "--githubtoken")
                .help("GitHub Token (not required but heavily recommended)")
                .setDefault("");
        parser.addArgument("-ghu", "--githubuser")
                .help("Used to build the uri: github.com/user/repo")
                .setDefault("neo4j");
        parser.addArgument("-ghr", "--githubrepo")
                .help("Used to build the uri: github.com/user/repo")
                .setDefault("neo4j");
        parser.addArgument("-o", "--output")
                .help("Path to output file")
                .setDefault("CHANGELOG.md");
        parser.addArgument("-d", "--directory")
                .help("Path to local checked out git repo")
                .setDefault("./");
        parser.addArgument("-f", "--from")
                .help("Gitref from which the changelog is generated. For any tags to be included in the log, this commit must be reachable from them. (default: earliest commit in the log)");
        parser.addArgument("-t", "--to")
                .help("Gitref up to which the changelog is generated. Any tags included in the log must be reachable from this commit.")
                .required(true);
        parser.addArgument("-v", "--version-prefix")
                .dest("version-prefix")
                .help("Only include tags which match the specified version prefix. Example, 3.1 will include all 3.1.x tags but not 3.0.x tags.")
                .required(true);
        parser.addArgument("-n", "--next-header")
                .dest("next-header")
                .help("Any changes occurring after the latest tag will be placed under this header at the top in the log.")
                .setDefault("Unreleased")
                .required(false);
        parser.addArgument("-rl", "--required-label")
                .dest("required-label")
                .help("Only include PRs with this label")
                .required(false)
                .setDefault("");
        parser.addArgument("category")
                .nargs("*")
                .help("Categories to sort changes under. These should match (case-insensitively) the tags of the GitHub issues. Will always include the catch-all category 'Misc'");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        GitHelper gitHelper = null;
        try {
            gitHelper = new GitHelper(new File(ns.getString("directory")));
        } catch (IOException e) {
            System.err.printf("\nError: Could not open git repo at %s: %s\n", ns.getString("directory"), e.getMessage());
            System.exit(1);
        }

        String fromRef = ns.getString("from");
        if (fromRef == null) {
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

        String toRef = ns.getString("to");
        try {
            toRef = gitHelper.getCommitFromString(toRef).getName();
        } catch (IOException | NullPointerException e) {
            System.err.printf("\nError: Could not parse commit for %s: %s\n", toRef, e.getMessage());
            System.exit(1);
        }

        String versionPrefix = ns.getString("version-prefix");
        if (!Util.isSemanticVersion(versionPrefix)) {
            System.err.printf("\nError: version-prefix is not a semantic version: %s\n", versionPrefix);
            System.exit(1);
        }

        List<PullRequest> pullRequests = null;
        try {
            System.out.printf("Fetching pull requests from github.com/%s/%s\n", ns.getString("githubuser"),
                    ns.getString("githubrepo"));
            pullRequests = getPullRequests(ns.getString("githubtoken"),
                    ns.getString("githubuser"),
                    ns.getString("githubrepo"));
            System.out.printf("%d pull requests fetched.\n", pullRequests.size());
        } catch (Exception e) {
            System.err.printf("\nError: An error occurred while fetching pull requests: %s\n", e.getMessage());
            System.exit(1);
        }

        try {
            System.out.printf("Generating changelog between %s and %s for %s\n", fromRef, toRef, versionPrefix);
            generateChangelog(fromRef, toRef, versionPrefix, ns.getString("next-header"), gitHelper, ns.getString("output"),
                    ns.getString("required-label"), ns.getList("category"), pullRequests);
            System.out.printf("\nDone. Changelog written to %s\n", ns.getString("output"));
        } catch (Exception e) {
            System.err.printf("\nError: An error occurred while building changelog: %s\n", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
