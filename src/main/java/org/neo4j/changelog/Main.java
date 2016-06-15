package org.neo4j.changelog;

import org.apache.commons.cli.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.git.GitHelper;
import org.neo4j.changelog.github.GitHubHelper;
import org.neo4j.changelog.github.PullRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    private static List<PullRequest> getPullRequests(@Nonnull String token) {
        GitHubHelper gitHubHelper = new GitHubHelper(token);
        return gitHubHelper.getChangeLogPullRequests();
    }

    private static void generateChangelog(
            @Nonnull String fromRef,
            @Nonnull String toRef,
            @Nonnull String version,
            @Nonnull File localDir,
            @Nonnull Path changeLogPath,
            @Nonnull List<String> categories,
            @Nonnull Stream<PullRequest> pullRequests) throws GitAPIException, IOException {
        GitHelper gitHelper = new GitHelper(localDir);
        List<Ref> versionTags = gitHelper.getVersionTags(fromRef, version);
        ChangeLog changeLog = new ChangeLog(versionTags, version, categories);

        System.out.println("Version tags:");
        versionTags.forEach(t -> System.out.println(Util.getTagName(t)));

        if (!gitHelper.isAncestorOf(fromRef, toRef)) {
            throw new RuntimeException(
                    String.format("%s is not an ancestor of %s, can't generate changelog", fromRef, toRef));
        }

        pullRequests
                .filter(pr -> GitHubHelper.isChangeLogWorthy(pr) && GitHubHelper.isIncluded(pr, version) &&
                        gitHelper.isAncestorOf(pr.getCommit(), toRef) &&
                        !gitHelper.isAncestorOf(pr.getCommit(), fromRef))
                .map(pr -> GitHubHelper.convertToChange(pr,
                        gitHelper.getFirstVersionOf(pr.getCommit(), versionTags, version)))
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogPath);
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        Options options = new Options();

        options.addOption("h", "help", false, "Print help")
                .addOption("ght", "token", true, "GitHub Token (not required but heavily recommended)")
                .addOption("t", "to", true, "Gitref up to which the changelog will be generated")
                .addOption("f", "from", true,
                        "Gitref starting from which the changelog is generated")
                .addOption("d", "directory", true, "Directory of local git repo")
                .addOption("o", "output", true, "Path to output file")
                .addOption("v", "version", true, "Latest/next semantic version of branch");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "program", options );
            System.exit(0);
        }

        List<PullRequest> pullRequests = getPullRequests(cmd.getOptionValue("ght", ""));

        if (pullRequests.isEmpty()) {
            System.err.println("No pull requests found!");
            System.exit(1);
        } else {
            System.out.println("PRs: " + pullRequests.size());
        }

        generateChangelog(
                required(cmd, "f"),
                required(cmd, "t"),
                required(cmd, "v"),
                new File(required(cmd, "d")),
                new File(required(cmd, "o")).toPath(),
                Arrays.asList("Kernel", "Cypher", "Packaging", "HA", "Core-Edge"),
                pullRequests.stream());
    }

    @Nonnull
    private static String required(CommandLine cmd, String option) {
        if (cmd.hasOption(option)) {
            return cmd.getOptionValue(option);
        }
        System.err.println("Missing required option: " + option);
        System.exit(1);
        return "";
    }
}
