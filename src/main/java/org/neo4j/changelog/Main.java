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

    private static void generateChangelog(@Nonnull String nextVersion,
                                          @Nonnull File localDir,
                                          @Nonnull String branchRoot,
                                          @Nonnull String gitRef,
                                          @Nonnull Path changeLogPath,
                                          @Nonnull List<String> categories,
                                          @Nonnull Stream<PullRequest> pullRequests) throws GitAPIException, IOException {
        GitHelper gitHelper = new GitHelper(localDir);
        List<Ref> versionTags = gitHelper.getVersionTags(nextVersion);
        ChangeLog changeLog = new ChangeLog(versionTags, categories);

        System.out.println("Version tags:");
        versionTags.forEach(t -> System.out.println(Util.getTagName(t)));

        // pr.getCommit might not work for this?
        //  gitHelper.isAncestorOf(branchRoot, pr.getCommit())

        pullRequests
                .filter(pr -> GitHubHelper.isChangeLogWorthy(pr) && GitHubHelper.isIncluded(pr, nextVersion) &&
                        gitHelper.isAncestorOf(pr.getCommit(), gitRef))
                .map(pr -> GitHubHelper.convertToChange(pr,
                        gitHelper.getFirstVersionOf(pr.getCommit(), versionTags, nextVersion)))
                .forEach(changeLog::addToChangeLog);

        changeLog.write(changeLogPath);
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        Options options = new Options();

        options.addOption("h", "help", false, "Print help")
                .addOption("t", "token", true, "GitHub Token (not required but heavily recommended)")
                .addOption("b", "branch", true, "Branch to generate changelog for")
                .addOption("br", "branch-root", true,
                        "Root of branch. Example if branch is 3.0: git log 2.3..3.0 --oneline | tail -n 1\n")
                .addOption("d", "directory", true, "Directory of local git repo")
                .addOption("o", "output", true, "Path to output file")
                .addOption("nv", "next-version", true, "Latest/next version of branch");

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

        List<PullRequest> pullRequests = getPullRequests(cmd.getOptionValue("t", ""));

        if (pullRequests.isEmpty()) {
            System.err.println("No pull requests found!");
            System.exit(1);
        } else {
            System.out.println("PRs: " + pullRequests.size());
        }

        generateChangelog(required(cmd, "nv"),
                new File(required(cmd, "d")),
                required(cmd, "br"),
                required(cmd, "b"),
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
